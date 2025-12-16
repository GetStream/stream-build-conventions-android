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

import io.getstream.android.coverage.CoverageOptions
import io.getstream.android.publishing.PublishingOptions
import io.getstream.android.spotless.SpotlessOptions
import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.property

/**
 * Extension for configuring Stream project-wide settings. Apply the `io.getstream.project` plugin
 * to the root project to use this extension.
 */
abstract class StreamProjectExtension
@Inject
constructor(project: Project, objects: ObjectFactory) {

    /** The repository name used for inferring the repository URL. Example: "stream-core-android" */
    val repositoryName: Property<String> =
        objects.property<String>().convention(project.provider { project.rootProject.name })

    /** Spotless formatting configuration */
    val spotless: SpotlessOptions = objects.newInstance<SpotlessOptions>()

    /** Configure Spotless formatting */
    fun spotless(action: Action<SpotlessOptions>) = action.execute(spotless)

    /** Code coverage configuration */
    val coverage: CoverageOptions = objects.newInstance<CoverageOptions>()

    /** Configure code coverage */
    fun coverage(action: Action<CoverageOptions>) = action.execute(coverage)

    /** Publishing configuration */
    val publishing: PublishingOptions = objects.newInstance<PublishingOptions>()

    /** Configure publishing */
    fun publishing(action: Action<PublishingOptions>) = action.execute(publishing)
}

internal fun Project.createProjectExtension(): StreamProjectExtension =
    extensions.create<StreamProjectExtension>("streamProject")

internal fun Project.requireStreamProjectExtension(): StreamProjectExtension =
    requireNotNull(rootProject.extensions.findByType<StreamProjectExtension>()) {
        "${StreamProjectExtension::class.simpleName} not found. " +
            "Apply the io.getstream.project plugin to the root project"
    }
