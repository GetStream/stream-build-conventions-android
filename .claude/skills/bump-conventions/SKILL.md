---
name: bump-conventions
description: Propagate a new release of stream-build-conventions-android to its consumer repos. Creates a bump branch in each consumer, detects current versions, summarizes release notes between current and target, and produces a per-repo plan for the user to approve before any edits are made.
---

# Bump conventions across consumer repos

Use this skill after cutting a new release of `stream-build-conventions-android` to plan and stage version bumps across the consumer SDK repos.

## Inputs

- Optional argument: target version (e.g. `0.12.0` or `v0.12.0`). If omitted, use the latest release tag of `GetStream/stream-build-conventions-android` (run from this repo: `gh release view --json tagName -q .tagName`). Use `X.Y.Z` (no `v`) when writing the gradle version, and `vX.Y.Z` when writing workflow refs.

## Consumer repos

Always operate on all of these unless the user narrows the scope:

- `stream-core-android`
- `stream-chat-android`
- `stream-chat-android-ai`
- `stream-feeds-android`
- `stream-video-android`

### Resolving where they live on disk

Don't assume a specific parent directory — different people check out projects in different places. For each consumer, resolve its path in this order and **stop at the first hit**:

1. **Sibling of this repo.** If this repo is at `<base>/stream-build-conventions-android`, check `<base>/<consumer>`.
2. **Common Android Studio defaults**, in order:
   - `~/AndroidStudioProjects/<consumer>`
   - `~/StudioProjects/<consumer>`
   - `~/IdeaProjects/<consumer>`
   - `~/projects/<consumer>`
3. **Ask the user** for the base directory containing the consumer repos. Once they answer, use it for the remaining repos in this run, and save it as a `reference` memory (e.g. "Android consumer SDK repos for the bump-conventions skill live under `<path>`") so future runs skip the prompt.

If a particular repo is missing from the resolved base but others are present, report it and ask how to proceed.

## Where the version is referenced in consumers

Two surfaces, both must be bumped together:

1. **Gradle plugin version** in `gradle/libs.versions.toml` — entry `streamConventions = "X.Y.Z"` under `[versions]`. The plugin IDs that consume it: `io.getstream.project`, `io.getstream.android.library`, `io.getstream.android.application`, `io.getstream.android.test`, `io.getstream.java.library`, `io.getstream.java.platform`, `io.getstream.publish`.
2. **Reusable workflow refs** in `.github/workflows/*.yml` and `*.yaml` — lines like `uses: GetStream/stream-build-conventions-android/.github/workflows/<file>@vX.Y.Z`. Find them with: `grep -rn "GetStream/stream-build-conventions-android" .github/workflows`.

If a consumer ever pins different versions across these surfaces, treat each as a separate "current version" in the plan — don't paper over the mismatch.

## Steps

Run steps 1–5 in parallel across repos where possible. Do **not** edit any files in this phase — the goal is to land each repo on a clean bump branch and produce a plan for review.

### 1. Resolve the target version

From this repo, fetch latest tag if no argument was given. Confirm the tag exists as a release: `gh release view vX.Y.Z --repo GetStream/stream-build-conventions-android`. Abort with a clear message if it doesn't.

### 2. Pre-flight each consumer repo

For each repo, in parallel:

- Verify the working tree is clean: `git -C <repo> status --porcelain`. If dirty, **stop** and report — do not stash or discard.
- Determine the default branch (usually `develop`. if it's not develop, ask the user): `git -C <repo> symbolic-ref refs/remotes/origin/HEAD --short` → strip `origin/`.
- Fetch: `git -C <repo> fetch origin`.
- Detect current versions:
  - Gradle: `streamConventions` value in `gradle/libs.versions.toml`.
  - Workflows: unique `@vX.Y.Z` suffixes from the grep above.

### 3. Create the bump branch in each consumer

For each repo:

- Branch name: `bump/conventions/<target>`.
- If the branch already exists locally or on origin, **stop** for that repo and ask the user whether to reuse, recreate, or skip — don't auto-delete.
- Otherwise, branch off the up-to-date default branch: `git -C <repo> checkout -b bump/conventions/<target> origin/<default-branch>`.

### 4. Summarize release notes between current → target

For each distinct current version found, list the releases that fall between it (exclusive) and the target (inclusive) using `gh release list --repo GetStream/stream-build-conventions-android` plus `gh release view <tag> --repo GetStream/stream-build-conventions-android`. For each intermediate release, capture:

- Tag and date
- A 1–3 line summary of notable changes
- **Explicitly flag**: breaking changes, new required configuration, removed/renamed plugins, workflow input changes. If a release affects only Gradle plugins or only workflows, note which surface — it tells the consumer what to test.

Only mark a repo as already up to date (and skip it in the plan) when **every** detected surface — Gradle `streamConventions` and all workflow `@vX.Y.Z` refs — equals the target. A mismatch on any single surface keeps the repo in the plan, with a todo for just the surfaces that need bumping.

### 5. Build the plan and present it

**Present the plan via the `ExitPlanMode` tool, not as a chat message.** That gives the user the native accept/reject UI. The plan goes in the `plan` argument as a single markdown string, structured as:

```
# Conventions bump plan → vX.Y.Z

## Releases since each consumer's current version
<grouped summary; one block per distinct current version>

## Per-repo todos

### stream-feeds-android  (0.10.0 → 0.12.0)
Branch: bump/conventions/0.12.0 (created off develop)
- [ ] Update `streamConventions` in gradle/libs.versions.toml to 0.12.0
- [ ] Bump @v0.10.0 → @v0.12.0 in N workflow file(s): <list>
- [ ] <any release-note-driven action, e.g. "rename ignoredModules → excludedModules per v0.11.0">
- [ ] Run ./gradlew help (or build) locally to confirm plugins still apply
- [ ] Open PR titled "Bump build-conventions to v0.12.0"

### stream-core-android  ...
### stream-chat-android  ...
### stream-video-android ...

## Notes / risks
<call out anything surprising: mismatched current versions within a repo, repos already up to date, breaking changes that need code edits beyond version strings>
```

Call `ExitPlanMode` with that markdown as `plan`. Do **not** also dump the plan into chat text — `ExitPlanMode` renders it. After the call, stop and wait for the user's approval signal from the harness; do not start applying edits in the same turn.

## After approval (only if the user confirms)

- Apply the version bumps in each repo on its bump branch.
- Re-run any relevant local verification the user asks for (build, lint).
- Do **not** push or open PRs unless the user explicitly asks.

## Guardrails

- Never run destructive git operations (`reset --hard`, `branch -D`, `push --force`) without explicit user approval — even if the user told you to "fix" a stuck branch.
- Never skip hooks (`--no-verify`).
- If `gh` is unauthenticated or a repo is missing, stop and report — don't fall back to scraping or guessing release notes.
- Keep the plan tight. Bullet points, no prose padding. The user wants to skim it.
