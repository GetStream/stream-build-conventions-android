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

import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.setProperty

/**
 * Extension for configuring Stream project-wide settings. Apply the `io.getstream.project` plugin
 * to the root project to use this extension.
 */
abstract class StreamProjectExtension
@Inject
constructor(project: Project, objects: ObjectFactory) {

    /** The repository name used for inferring the repository URL.Example: "stream-core-android" */
    val repositoryName: Property<String> =
        objects.property<String>().convention(project.provider { project.rootProject.name })

    /** Whether to apply ktfmt formatting instead of ktlint to Kotlin files. Default: false */
    val useKtfmt: Property<Boolean> = objects.property<Boolean>().convention(false)

    /** The version string for published artifacts. */
    val version: Property<String> =
        objects.property<String>().convention(project.provider { project.version.toString() })

    /** Description of the artifacts. */
    val description: Property<String> = objects.property()

    /**
     * Additional Sonar exclusion patterns beyond the defaults. Default exclusions include test
     * files, generated code, etc.
     */
    val sonarExclusions: ListProperty<String> =
        objects.listProperty<String>().convention(emptyList())

    /**
     * Modules to exclude from Sonar analysis by their Gradle path (without leading colon).
     * Examples: "sample", "metrics:app", "buildSrc". These modules will not have Sonar/Kover
     * configured.
     */
    val sonarIgnoredModules: SetProperty<String> =
        objects.setProperty<String>().convention(emptySet())
}

/**
 * Extension for configuring Stream module-specific settings. This extension is created in each
 * module where a Stream convention plugin is applied.
 */
abstract class StreamModuleExtension @Inject constructor(project: Project, objects: ObjectFactory) {

    /** Whether to disable Spotless formatting in this specific module. Default: false */
    val disableSpotless: Property<Boolean> = objects.property<Boolean>().convention(false)

    /** Whether to enable Maven publishing for this module. Default: true */
    val publishingEnabled: Property<Boolean> = objects.property<Boolean>().convention(true)

    /** The Maven artifact ID for this module. Defaults to the module name. */
    val artifactId: Property<String> = objects.property<String>().convention(project.name)
}
