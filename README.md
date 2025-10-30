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
    repositoryName.set("stream-chat-android")
    
    // Choose formatter (default: false = ktlint)
    useKtfmt.set(false)
}
```

### 2. Module Configuration

Apply the appropriate plugin to each module:

```kotlin
plugins {
    id("io.getstream.android.library")
    // or id("io.getstream.android.application")
    // or id("io.getstream.java.library")
}

streamModule {
    // Disable Spotless for this module (default: true)
    spotlessEnabled.set(false)
}
```

## License

See [LICENSE](LICENSE) file for details.
