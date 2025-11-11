/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-build-conventions-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.getstream.android.coverage

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import io.getstream.android.StreamProjectExtension
import io.getstream.android.requireStreamProjectExtension
import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.sonarqube.gradle.SonarExtension

private object SonarConstants {
    const val HOST_URL = "https://sonarcloud.io"
    const val ORGANIZATION = "getstream"

    val EXCLUSIONS =
        listOf(
            "**/test/**",
            "**/androidTest/**",
            "**/R.class",
            "**/R2.class",
            "**/R$*.class",
            "**/BuildConfig.*",
            "**/Manifest*.*",
            "**/*Test*.*",
        )
}

object KoverConstants {
    const val VARIANT_NAME = "coverage"
    const val VARIANT_SUFFIX = "Coverage"
    const val TEST_TASK = "test$VARIANT_SUFFIX"
    val CLASS_EXCLUSIONS = listOf("*R", "*R$*", "*BuildConfig", "*Manifest*", "*Composable*")
    val ANNOTATION_EXCLUSIONS = arrayOf("androidx.compose.ui.tooling.preview.Preview")
}

internal fun Project.configureCoverageRoot() {
    pluginManager.apply("org.sonarqube")

    afterEvaluate {
        val projectExtension = requireStreamProjectExtension()
        val includedModules = projectExtension.coverage.includedModules.get()

        configureKover(projectExtension.coverage, isRoot = true)
        setupKoverDependencyOnModules(includedModules)
        configureSonar(projectExtension)
        registerAggregatedCoverageTask(includedModules)
    }
}

private fun Project.configureSonar(extension: StreamProjectExtension) {
    val repositoryName = extension.repositoryName.get()
    val exclusions = buildList {
        addAll(SonarConstants.EXCLUSIONS)
        addAll(extension.coverage.sonarCoverageExclusions.get())
    }

    extensions.configure<SonarExtension> {
        properties {
            property("sonar.host.url", SonarConstants.HOST_URL)
            property("sonar.token", System.getenv("SONAR_TOKEN"))
            property("sonar.organization", SonarConstants.ORGANIZATION)
            property("sonar.projectKey", "GetStream_$repositoryName")
            property("sonar.projectName", repositoryName)
            property("sonar.java.coveragePlugin", "jacoco")
            property("sonar.sourceEncoding", "UTF-8")
            property("sonar.java.binaries", "$rootDir/**/build/tmp/kotlin-classes/debug")
            property("sonar.coverage.exclusions", exclusions)
            property(
                "sonar.coverage.jacoco.xmlReportPaths",
                layout.buildDirectory
                    .file("/reports/kover/report${KoverConstants.VARIANT_SUFFIX}.xml")
                    .get(),
            )
        }
    }
}

private fun Project.setupKoverDependencyOnModules(includedModules: Set<String>) {
    subprojects.forEach { subproject ->
        if (subproject.name in includedModules) {
            dependencies.add("kover", subproject)
        }
    }
}

internal fun Project.configureCoverageModule() {
    val coverageOptions = requireStreamProjectExtension().coverage

    // Only configure coverage for included modules
    if (name !in coverageOptions.includedModules.get()) {
        return
    }

    pluginManager.apply("org.sonarqube")

    // Configure Android test coverage if this is an Android module
    pluginManager.withPlugin("com.android.library") { configureAndroid<LibraryExtension>() }
    pluginManager.withPlugin("com.android.application") { configureAndroid<ApplicationExtension>() }

    configureKover(coverageOptions, isRoot = false)
    registerModuleCoverageTask()
}

private fun Project.registerAggregatedCoverageTask(includedModules: Set<String>) {
    tasks.register(KoverConstants.TEST_TASK) {
        group = "verification"
        description = "Run all tests in all modules and generate merged coverage report"

        // Depend on all module-specific testCoverage tasks
        val coverageModuleTasks =
            subprojects
                .filter { it.name in includedModules }
                .map { ":${it.name}:${KoverConstants.TEST_TASK}" }
        dependsOn(coverageModuleTasks)

        finalizedBy(
            "koverXmlReport${KoverConstants.VARIANT_SUFFIX}",
            "koverHtmlReport${KoverConstants.VARIANT_SUFFIX}",
        )
    }
}

private fun Project.registerModuleCoverageTask() {
    // Determine the appropriate test task based on module type and plugins
    val hasPaparazziPlugin = pluginManager.hasPlugin("app.cash.paparazzi")
    val hasAndroidPlugin =
        pluginManager.hasPlugin("com.android.library") ||
            pluginManager.hasPlugin("com.android.application")

    val testTaskName =
        when {
            hasPaparazziPlugin -> "verifyPaparazziDebug"
            hasAndroidPlugin -> "testDebugUnitTest"
            else -> "test"
        }

    tasks.register(KoverConstants.TEST_TASK) {
        group = "verification"
        description = "Run module-specific tests"
        dependsOn(testTaskName)
    }
}

private inline fun <reified E : CommonExtension<*, *, *, *, *, *>> Project.configureAndroid() {
    extensions.configure<E> {
        buildTypes {
            getByName("debug") {
                enableUnitTestCoverage = true
                enableAndroidTestCoverage = true
            }
        }
    }
}

private fun Project.configureKover(options: CoverageOptions, isRoot: Boolean) {
    pluginManager.apply("org.jetbrains.kotlinx.kover")

    extensions.configure<KoverProjectExtension> {
        // Create custom variant in each project (including root) for coverage aggregation
        currentProject {
            createVariant(KoverConstants.VARIANT_NAME) {
                if (isRoot) {
                    // Root variant is empty, it just aggregates from dependencies
                } else {
                    add("jvm", "debug", optional = true)
                }
            }
        }

        reports {
            verify.warningInsteadOfFailure.set(true)

            filters.excludes {
                classes(KoverConstants.CLASS_EXCLUSIONS)
                classes(options.koverClassExclusions.get())

                annotatedBy(*KoverConstants.ANNOTATION_EXCLUSIONS)
            }
        }
    }
}
