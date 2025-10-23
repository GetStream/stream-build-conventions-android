plugins {
    base
    alias(libs.plugins.detekt) apply true
    alias(libs.plugins.spotless) apply true
}

val isSnapshot = System.getenv("SNAPSHOT")?.toBoolean() == true

allprojects {
    group = "io.getstream"
    if (isSnapshot) {
        version = "$version-SNAPSHOT"
    }
}

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
