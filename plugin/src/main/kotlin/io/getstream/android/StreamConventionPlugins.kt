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
import com.android.build.api.dsl.TestExtension
import io.getstream.android.spotless.configureSpotless
import org.gradle.api.Plugin
import org.gradle.api.Project

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

            createProjectExtension()
        }
    }
}

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")

            configureAndroid<ApplicationExtension>()
            configureKotlin()
            configureSpotless()
        }
    }
}

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")

            configureAndroid<LibraryExtension>()
            configureKotlin()
            configureSpotless()
        }
    }
}

class AndroidTestConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.test")

            configureAndroid<TestExtension>()
            configureKotlin()
            configureSpotless()
        }
    }
}

class JavaLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("java-library")

            configureJava()
            configureKotlin()
            configureSpotless()
        }
    }
}
