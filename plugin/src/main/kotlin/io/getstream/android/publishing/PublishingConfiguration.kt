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
import org.gradle.kotlin.dsl.project

private const val groupId = "io.getstream"

// Where artifacts go. Selected by a Gradle property so CI flips it with -P (or
// ORG_GRADLE_PROJECT_streamPublishTargets) without a code change.
//
// The DEFAULT IS THE STREAM REPOSITORY. Central is a fallback a repo opts into,
// not the baseline -- the point of this work is that we stop depending on it.
// Nothing changes for a repo until it bumps its pinned conventions SHA, so the
// bump is the cutover for that repo, deliberately and one at a time.
//
// Both targets at once is the dual-publish window: a single `./gradlew publish`
// pushes to every declared repository, so one run produces a Central release and
// a staged tree for the Stream repository from the same signed bytes.
private const val publishTargetsProperty = "streamPublishTargets"
private const val targetCentral = "central"
private const val targetStreamRepo = "streamRepo"

// One tree for the whole build, under the ROOT build directory -- the upload
// step ships a single Maven-2 tree, and a per-module directory would give it
// one tree per module. Matches the composite action's default staged-directory.
private const val stagingDirectory = "staged-repo"

internal fun Project.configurePublishingRoot() {
    pluginManager.apply("org.jetbrains.dokka")

    // Aggregate every subproject that applies Dokka into the root Dokka publication
    val rootDependencies = dependencies
    subprojects {
        pluginManager.withPlugin("org.jetbrains.dokka") {
            rootDependencies.add("dokka", rootDependencies.project(path))
        }
    }
}

internal fun Project.configurePublishingModule() {
    val projectExtension = requireStreamProjectExtension()

    if (name in projectExtension.publishing.ignoredModules.get()) {
        return
    }

    val artifactId = getArtifactId(projectExtension.publishing)

    pluginManager.apply("com.vanniktech.maven.publish")
    pluginManager.apply("org.jetbrains.dokka")
    pluginManager.apply("org.jetbrains.dokka-javadoc")

    this.group = groupId
    this.version = computeVersion()

    val targets = publishTargets()

    pluginManager.withPlugin("com.vanniktech.maven.publish") {
        extensions.configure<MavenPublishBaseExtension> {
            if (targetCentral in targets) {
                publishToMavenCentral(automaticRelease = true)
            }

            if (targetStreamRepo in targets) {
                // Explicit, even though the plugin already signs whenever
                // RELEASE_SIGNING_ENABLED is set: the Stream repository REJECTS an
                // unsigned release run, so the requirement is stated where it is
                // load-bearing rather than left to an environment variable a repo
                // could forget to pass.
                signAllPublications()
            }

            coordinates(groupId = groupId, artifactId = artifactId, version = version.toString())

            configure(computeArtifactPlatform())

            configurePom(projectExtension, artifactId)
        }

        if (targetStreamRepo in targets) {
            configureStreamRepoStaging()
        }
    }

    rootProject.registerPrintAllArtifactsTask()
}

/**
 * Which repositories this build publishes to.
 *
 * Unknown names fail the build rather than being ignored. A typo that silently narrowed the set
 * would publish to fewer places than the release expected and only surface as a missing artifact
 * afterwards.
 */
private fun Project.publishTargets(): Set<String> {
    val raw = providers.gradleProperty(publishTargetsProperty).getOrElse(targetStreamRepo)
    val targets = raw.split(",").map(String::trim).filter(String::isNotEmpty).toSet()

    val known = setOf(targetCentral, targetStreamRepo)
    val unknown = targets - known
    require(targets.isNotEmpty() && unknown.isEmpty()) {
        "'$publishTargetsProperty' must be a comma-separated subset of " +
            "${known.joinToString()} but was '$raw'"
    }

    return targets
}

/**
 * Stages the Maven-2 tree on disk for the Stream repository upload.
 *
 * Gradle could write to the bucket directly over the S3-compatible endpoint, which is fewer moving
 * parts -- but that puts the upload credential in the same job as the signing key. Staging to a
 * directory is what lets CI hand the tree to a separate job that has never seen the key.
 */
private fun Project.configureStreamRepoStaging() {
    val staging = rootProject.layout.buildDirectory.dir(stagingDirectory)

    extensions.configure<PublishingExtension> {
        repositories {
            maven {
                name = "streamRepoStaging"
                url = staging.get().asFile.toURI()
            }
        }
    }
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

            KotlinJvm(
                sourcesJar = true,
                javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationJavadoc"),
            )
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
