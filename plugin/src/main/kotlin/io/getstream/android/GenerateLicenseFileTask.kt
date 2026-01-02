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

import java.io.BufferedReader
import java.time.Year
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Task that generates license header files from templates bundled in the plugin.
 *
 * This task uses [@Classpath] to track the plugin JAR that contains the template resources. When
 * the plugin is updated with new template content, Gradle will automatically detect the change and
 * regenerate the license files.
 */
@CacheableTask
abstract class GenerateLicenseFileTask : DefaultTask() {
    @get:Input abstract val repositoryName: Property<String>

    @get:Input abstract val templateName: Property<String>

    /**
     * The classpath containing the plugin resources (the plugin JAR itself). Used to track changes
     * to the template files so the task is re-run.
     */
    @get:Classpath abstract val pluginClasspath: ConfigurableFileCollection

    @get:OutputFile abstract val outputFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val templateContent =
            javaClass.classLoader
                .getResourceAsStream(templateName.get())
                ?.bufferedReader()
                ?.use(BufferedReader::readText)
                ?: throw IllegalStateException(
                    "Could not find bundled ${templateName.get()} resource"
                )

        outputFile
            .get()
            .asFile
            .writeText(
                templateContent
                    .replace("\$PROJECT", repositoryName.get())
                    .replace("\$YEAR", "${Year.now()}")
            )
    }
}
