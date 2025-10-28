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

import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

private const val groupId = "io.getstream"

internal fun Project.configurePublishing() {
    pluginManager.apply("com.vanniktech.maven.publish")

    val projectExtension = requireStreamProjectExtension()
    val moduleExtension =
        extensions.findByType(StreamModuleExtension::class.java)
            ?: error("StreamModuleExtension not found. This should not happen.")

    this.group = groupId

    afterEvaluate {
        if (!moduleExtension.publishingEnabled.get()) {
            logger.info("[StreamPublishing] Publishing is disabled for ${project.path}")
        }
    }

    pluginManager.withPlugin("com.vanniktech.maven.publish") {
        extensions.configure<MavenPublishBaseExtension> {
            publishToMavenCentral(SonatypeHost.Companion.CENTRAL_PORTAL, automaticRelease = true)

            // Configure coordinates in afterEvaluate to read user-configured values
            afterEvaluate {
                coordinates(
                    groupId = groupId,
                    artifactId = moduleExtension.artifactId.get(),
                    version = projectExtension.version.get(),
                )
            }

            when {
                pluginManager.hasPlugin("com.android.library") -> {
                    configure(
                        AndroidSingleVariantLibrary(
                            variant = "release",
                            sourcesJar = true,
                            publishJavadocJar = true,
                        )
                    )
                }

                pluginManager.hasPlugin("java-library") ||
                    pluginManager.hasPlugin("org.jetbrains.kotlin.jvm") -> {
                    configure(
                        KotlinJvm(
                            javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationHtml"),
                            sourcesJar = true,
                        )
                    )
                }
            }

            pom {
                name.set(moduleExtension.artifactId)
                description.set(projectExtension.description)
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
                        id.set("gpunto")
                        name.set("Gianmarco David")
                        email.set("gianmarco.david@getstream.io")
                    }
                    developer {
                        id.set("PratimMallick")
                        name.set("Pratim Mallick")
                        email.set("pratim.mallick@getstream.io")
                    }
                }

                scm {
                    url.set(
                        projectExtension.repositoryName.map { "https://github.com/GetStream/$it" }
                    )
                    connection.set(
                        projectExtension.repositoryName.map {
                            "scm:git:git://github.com/GetStream/$it.git"
                        }
                    )
                    developerConnection.set(
                        projectExtension.repositoryName.map {
                            "scm:git:ssh://github.com:GetStream/$it.git"
                        }
                    )
                }
            }
        }
    }
}
