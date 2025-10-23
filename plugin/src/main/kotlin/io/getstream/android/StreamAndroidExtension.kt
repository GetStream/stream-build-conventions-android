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

/**
 * Extension for configuring Android SDK versions and other properties in Stream convention plugins.
 *
 * Example usage:
 * ```kotlin
 * streamAndroid {
 *     compileSdk(36)
 *     minSdk(21)
 *     targetSdk(36)
 *     versionName("1.0.0")
 * }
 * ```
 */
abstract class StreamAndroidExtension(
    private val commonExtension: CommonExtension<*, *, *, *, *, *>
) {
    /** Set the Android compileSdk version on the Android extension. */
    fun compileSdk(version: Int) {
        commonExtension.compileSdk = version
    }

    /** Set the Android minSdk version on the Android extension. */
    fun minSdk(version: Int) {
        commonExtension.defaultConfig.minSdk = version
    }

    /** Set the Android targetSdk version on the Android extension. */
    fun targetSdk(version: Int) {
        when (commonExtension) {
            is LibraryExtension -> {
                commonExtension.testOptions.targetSdk = version
                commonExtension.lint.targetSdk = version
            }

            is ApplicationExtension -> {
                commonExtension.defaultConfig.targetSdk = version
            }
        }
    }

    /** Set the Android version name on the Android extension, if it's an ApplicationExtension. */
    fun appVersionName(name: String) {
        (commonExtension as? ApplicationExtension)?.defaultConfig?.versionName = name
    }
}
