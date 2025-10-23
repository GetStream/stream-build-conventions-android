# Bump Version Action

A reusable composite action that bumps semantic versions (semver) based on the specified bump type. It can automatically read from and write to properties files.

## Inputs

| Input         | Description                                 | Required | Default             | Example                      |
|---------------|---------------------------------------------|----------|---------------------|------------------------------|
| `bump`        | Version bump type                           | Yes      | -                   | `major`, `minor`, or `patch` |
| `file`        | Path to properties file containing version  | No       | `gradle.properties` | `gradle.properties`          |
| `version-key` | Key name for version in the properties file | No       | `version`           | `version`                    |

## Outputs

| Output        | Description                   | Example |
|---------------|-------------------------------|---------|
| `new-version` | The new version after bumping | `1.2.4` |

## Usage

### Basic Usage

The action reads the version from a file, bumps it, and writes it back:

```yaml
- name: Bump version
  id: bump
  uses: ./.github/actions/bump-version
  with:
    bump: patch
    # file defaults to gradle.properties

- name: Use new version
  run: echo "New version is ${{ steps.bump.outputs.new-version }}"
```

### Custom Properties File

For non-standard file paths or version keys:

```yaml
- name: Bump version
  id: bump
  uses: ./.github/actions/bump-version
  with:
    bump: minor
    file: custom/path/version.properties
    version-key: appVersion
```

## Examples

### Version Bumping

| Bump Type | Input   | Output  |
|-----------|---------|---------|
| Major     | `1.2.3` | `2.0.0` |
| Minor     | `1.2.3` | `1.3.0` |
| Patch     | `1.2.3` | `1.2.4` |

### File Format

The action expects properties files in the format:
```properties
version=1.2.3
```

You can customize the key name using the `version-key` input if your file uses a different format (e.g., `appVersion=1.2.3`).

## Validation

The action validates:
- File exists at the specified path
- Version key exists in the file
- Version format matches semver pattern (`X.Y.Z`)
- Bump type is one of: `major`, `minor`, or `patch`

If validation fails, the action will exit with an error.
