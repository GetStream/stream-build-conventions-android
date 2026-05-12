import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

plugins {
    base
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.detekt)
    alias(libs.plugins.spotless)
    alias(libs.plugins.dokka)
}

val isSnapshot = System.getenv("SNAPSHOT")?.toBoolean() == true

allprojects {
    group = "io.getstream"
    if (isSnapshot) {
        version = "$version-SNAPSHOT"
    }
}

dependencies { dokka(project(":plugin")) }

spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**/*.kt")
        ktfmt().kotlinlangStyle()
        licenseHeaderFile(rootProject.file("LICENSE_HEADER"))
    }
}

detekt {
    autoCorrect = true
    toolVersion = libs.versions.detekt.get()
    buildUponDefaultConfig = true
}

tasks.register("printAllArtifacts") {
    group = "publishing"
    description = "Prints all artifacts that will be published"

    val coordinates = mutableListOf<String>()
    subprojects.forEach { subproject ->
        subproject.plugins.withId("com.vanniktech.maven.publish") {
            subproject.extensions
                .findByType(PublishingExtension::class.java)
                ?.publications
                ?.filterIsInstance<MavenPublication>()
                ?.forEach { coordinates += "${it.groupId}:${it.artifactId}:${it.version}" }
        }
    }

    doLast { coordinates.forEach(::println) }
}
