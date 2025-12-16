import com.vanniktech.maven.publish.GradlePlugin
import com.vanniktech.maven.publish.JavadocJar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.dokka)
    alias(libs.plugins.maven.publish)
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
    implementation(libs.dokka.gradle.plugin)
    implementation(libs.kover.gradle.plugin)
    implementation(libs.maven.publish.gradle.plugin)
    implementation(libs.sonarqube.gradle.plugin)
    implementation(libs.spotless.gradle.plugin)
}

val repoId = "GetStream/stream-build-conventions-android"
val repoUrl = "https://github.com/$repoId"

gradlePlugin {
    website = repoUrl
    vcsUrl = repoUrl

    plugins {
        create("root") {
            id = "io.getstream.project"
            implementationClass = "io.getstream.android.RootConventionPlugin"
            displayName = "Stream Root Convention Plugin"
            description =
                "Root convention plugin for Stream projects - configures project-wide settings"
            tags = listOf("stream", "conventions", "configuration")
        }
        create("androidLibrary") {
            id = "io.getstream.android.library"
            implementationClass = "io.getstream.android.AndroidLibraryConventionPlugin"
            displayName = "Stream Android Library Convention Plugin"
            description = "Convention plugin for Stream Android library modules"
            tags = listOf("android", "library", "convention", "stream", "kotlin")
        }
        create("androidApplication") {
            id = "io.getstream.android.application"
            implementationClass = "io.getstream.android.AndroidApplicationConventionPlugin"
            displayName = "Stream Android Application Convention Plugin"
            description = "Convention plugin for Stream Android application modules"
            tags = listOf("android", "application", "convention", "stream", "kotlin")
        }
        create("androidTest") {
            id = "io.getstream.android.test"
            implementationClass = "io.getstream.android.AndroidTestConventionPlugin"
            displayName = "Stream Android Test Convention Plugin"
            description = "Convention plugin for Stream Android test modules"
            tags = listOf("android", "test", "convention", "stream", "kotlin")
        }
        create("javaLibrary") {
            id = "io.getstream.java.library"
            implementationClass = "io.getstream.android.JavaLibraryConventionPlugin"
            displayName = "Stream Java Library Convention Plugin"
            description = "Convention plugin for Stream Java/Kotlin JVM library modules"
            tags = listOf("java", "library", "convention", "stream", "kotlin")
        }
        create("javaPlatform") {
            id = "io.getstream.java.platform"
            implementationClass = "io.getstream.android.JavaPlatformConventionPlugin"
            displayName = "Stream Java Platform Convention Plugin"
            description = "Convention plugin for Stream Java/Kotlin JVM platform modules"
            tags = listOf("java", "platform", "convention", "stream", "kotlin")
        }
    }
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    configure(GradlePlugin(javadocJar = JavadocJar.Dokka("dokkaJavadoc"), sourcesJar = true))

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
