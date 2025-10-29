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
package io.getstream.android

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.sonarqube.gradle.SonarExtension

private const val ORGANIZATION = "getstream"
private const val HOST_URL = "https://sonarcloud.io"
private val DEFAULT_EXCLUSIONS =
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

internal fun Project.configureSonarRoot() {
    val projectExtension = requireStreamProjectExtension()

    afterEvaluate {
        // Only configure if SONAR_TOKEN is present
        val sonarToken = System.getenv("SONAR_TOKEN")
        if (sonarToken.isNullOrBlank()) {
            logger.info("[StreamSonar] SONAR_TOKEN not found, skipping Sonar configuration")
            return@afterEvaluate
        }

        pluginManager.apply("org.sonarqube")

        val repositoryName = projectExtension.repositoryName.get()

        // Build exclusion filter: defaults + user additions + ignored modules
        val exclusions = buildList {
            addAll(DEFAULT_EXCLUSIONS)
            addAll(projectExtension.sonarExclusions.get())
            projectExtension.sonarIgnoredModules.get().mapTo(this@buildList) { modulePath ->
                // Convert Gradle path (e.g., "metrics:app") to file path pattern
                "**/${modulePath.replace(":", "/")}/**"
            }
        }

        extensions.configure<SonarExtension> {
            properties {
                property("sonar.host.url", HOST_URL)
                property("sonar.token", sonarToken)
                property("sonar.organization", ORGANIZATION)
                property("sonar.projectKey", "GetStream_$repositoryName")
                property("sonar.projectName", repositoryName)
                property("sonar.java.coveragePlugin", "jacoco")
                property("sonar.sourceEncoding", "UTF-8")
                property("sonar.java.binaries", "${rootDir}/**/build/tmp/kotlin-classes/debug")
                property("sonar.coverage.exclusions", exclusions)
            }
        }
    }
}

internal fun Project.configureSonarModule() {
    val projectExtension = requireStreamProjectExtension()

    afterEvaluate {
        // Only configure Sonar if SONAR_TOKEN is present
        val sonarToken = System.getenv("SONAR_TOKEN")
        if (sonarToken.isNullOrBlank()) {
            return@afterEvaluate
        }

        // Check if this module should be ignored
        val isIgnored = project.path.removePrefix(":") in projectExtension.sonarIgnoredModules.get()
        if (isIgnored) {
            logger.info(
                "[StreamSonar] Module ${project.path} is in the ignored set, skipping Sonar config"
            )
            return@afterEvaluate
        }

        // Apply Kover plugin for coverage
        pluginManager.apply("org.jetbrains.kotlinx.kover")

        extensions.configure<KoverProjectExtension> {
            reports.verify.warningInsteadOfFailure.set(true)
        }

        // Apply Sonar plugin
        pluginManager.apply("org.sonarqube")

        // Configure Android test coverage if this is an Android module
        pluginManager.withPlugin("com.android.library") {
            extensions.configure<LibraryExtension> {
                buildTypes {
                    getByName("debug") {
                        enableUnitTestCoverage = true
                        enableAndroidTestCoverage = true
                    }
                }
            }
        }

        pluginManager.withPlugin("com.android.application") {
            extensions.configure<ApplicationExtension> {
                buildTypes {
                    getByName("debug") {
                        enableUnitTestCoverage = true
                        enableAndroidTestCoverage = true
                    }
                }
            }
        }

        // Configure module-specific Sonar properties
        extensions.configure<SonarExtension> {
            properties {
                property(
                    "sonar.junit.reportPaths",
                    layout.buildDirectory.dir("test-results/testDebugUnitTest").get().asFile.path,
                )
                property(
                    "sonar.coverage.jacoco.xmlReportPaths",
                    layout.buildDirectory.file("reports/kover/reportDebug.xml").get().asFile.path,
                )
            }
        }
    }
}
