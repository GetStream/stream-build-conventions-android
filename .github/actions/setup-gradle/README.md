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
  uses: ./.github/actions/setup-gradle
```

### CI Workflow (write cache for main/develop)
```yaml
- name: Setup Gradle
  uses: ./.github/actions/setup-gradle
  with:
    cache-read-only: ${{ github.ref != 'refs/heads/main' && github.ref != 'refs/heads/develop' }}
```

### Release Workflow (write cache)
```yaml
- name: Setup Gradle
  uses: ./.github/actions/setup-gradle
  with:
    cache-read-only: false
```
