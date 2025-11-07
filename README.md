# Stream Build Conventions for Android

Gradle convention plugins for Stream Android projects. These plugins provide standardized build
configurations to ensure consistency across Stream's Android libraries and applications.

> **Note:** These plugins are designed specifically for Stream's projects and workflows. They aren't
> intended for general-purpose use and may include Stream-specific configurations and conventions.

## Available Plugins

- **`io.getstream.project`** - Root project configuration (apply to root `build.gradle.kts`)
- **`io.getstream.android.library`** - Android library modules
- **`io.getstream.android.application`** - Android application modules
- **`io.getstream.android.test`** - Android test modules
- **`io.getstream.java.library`** - Java/Kotlin JVM library modules
- **`io.getstream.java.platform`** - Java/Kotlin JVM platform modules

## Usage

### Root Project Setup

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

    publishing {
        // Required: Description for all published artifacts
        description = "Magical Stream SDK"

        // Optional: Override artifact IDs for specific modules
        moduleArtifactIdOverrides = mapOf(
            "my-module" to "custom-artifact-id"
        )
    }
}
```

### Module Setup

Apply the appropriate plugin to each module:

```kotlin
plugins {
    id("io.getstream.android.library")
    // or: id("io.getstream.android.application")
    // or: id("io.getstream.android.test")
    // or: id("io.getstream.java.library")
    // or: id("io.getstream.java.platform")
}
```

Library and platform plugins automatically configure Maven publishing.

## Versioning

Version is read from the project version. Can be set, for example, by adding it to
`gradle.properties`:

```properties
version=1.0.0
```

All published modules use this version. For snapshot builds, set the `SNAPSHOT` environment
variable:

```bash
SNAPSHOT=true ./gradlew publish
```

This produces timestamped snapshot versions: `1.0.0-yyyyMMddHHmm-SNAPSHOT`

## Publishing

Published artifacts use:

- **Group ID**: `io.getstream`
- **Artifact ID**: Module name (or override via `moduleArtifactIdOverrides`)
- **Version**: From `gradle.properties`

## License

See [LICENSE](LICENSE) file for details.
