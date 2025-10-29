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
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")

            configureAndroid<ApplicationExtension>()
            configureKotlin()
        }
    }
}

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")

            configureAndroid<LibraryExtension>()
            configureKotlin()
        }
    }
}

class JavaLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("java-library")
            pluginManager.apply("org.jetbrains.kotlin.jvm")

            configureJava()
            configureKotlin()
        }
    }
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
                all(Test::configureTestLogging)
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

    tasks.withType<Test>().configureEach(Test::configureTestLogging)
}

private fun Test.configureTestLogging() = testLogging {
    events("failed")
    showExceptions = true
    showCauses = true
    showStackTraces = true
    exceptionFormat = TestExceptionFormat.FULL
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
