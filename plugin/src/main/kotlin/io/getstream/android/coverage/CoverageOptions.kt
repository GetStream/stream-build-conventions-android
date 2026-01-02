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
package io.getstream.android.coverage

import javax.inject.Inject
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.SetProperty
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.setProperty

abstract class CoverageOptions @Inject constructor(objects: ObjectFactory) {
    /** Modules to include in coverage analysis. Default: none */
    val includedModules: SetProperty<String> = objects.setProperty<String>().convention(emptySet())

    /**
     * Additional Kover exclusion patterns beyond the defaults. Default exclusions include tests,
     * generated code, etc. Expected patterns matching classes/packages, e.g. "*SomeClass",
     * "io.getstream.some.package.*"
     */
    val koverClassExclusions: ListProperty<String> =
        objects.listProperty<String>().convention(emptyList())

    /**
     * Additional Sonar coverage exclusion patterns beyond the defaults. Default exclusions include
     * tests, generated code, etc. Expected patterns matching file paths, e.g.
     * "&#42;&#42;/io/getstream/some/package/&#42;&#42;"
     */
    val sonarCoverageExclusions: ListProperty<String> =
        objects.listProperty<String>().convention(emptyList())
}
