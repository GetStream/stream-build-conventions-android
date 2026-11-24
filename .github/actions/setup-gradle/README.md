# Setup Gradle Action

A reusable composite action that sets up Java and Gradle.

## Inputs

| Input             | Description                       | Required | Default |
|-------------------|-----------------------------------|----------|---------|
| `cache-read-only` | Whether Gradle cache is read-only | No       | `true`  |

## Usage

### Basic Usage (read-only cache)

```yaml
- name: Checkout
  uses: actions/checkout@v4

- name: Setup Gradle
  uses: GetStream/stream-build-conventions-android/.github/actions/setup-gradle@main
```

### Saving the cache

Only pushes to the default branch should save, so pull requests, merge queue, manual and scheduled runs don't fill the repository's Actions cache.

```yaml
- name: Setup Gradle
  uses: GetStream/stream-build-conventions-android/.github/actions/setup-gradle@main
  with:
    cache-read-only: ${{ github.event_name != 'push' || github.ref_name != github.event.repository.default_branch }}
```
