import com.vanniktech.maven.publish.GradlePublishPlugin
import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.dokka)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.gradle.plugin.publish)
}

group = "io.getstream"
description = "Gradle build conventions for Stream Android projects"

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
        freeCompilerArgs.add("-Xjvm-default=all-compatibility")
    }
}

dependencies {
    compileOnly(gradleKotlinDsl())
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
}

val repoId = "GetStream/stream-build-conventions-android"
val repoUrl = "https://github.com/$repoId"

gradlePlugin {
    website = repoUrl
    vcsUrl = repoUrl

    plugins {
        create("androidLibrary") {
            id = "io.getstream.android.library"
            implementationClass = "io.getstream.android.AndroidLibraryConventionPlugin"
            displayName = "Stream Android Library Convention Plugin"
            description = "Convention plugin for Stream Android library modules"
        }
        create("androidApplication") {
            id = "io.getstream.android.application"
            implementationClass = "io.getstream.android.AndroidApplicationConventionPlugin"
            displayName = "Stream Android Application Convention Plugin"
            description = "Convention plugin for Stream Android application modules"
        }
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    configure(GradlePublishPlugin())

    pom {
        name.set("Stream Build Conventions")
        description.set(project.description)
        url.set(repoUrl)

        licenses {
            license {
                name.set("Stream License")
                url.set("$repoUrl/blob/main/LICENSE")
            }
        }

        developers {
            developer {
                id = "aleksandar-apostolov"
                name = "Aleksandar Apostolov"
                email = "aleksandar.apostolov@getstream.io"
            }
            developer {
                id = "VelikovPetar"
                name = "Petar Velikov"
                email = "petar.velikov@getstream.io"
            }
            developer {
                id = "andremion"
                name = "André Mion"
                email = "andre.rego@getstream.io"
            }
            developer {
                id = "rahul-lohra"
                name = "Rahul Kumar Lohra"
                email = "rahul.lohra@getstream.io"
            }
            developer {
                id = "gpunto"
                name = "Gianmarco David"
                email = "gianmarco.david@getstream.io"
            }
            developer {
                id = "PratimMallick"
                name = "Pratim Mallick"
                email = "pratim.mallick@getstream.io"
            }
        }

        scm {
            connection.set("scm:git:git://github.com/$repoId.git")
            developerConnection.set("scm:git:ssh://github.com:$repoId.git")
            url.set(repoUrl)
        }
    }
}

tasks.withType<PublishToMavenRepository>().configureEach {
    mustRunAfter(tasks.publishPlugins)
}

// Publish on the Gradle Plugin Portal only final versions, not snapshots
tasks.publishPlugins {
    enabled = System.getenv("SNAPSHOT")?.toBoolean() != true
}

// Publish on Maven after publishing on the Gradle Plugin Portal
tasks.publish {
    dependsOn(tasks.publishPlugins)
}
