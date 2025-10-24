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

import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

private const val KTLINT_VERSION = "0.50.0"

internal fun Project.configureSpotless() {
    pluginManager.apply("com.diffplug.spotless")

    afterEvaluate {
        val projectExtension = requireStreamProjectExtension()

        // Check if Spotless should be disabled for this module
        if (name in projectExtension.spotlessIgnoredModules.get()) {
            return@afterEvaluate
        }

        val repositoryName =
            projectExtension.repositoryName.orNull
                ?: error("streamProject.repositoryName must be configured in the root project")

        val useKtfmt = projectExtension.useKtfmt.get()

        val generateKotlinLicenseTask =
            registerLicenseGenerationTask(
                taskName = "generateKotlinLicenseHeader",
                repositoryName = repositoryName,
                templateName = "license-header.txt",
                fileName = "license.kt",
            )

        val generateXmlLicenseTask =
            registerLicenseGenerationTask(
                taskName = "generateXmlLicenseHeader",
                repositoryName = repositoryName,
                templateName = "license-header.xml",
                fileName = "license.xml",
            )

        extensions.configure<SpotlessExtension> {
            val kotlinLicenseFile = generateKotlinLicenseTask.get().outputFile.get().asFile
            val xmlLicenseFile = generateXmlLicenseTask.get().outputFile.get().asFile

            encoding = Charsets.UTF_8
            kotlin {
                target("**/*.kt")
                targetExclude("**/build/**/*.kt")

                projectExtension.spotlessExcludePatterns.orNull
                    ?.takeUnless(Set<String>::isEmpty)
                    ?.let { patterns -> targetExclude(*patterns.toTypedArray()) }

                if (useKtfmt) {
                    ktfmt().kotlinlangStyle()
                } else {
                    // For now, we are fixing the ktlint version to the one currently used by Chat &
                    // Video to dodge this issue: https://github.com/diffplug/spotless/issues/1913
                    ktlint(KTLINT_VERSION)
                }

                trimTrailingWhitespace()
                endWithNewline()
                licenseHeaderFile(kotlinLicenseFile)
            }
            java {
                importOrder()
                removeUnusedImports()
                googleJavaFormat()
                trimTrailingWhitespace()
                endWithNewline()
                licenseHeaderFile(kotlinLicenseFile)
            }
            kotlinGradle {
                target("*.gradle.kts")
                ktlint(KTLINT_VERSION)
                trimTrailingWhitespace()
                endWithNewline()
            }
            format("xml") {
                target("**/*.xml")
                targetExclude("**/build/**/*.xml", "**/detekt-baseline.xml")
                licenseHeaderFile(xmlLicenseFile, "(<[^!?])")
            }
        }

        // Make Spotless tasks depend on license generation tasks
        tasks
            .matching { it.name.startsWith("spotless") }
            .configureEach { dependsOn(generateKotlinLicenseTask, generateXmlLicenseTask) }
    }
}

/**
 * Registers a task to generate a license file from a bundled template. Safe to call from multiple
 * subprojects, as it will reuse the task if it already exists.
 */
private fun Project.registerLicenseGenerationTask(
    taskName: String,
    repositoryName: String,
    templateName: String,
    fileName: String,
) =
    rootProject.tasks.findOrRegister<GenerateLicenseFileTask>(taskName) {
        this.repositoryName.set(repositoryName)
        this.templateName.set(templateName)

        // Set the plugin's classpath as an input so Gradle tracks when the plugin JAR changes
        pluginClasspath.from(
            GenerateLicenseFileTask::class.java.protectionDomain.codeSource.location
        )

        // Set the output file location
        val outputDir = rootProject.layout.buildDirectory.dir("stream-spotless-config")
        outputFile.set(outputDir.map { it.file(fileName) })
    }

private inline fun <reified T : Task> TaskContainer.findOrRegister(
    name: String,
    noinline configuration: T.() -> Unit,
) = findByName(name)?.let { named<T>(name) } ?: register<T>(name, configuration)
