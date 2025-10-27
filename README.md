# Stream Build Conventions for Android

Gradle convention plugins for Stream Android projects. These plugins provide standardized build
configurations to ensure consistency across Stream's Android libraries and applications.

> **Note:** These plugins are designed specifically for Stream's projects and workflows. They aren't
> intended for general-purpose use and may include Stream-specific configurations and conventions.

## Overview

This repository contains reusable Gradle convention plugins that encapsulate common build logic,
dependencies, and configurations used across Stream's Android projects.

## Available Plugins

- **`io.getstream.android.library`** - For Android library modules
- **`io.getstream.android.application`** - For Android application modules
- **`io.getstream.java.library`** - For Java/Kotlin JVM library modules

## Usage

Add the plugin to your project's build file:

```kotlin
plugins {
    id("io.getstream.android.library") version "<version>"
    // or
    id("io.getstream.android.application") version "<version>"
    // or
    id("io.getstream.java.library") version "<version>"
}
```

## Distribution

These plugins are published to:

- [Maven Central](https://central.sonatype.com/)
- [Gradle Plugin Portal](https://plugins.gradle.org/)

## License

See [LICENSE](LICENSE) file for details.
