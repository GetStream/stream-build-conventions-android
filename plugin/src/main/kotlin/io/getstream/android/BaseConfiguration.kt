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

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.AppliedPlugin
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

private val javaVersion = JavaVersion.VERSION_11
private val jvmTargetVersion = JvmTarget.JVM_11

internal inline fun <reified Ext : CommonExtension<*, *, *, *, *, *>> Project.configureAndroid() {
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

internal fun Project.configureJava() {
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

internal fun Project.configureKotlin() {
    val configure = { _: AppliedPlugin ->
        tasks.withType<KotlinCompile>().configureEach {
            compilerOptions { jvmTarget.set(jvmTargetVersion) }
        }
    }

    // Configure the Kotlin plugin that is applied, if any
    pluginManager.withPlugin("org.jetbrains.kotlin.android", configure)
    pluginManager.withPlugin("org.jetbrains.kotlin.jvm", configure)
}
