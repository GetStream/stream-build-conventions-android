# Release Workflow

A reusable GitHub Actions workflow for automating releases of Gradle-based projects with semantic
versioning, Maven publishing, and branch management.

## Overview

The release workflow (`release.yml`) provides:

- **Semantic versioning** - Automatic version bumping (major, minor, patch)
- **Maven publishing** - Build and publish to Maven Central
- **Branch management** - Automatic syncing of main, develop, and ci-release branches
- **GitHub releases** - Automatic release creation with generated notes
- **Snapshot support** - Optional snapshot releases without branch updates
- **Signing** - Automatic artifact signing with GPG

## Usage

### In Your Project

Create a workflow file (e.g., `.github/workflows/publish-new-version.yml`) in your project:

```yaml
name: Publish New Version

on:
  workflow_dispatch:
    inputs:
      bump:
        description: 'Version bump type (major, minor, patch)'
        required: true
        type: choice
        options:
          - patch
          - minor
          - major
      snapshot:
        description: 'Snapshot release'
        required: false
        type: boolean
        default: false

concurrency:
  group: release
  cancel-in-progress: false

jobs:
  release:
    uses: GetStream/stream-build-conventions-android/.github/workflows/release.yml@develop
    with:
      bump: ${{ inputs.bump }}
      snapshot: ${{ inputs.snapshot }}
    secrets:
      github-token: ${{ secrets.STREAM_PUBLIC_BOT_TOKEN }}
      maven-central-username: ${{ secrets.MAVEN_USERNAME }}
      maven-central-password: ${{ secrets.MAVEN_PASSWORD }}
      signing-key: ${{ secrets.SIGNING_KEY }}
      signing-key-id: ${{ secrets.SIGNING_KEY_ID }}
      signing-key-password: ${{ secrets.SIGNING_PASSWORD }}
```

### Requirements

Your project must have:

1. **Branch structure**:
    - `develop` - Development branch (default branch to release from)
    - `main` - Production branch
    - `ci-release` - Created automatically during release

2. **Version file**: A properties file with semantic version (defaults to `gradle.properties`, but
   can be customized via the `version-properties-file` input):
   ```properties
   version=1.2.3
   ```

3. **Gradle project**: A Gradle project with a `publish` task configured

## Inputs

| Input                     | Required | Default             | Description                                     |
|---------------------------|----------|---------------------|-------------------------------------------------|
| `bump`                    | Yes      | -                   | Version bump type: `major`, `minor`, or `patch` |
| `snapshot`                | No       | `false`             | Whether this is a snapshot release              |
| `version-properties-file` | No       | `gradle.properties` | Path to file containing version                 |

## Secrets

| Secret                   | Required | Description                                                                         |
|--------------------------|----------|-------------------------------------------------------------------------------------|
| `github-token`           | Yes      | GitHub token with repo with write permissions (i.e. able to push to main & develop) |
| `maven-central-username` | Yes      | Maven Central username for publishing                                               |
| `maven-central-password` | Yes      | Maven Central password for publishing                                               |
| `signing-key`            | Yes      | GPG signing key for artifact signing                                                |
| `signing-key-id`         | Yes      | GPG signing key ID                                                                  |
| `signing-key-password`   | Yes      | GPG signing key password                                                            |

## Snapshot vs Production Releases

Both release types bump the version and run `./gradlew publish`. The key differences:

| Feature            | Production (`snapshot: false`) | Snapshot (`snapshot: true`) |
|--------------------|--------------------------------|-----------------------------|
| Version commit     | Pushed to ci-release           | Local only                  |
| Branch updates     | main and develop synced        | No branches updated         |
| GitHub release     | Created with tag               | Not created                 |
| `SNAPSHOT` env var | `"false"`                      | `"true"`                    |

## Environment Variables

The workflow sets these environment variables during the publish step:

| Variable                                        | Description                                                    |
|-------------------------------------------------|----------------------------------------------------------------|
| `SNAPSHOT`                                      | `"true"` or `"false"` - Access via `System.getenv("SNAPSHOT")` |
| `ORG_GRADLE_PROJECT_RELEASE_SIGNING_ENABLED`    | Always `"true"`                                                |
| `ORG_GRADLE_PROJECT_mavenCentralUsername`       | From secrets                                                   |
| `ORG_GRADLE_PROJECT_mavenCentralPassword`       | From secrets                                                   |
| `ORG_GRADLE_PROJECT_signingInMemoryKey`         | From secrets                                                   |
| `ORG_GRADLE_PROJECT_signingInMemoryKeyId`       | From secrets                                                   |
| `ORG_GRADLE_PROJECT_signingInMemoryKeyPassword` | From secrets                                                   |
