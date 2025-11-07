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
package io.getstream.android.publishing

import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import com.vanniktech.maven.publish.JavaPlatform
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.Platform
import io.getstream.android.StreamProjectExtension
import io.getstream.android.findOrRegister
import io.getstream.android.requireStreamProjectExtension
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.findByType

private const val groupId = "io.getstream"

internal fun Project.configurePublishing() {
    val projectExtension = requireStreamProjectExtension()
    val artifactId = getArtifactId(projectExtension.publishing)

    pluginManager.apply("com.vanniktech.maven.publish")
    pluginManager.apply("org.jetbrains.dokka")

    this.group = groupId
    this.version = computeVersion()

    pluginManager.withPlugin("com.vanniktech.maven.publish") {
        extensions.configure<MavenPublishBaseExtension> {
            publishToMavenCentral(automaticRelease = true)

            coordinates(groupId = groupId, artifactId = artifactId, version = version.toString())

            configure(computeArtifactPlatform())

            configurePom(projectExtension, artifactId)
        }
    }

    rootProject.registerPrintAllArtifactsTask()
}

// Get the overridden artifact ID if present or use the project name as default
private fun Project.getArtifactId(publishing: PublishingOptions): String =
    publishing.moduleArtifactIdOverrides.get().getOrDefault(name, name)

private fun Project.computeVersion(): String {
    val isSnapshot = System.getenv("SNAPSHOT")?.toBoolean() == true
    return if (isSnapshot) {
        val timestamp =
            DateTimeFormatter.ofPattern("yyyyMMddHHmm")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now())

        "$version-$timestamp-SNAPSHOT"
    } else version.toString()
}

// Discover the artifact platform based on the applied plugins
private fun Project.computeArtifactPlatform(): Platform =
    when {
        pluginManager.hasPlugin("com.android.library") -> {
            AndroidSingleVariantLibrary(
                variant = "release",
                sourcesJar = true,
                publishJavadocJar = true,
            )
        }

        pluginManager.hasPlugin("java-library") -> {
            if (!pluginManager.hasPlugin("org.jetbrains.kotlin.jvm")) {
                throw IllegalStateException(
                    "The 'kotlin-jvm' plugin must be applied before the " +
                        "'stream.java.library' plugin"
                )
            }

            KotlinJvm(sourcesJar = true, javadocJar = JavadocJar.Dokka("dokkaJavadoc"))
        }

        pluginManager.hasPlugin("java-platform") -> {
            JavaPlatform()
        }

        else ->
            error(
                "Unsupported project type for publishing. The project must apply either " +
                    "'com.android.library', 'java-library' or 'java-platform' plugin."
            )
    }

private fun MavenPublishBaseExtension.configurePom(
    projectExtension: StreamProjectExtension,
    artifactId: String,
) {
    pom {
        name.set(artifactId)
        description.set(projectExtension.publishing.description)
        url.set(projectExtension.repositoryName.map { "https://github.com/GetStream/$it" })

        licenses {
            license {
                name.set("Stream License")
                url.set(
                    projectExtension.repositoryName.map {
                        "https://github.com/GetStream/$it/blob/main/LICENSE"
                    }
                )
            }
        }

        developers {
            developer {
                id.set("aleksandar-apostolov")
                name.set("Aleksandar Apostolov")
                email.set("aleksandar.apostolov@getstream.io")
            }
            developer {
                id.set("VelikovPetar")
                name.set("Petar Velikov")
                email.set("petar.velikov@getstream.io")
            }
            developer {
                id.set("andremion")
                name.set("André Mion")
                email.set("andre.rego@getstream.io")
            }
            developer {
                id.set("rahul-lohra")
                name.set("Rahul Kumar Lohra")
                email.set("rahul.lohra@getstream.io")
            }
            developer {
                id.set("PratimMallick")
                name.set("Pratim Mallick")
                email.set("pratim.mallick@getstream.io")
            }
            developer {
                id.set("gpunto")
                name.set("Gianmarco David")
                email.set("gianmarco.david@getstream.io")
            }
        }

        scm {
            url.set(projectExtension.repositoryName.map { "https://github.com/GetStream/$it" })
            connection.set(
                projectExtension.repositoryName.map { "scm:git:git://github.com/GetStream/$it.git" }
            )
            developerConnection.set(
                projectExtension.repositoryName.map { "scm:git:ssh://github.com:GetStream/$it.git" }
            )
        }
    }
}

private fun Project.registerPrintAllArtifactsTask() {
    tasks.findOrRegister<Task>("printAllArtifacts") {
        group = "publishing"
        description = "Prints all artifacts that will be published"

        doLast {
            subprojects.forEach { subproject ->
                subproject.plugins.withId("com.vanniktech.maven.publish") {
                    subproject.extensions
                        .findByType<PublishingExtension>()
                        ?.publications
                        ?.filterIsInstance<MavenPublication>()
                        ?.forEach { println("${it.groupId}:${it.artifactId}:${it.version}") }
                }
            }
        }
    }
}
