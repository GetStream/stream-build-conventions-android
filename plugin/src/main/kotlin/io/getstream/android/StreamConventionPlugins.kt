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
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.AppliedPlugin
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * Root-level convention plugin for Stream projects. Apply this plugin to the root project to
 * configure project-wide settings.
 */
class RootConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            require(this == rootProject) {
                "The io.getstream.project plugin should be applied to the root project only"
            }

            // Create the project-wide extension
            extensions.create<StreamProjectExtension>("streamProject")
            configureSonarRoot()
        }
    }
}

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")

            createModuleExtension()
            configureAndroid<ApplicationExtension>()
            configureKotlin()
            configureSpotless()
            configureSonarModule()
        }
    }
}

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")

            createModuleExtension()
            configureAndroid<LibraryExtension>()
            configureKotlin()
            configureSpotless()
            configureSonarModule()
            configurePublishing()
        }
    }
}

class JavaLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("java-library")
            pluginManager.apply("org.jetbrains.kotlin.jvm")

            createModuleExtension()
            configureJava()
            configureKotlin()
            configureSpotless()
            configureSonarModule()
            configurePublishing()
        }
    }
}

private fun Project.createModuleExtension() {
    extensions.create<StreamModuleExtension>("streamModule")
}

private val javaVersion = JavaVersion.VERSION_11
private val jvmTargetVersion = JvmTarget.JVM_11

private inline fun <reified Ext : CommonExtension<*, *, *, *, *, *>> Project.configureAndroid() {
    val commonExtension = extensions.getByType<Ext>()

    commonExtension.apply {
        compileOptions {
            sourceCompatibility = javaVersion
            targetCompatibility = javaVersion
        }

        testOptions {
            unitTests {
                isIncludeAndroidResources = true
                isReturnDefaultValues = true
                all {
                    it.testLogging {
                        events("failed")
                        showExceptions = true
                        showCauses = true
                        showStackTraces = true
                        exceptionFormat = TestExceptionFormat.FULL
                    }
                }
            }
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = javaVersion.toString()
        targetCompatibility = javaVersion.toString()
    }
}

private fun Project.configureJava() {
    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = javaVersion.toString()
        targetCompatibility = javaVersion.toString()
    }

    tasks.withType<Test>().configureEach {
        testLogging {
            events("failed")
            showExceptions = true
            showCauses = true
            showStackTraces = true
            exceptionFormat = TestExceptionFormat.FULL
        }
    }
}

private fun Project.configureKotlin() {
    val configure =
        fun(_: AppliedPlugin) {
            tasks.withType<KotlinCompile>().configureEach {
                compilerOptions { jvmTarget.set(jvmTargetVersion) }
            }
        }

    // Configure the Kotlin plugin that is applied, if any
    pluginManager.withPlugin("org.jetbrains.kotlin.android", configure)
    pluginManager.withPlugin("org.jetbrains.kotlin.jvm", configure)
}

internal fun Project.requireStreamProjectExtension(): StreamProjectExtension =
    rootProject.extensions.findByType<StreamProjectExtension>()
        ?: error(
            "${StreamProjectExtension::class.simpleName} not found. " +
                "Apply the io.getstream.project plugin to the root project"
        )
