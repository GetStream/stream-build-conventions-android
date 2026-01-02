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
package io.getstream.android.spotless

import javax.inject.Inject
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.setProperty

abstract class SpotlessOptions @Inject constructor(objects: ObjectFactory) {

    /** Whether to apply ktfmt formatting instead of ktlint to Kotlin files. Default: false */
    val useKtfmt: Property<Boolean> = objects.property<Boolean>().convention(false)

    /** Modules to exclude from Spotless formatting. Default: none */
    val ignoredModules: SetProperty<String> = objects.setProperty<String>().convention(emptySet())

    /** File patterns to exclude from Spotless formatting beyond build files. Default: none */
    val excludePatterns: SetProperty<String> = objects.setProperty<String>().convention(emptySet())
}
