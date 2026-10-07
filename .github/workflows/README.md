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
| `publish-targets`         | No       | `streamRepo`        | Where to publish: `streamRepo`, `central`, or both |

## Secrets

| Secret                   | Required | Description                                                                         |
|--------------------------|----------|-------------------------------------------------------------------------------------|
| `github-token`           | Yes      | GitHub token with repo with write permissions (i.e. able to push to main & develop) |
| `maven-central-username` | Yes      | Maven Central username for publishing                                               |
| `maven-central-password` | Yes      | Maven Central password for publishing                                               |
| `signing-key`            | Yes      | GPG signing key for artifact signing                                                |
| `signing-key-id`         | Yes      | GPG signing key ID                                                                  |
| `signing-key-password`   | Yes      | GPG signing key password                                                            |

Four more are needed only when `publish-targets` includes `streamRepo`. They are
an **R2 S3 credential scoped to the staging bucket**, not a Cloudflare API token,
and they are read only by the upload jobs — which never see the signing key.

| Secret                          | Required | Description                                      |
|---------------------------------|----------|--------------------------------------------------|
| `stream-repo-endpoint`          | No       | S3 API endpoint of the R2 account                |
| `stream-repo-bucket`            | No       | Staging bucket name                              |
| `stream-repo-access-key-id`     | No       | R2 access key id, scoped to the staging bucket   |
| `stream-repo-secret-access-key` | No       | R2 secret access key                             |

## Publish targets

`publish-targets` selects the repositories a run publishes to. It reaches Gradle
as `ORG_GRADLE_PROJECT_streamPublishTargets` — an environment variable rather
than `-P`, so a value containing a space cannot word-split into a second Gradle
argument. An unrecognised name fails the build rather than being skipped.

**Two defaults, and they differ on purpose.** This input defaults to
`streamRepo`, and CI always passes it explicitly. The *plugin* falls back to
`central` when nothing passes the property at all, which covers the two cases
where that happens:

- **Local builds.** `publishToMavenLocal` with no property would otherwise take
  the `streamRepo` path, and vanniktech makes signing required for any non
  `-SNAPSHOT` version — breaking the usual `-Pversion=local-test` flow with
  `no configured signatory`.
- **New plugin, old workflow.** SDK repos bump the `release.yml` pin and the
  plugin version from different Dependabot ecosystems (`github-actions` and
  `gradle`), so they arrive as separate PRs and this window is routine. An old
  workflow passes no property; defaulting to `streamRepo` there would stage to
  disk with nothing to upload it, and tag and sync a release whose artifacts
  exist nowhere.

| Value                  | Effect                                                              |
|------------------------|---------------------------------------------------------------------|
| `streamRepo`           | The Stream repository only. **The default**                         |
| `central`              | Maven Central only. The fallback, opted into per repo               |
| `streamRepo,central`   | Both, from one `./gradlew publish` and one set of signed bytes      |

The Stream repository is the default because not depending on Central is the
point of the exercise. Nothing changes for a repo until it bumps its pinned
conventions SHA, so **that bump is the cutover for that repo** — deliberate, and
one repo at a time. A repo that is not ready passes `central` explicitly.

Both at once is the dual-publish window rather than a special mode: `publish`
pushes to every declared repository, so a single run produces a Central release
and a staged tree for the Stream repository. While both are live Central stays
authoritative — a failed upload fails the run but does not block the branch sync.

**This repo is the pilot, and it publishes to `streamRepo` only** — no dual
publish, no exception. The plugin is an ordinary Maven artifact under
`io/getstream/` like everything else, and it goes first because it is the
cheapest thing to get wrong: a plugin that will not resolve blocks the handful of
Android repos we own, while an SDK that will not resolve blocks every customer
build.

Nothing breaks when Central stops receiving it, for the same reason it does not
break for an SDK: **every version already on Central stays there and stays
resolvable.** A repo that does not bump its pin never notices. A repo that does
bump adds our repository to its `pluginManagement.repositories` in the same pull
request, because the new version exists nowhere else.

There is no bootstrap deadlock either. This repo's own build applies `base`,
`kotlin-jvm`, `detekt`, `spotless` and `dokka` — never the plugin it produces —
so republishing it never depends on resolving it.

Publishing here only is also what makes the pilot mean something. Consumers
resolve the plugin on **every** build, so each PR exercises publish and resolve
together rather than once per release — and with the artifact in no other
repository, a consumer that lists ours has to actually reach it, instead of
falling back to Central and reporting green while exercising nothing.

**One bootstrap step, once.** `release.yml` references the upload action as
`@main`, matching `bump-version` and `setup-gradle`, so `main` has to carry it
before any upload job runs. After this merges to `develop`, fast-forward it:

```
git push origin origin/develop:main
```

`main` is the stable mirror and sits behind `develop` by whatever has not been
released, so this is the same move `sync_branches` makes at the end of a release
— just done directly instead of as a side effect of one. Nothing needs to be
published to Central to get there.

The `central` option on the dispatch input stays as a manual escape hatch if a
release ever has to go out before the Stream repository can take it.

## Snapshot vs Production Releases

Both release types bump the version and run `./gradlew publish`. The key differences:

| Feature            | Production (`snapshot: false`) | Snapshot (`snapshot: true`) |
|--------------------|--------------------------------|-----------------------------|
| Version commit     | Pushed to ci-release           | Local only                  |
| Branch updates     | main and develop synced        | No branches updated         |
| GitHub release     | Created with tag               | Not created                 |
| `SNAPSHOT` env var | `"false"`                      | `"true"`                    |

## API Docs

The release workflow does not build API docs. `publish-api-docs.yml` builds the
Dokka HTML site and pushes it to `gh-pages`, off the release's critical path.
Repos that publish API docs trigger it on push to `main`, which the release
workflow fast-forwards after every `develop` release:

```yaml
name: Publish API Docs

on:
  push:
    branches: [main]
  workflow_dispatch:

jobs:
  docs:
    uses: GetStream/stream-build-conventions-android/.github/workflows/publish-api-docs.yml@<sha>
    permissions:
      contents: write
    secrets:
      slack-webhook-url: ${{ secrets.SLACK_WEBHOOK_ANDROID_CICD }}
```

Manual runs publish only from `main`. Failures post to the Slack webhook.

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
