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
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")

            configureAndroid<ApplicationExtension>()
        }
    }
}

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")

            configureAndroid<LibraryExtension>()
        }
    }
}

private val javaVersion = JavaVersion.VERSION_11
private val jvmTargetVersion = JvmTarget.JVM_11

private inline fun <reified Ext : CommonExtension<*, *, *, *, *, *>> Project.configureAndroid() {
    val commonExtension = extensions.getByType(Ext::class.java)

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

    // Configure the Kotlin plugin if it is applied
    pluginManager.withPlugin("org.jetbrains.kotlin.android") {
        extensions.configure<KotlinAndroidProjectExtension> {
            compilerOptions { jvmTarget.set(jvmTargetVersion) }
        }
    }
}
