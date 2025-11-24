# Stream Build Conventions for Android

Gradle convention plugins for Stream Android projects. These plugins provide standardized build
configurations to ensure consistency across Stream's Android libraries and applications.

> **Note:** These plugins are designed specifically for Stream's projects and workflows. They aren't
> intended for general-purpose use and may include Stream-specific configurations and conventions.

## Overview

This repository contains reusable Gradle convention plugins that encapsulate common build logic,
dependencies, and configurations used across Stream's Android projects.

## Available Plugins

- **`io.getstream.project`** - Root project configuration (apply to root `build.gradle.kts`)
- **`io.getstream.android.library`** - For Android library modules
- **`io.getstream.android.application`** - For Android application modules
- **`io.getstream.android.test`** - For Android test modules
- **`io.getstream.java.library`** - For Java/Kotlin JVM library modules

## Usage

### 1. Root Project Configuration

Apply the root plugin in your root `build.gradle.kts`:

```kotlin
plugins {
    id("io.getstream.project") version "<version>"
}

streamProject {
    // Repository name for GitHub URLs and license headers (default: project name)
    repositoryName = "stream-chat-android"

    spotless {
        // Choose formatter (default: false = ktlint)
        useKtfmt = false

        // Exclude specific modules from Spotless formatting (default: empty)
        ignoredModules = setOf("some-module")

        // Exclude file patterns from Spotless formatting (default: empty)
        excludePatterns = setOf("**/generated/**")
    }

    coverage {
        // Modules to include in coverage analysis (default: empty)
        includedModules = setOf("some-module", "some-ui-module")

        // Additional Kover exclusion patterns for classes/packages (default: empty)
        koverClassExclusions = listOf("*SomeClass", "io.getstream.some.package.*")

        // Additional Sonar coverage exclusion patterns for file paths (default: empty)
        sonarCoverageExclusions = listOf("**/io/getstream/some/package/**")
    }
}
```

### 2. Module Configuration

Apply the appropriate plugin to each module:

```kotlin
plugins {
    id("io.getstream.android.library")
    // or id("io.getstream.android.application")
    // or id("io.getstream.android.test")
    // or id("io.getstream.java.library")
}
```

## License

See [LICENSE](LICENSE) file for details.
