/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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
package io.getstream.android.publishing

import javax.inject.Inject
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.property

abstract class PublishingOptions @Inject constructor(objects: ObjectFactory) {
    /** Description of the project. Used for published artifacts. */
    val description: Property<String> = objects.property()

    /**
     * Map of module names to custom artifact IDs. Use this to override the default artifact ID for
     * specific modules.
     */
    val moduleArtifactIdOverrides: MapProperty<String, String> =
        objects.mapProperty<String, String>().convention(emptyMap())
}
