# LittleHorse Release Manager

A set of Python scripts to automate the release process for the [LittleHorse](https://github.com/littlehorse-enterprises/littlehorse) monorepo.

## Overview

This document describes the end-to-end workflow—from version bumping and tagging to publishing artifacts and generating changelogs—so that releases are repeatable, auditable, and less error-prone.

## Release Artifacts

A full LittleHorse release produces the following artifacts:

| Artifact | Registry / Distribution                                             | Source |
|---|---------------------------------------------------------------------|---|
| **LH Server** | GHCR or Docker Hub (`littlehorse/lh-server`)                 | `server/` |
| **LH Dashboard** | GHCR or Docker Hub (`littlehorse/lh-dashboard`)                     | `dashboard/` |
| **LH Standalone** | GHCR or Docker Hub (`littlehorse/lh-standalone`)                            | `docker/standalone/` |
| **LH Canary** | GHCR or Docker Hub (`littlehorse/lh-canary`)                                | `canary/` |
| **lhctl** | GHCR or Docker Hub (`littlehorse/lhctl`) / GitHub Release                   | `lhctl/` |
| **sdk-java** | Maven Central (`io.littlehorse:sdk-java`)                           | `sdk-java/` |
| **test-utils** | Maven Central (`io.littlehorse:test-utils`)                         | `test-utils/` |
| **test-utils-container** | Maven Central (`io.littlehorse:test-utils-container`)               | `test-utils-container/` |
| **sdk-python** | PyPI (`littlehorse-client`)                                         | `sdk-python/` |
| **sdk-js** | npm (`littlehorse-client`)                                          | `sdk-js/` |
| **sdk-go** | Go module (`github.com/littlehorse-enterprises/littlehorse/sdk-go`) | `sdk-go/` |
| **sdk-dotnet** | NuGet (`LittleHorseSDK`)                                            | `sdk-dotnet/` |

## Versioning

The project follows [Semantic Versioning](https://semver.org) (`MAJOR.MINOR.PATCH`). Git tags use a `v` prefix (e.g. `v1.2.0`) because the Go module system requires it; the `v` is stripped from all other published artifact versions.

### Version File

The version is defined in a single place:

| File | Field | Example |
|---|---|---|
| `gradle.properties` | `version` | `1.1.0-SNAPSHOT` / `1.1.0` |

All other manifests (`sdk-python/pyproject.toml`, `sdk-js/package.json`, `sdk-dotnet/LittleHorse.Sdk/LittleHorse.Sdk.csproj`) have their versions set at publish time by the CI pipeline. The Go SDK version is derived from the Git tag automatically.

## Release Types

### 1. Snapshot Releases

Snapshots are development builds published from the `master` branch. They let consumers test unreleased features without waiting for a formal release.

- **Version format:** `X.Y.Z-SNAPSHOT` (e.g. `1.1.0-SNAPSHOT`).
- **Trigger:** Automated weekly via the [`weekly-snapshot`](../.github/workflows/weekly-snapshot.yml) GitHub Actions workflow (runs every Friday at midnight UTC), or on-demand(TODO).
- **Published to:**
  - **Maven:** Sonatype snapshot repository (`https://central.sonatype.com/repository/maven-snapshots/`).
  - **PyPI:** Not published (snapshots are Maven-only; Python users install from source).
  - **npm:** Not published (snapshot builds are Maven-only).
  - **Docker:** Optional — images can be tagged `X.Y.Z-SNAPSHOT` or `latest-snapshot`.
- **No Git tag is created.**

**Automated workflow ([`weekly-snapshot.yml`](../.github/workflows/weekly-snapshot.yml)):**

The GitHub Actions workflow handles the entire snapshot process automatically:

1. **Prepare** — Checks out `master`, reads the version from `gradle.properties`, and validates it ends with `-SNAPSHOT`.
2. **Test** — Runs `sdk-java`, `test-utils`, and `test-utils-container` test suites.
3. **Publish** — Publishes Java artifacts to the Sonatype snapshot repository (no staging/close step needed):
   ```bash
   ./gradlew sdk-java:publishToSonatype test-utils:publishToSonatype test-utils-container:publishToSonatype
   ```

**Manual snapshot (if needed):**

```bash
# Ensure gradle.properties has the -SNAPSHOT suffix, then:
./gradlew sdk-java:publishToSonatype test-utils:publishToSonatype test-utils-container:publishToSonatype
```

---

### 2. Release Candidates

Release candidates (RC) are pre-release versions intended for final validation before a stable release. They allow the team and early adopters to test the exact bits that will become the final release.

> **Important:** Release candidates can only be tagged on a **release branch** (e.g. `1.0`). They must never be tagged directly on `master`.

- **Version format:** `X.Y.Z-RC<N>` (e.g. `1.0.0-RC1`, `1.0.0-RC2`).
- **Git tag:** `vX.Y.Z-RC<N>` (e.g. `v1.0.0-RC1`).
- **Branch:** `X.Y` (e.g. `1.0`). The release branch is created from `master` when the first RC is cut.
- **Published to:** Same registries as stable releases but with the RC version qualifier.
  - Maven Central will treat `-RC1` as a regular (non-snapshot) release.
  - PyPI supports pre-release versions (`1.0.0rc1` per [PEP 440](https://peps.python.org/pep-0440/)).
  - npm supports pre-release tags (`1.0.0-RC1` published under the `next` dist-tag).
  - NuGet supports pre-release versions (`1.0.0-RC1`).
  - Docker images are tagged `X.Y.Z-RC<N>`.

**Workflow:**

1. **Create the release branch** (first RC only):
   ```bash
   git checkout master
   git pull
   git checkout -b X.Y
   git push -u origin X.Y
   ```
2. **Pre-release checks**
   - Ensure the `X.Y` branch is green (CI passes).
   - Confirm no open blockers for the target milestone.
3. **Version bump** — On the release branch, update the version in [`gradle.properties`](#version-file) to `X.Y.Z-RC<N>`.
4. **Tag & push**
   ```bash
   git tag vX.Y.Z-RC<N>
   git push origin vX.Y.Z-RC<N>
   ```
5. **Publish artifacts** (see [Publishing Details](#publishing-details)).
6. **Validation** — Stakeholders test the RC. If issues are found:
   - Fix them on `master` first.
   - Cherry-pick the fixes onto the `X.Y` branch using the [`cherry-pick`](../.github/workflows/cherry-pick.yml) workflow.
   - Cut `RC<N+1>` from the release branch.

---

### 3. Major Releases

A major release introduces breaking changes to the public API (protobuf schemas, SDK interfaces, server behavior).

- **Version format:** `X.0.0` where `X` is incremented (e.g. `1.0.0` → `2.0.0`).
- **Git tag:** `vX.0.0`.
- **Branch:** The `X.0` branch should already exist from the RC phase.

**Workflow:**

1. **Pre-release checks**
   - Ensure the `X.0` branch is green.
   - Verify the changelog is up-to-date (generated via [git-cliff](https://git-cliff.org/) using `cliff.toml`).
   - Confirm all breaking changes are documented and migration guides are ready.
   - One or more RCs should have been published and validated on the release branch.
2. **Version bump** — On the release branch, update the version in [`gradle.properties`](#version-file) to `X.0.0`.
3. **Tag & push**
   ```bash
   git tag vX.0.0
   git push origin vX.0.0
   ```
4. **Publish artifacts** (see [Publishing Details](#publishing-details)).
5. **Post-release**
   - On `master`, bump `gradle.properties` to the next `(X+1).0.0-SNAPSHOT`.
   - Generate and publish the changelog.
   - Announce the release.

---

### 4. Minor Releases

A minor release adds new functionality in a backward-compatible manner.

- **Version format:** `X.Y.0` where `Y` is incremented (e.g. `1.1.0` → `1.2.0`).
- **Git tag:** `vX.Y.0`.

**Workflow:**

1. **Pre-release checks**
   - Ensure `master` is green.
   - Verify the changelog is up-to-date.
   - Confirm no open blockers for the target milestone.
   - Optionally publish one or more RCs for validation.
2. **Version bump** — Update the version in [`gradle.properties`](#version-file) to `X.Y.0`.
3. **Tag & push**
   ```bash
   git tag vX.Y.0
   git push origin vX.Y.0
   ```
4. **Publish artifacts** (see [Publishing Details](#publishing-details)).
5. **Post-release**
   - On `master`, bump `gradle.properties` to the next `X.(Y+1).0-SNAPSHOT`.
   - Generate and publish the changelog.
   - Announce the release.

---

### 5. Patch Releases

A patch release contains only backward-compatible bug fixes. Patches are typically cherry-picked onto the corresponding `X.Y` branch.

- **Version format:** `X.Y.Z` where `Z` is incremented (e.g. `1.0.0` → `1.0.1`).
- **Git tag:** `vX.Y.Z`.
- **Branch:** `X.Y` (e.g. `1.0`). Must already exist from a previous RC/release cycle.

**Workflow:**

1. **Cherry-pick fixes** — Fix on `master` first, then cherry-pick onto the `X.Y` branch using the [`cherry-pick`](../.github/workflows/cherry-pick.yml) GitHub Actions workflow. It takes a commit ID and a release branch as inputs, validates the commit is on `master`, and applies it to the target branch.
2. **Pre-release checks**
   - Ensure the `X.Y` branch is green.
   - Verify each fix has a corresponding test.
3. **Version bump** — On the release branch, update the version in [`gradle.properties`](#version-file) to `X.Y.Z`.
4. **Tag & push**
   ```bash
   git tag vX.Y.Z
   git push origin vX.Y.Z
   ```
5. **Publish artifacts** (see [Publishing Details](#publishing-details)).
6. **Post-release**
   - Generate and publish the changelog.
   - Announce the release.

---

## Automation Goals

The [`scripts/`](scripts/) subdirectory contains utility Python scripts that are invoked by the GitHub Actions workflows under [`.github/workflows/`](../.github/workflows/) to automate the release process. They can also be run locally.

The scripts handle tasks such as:

- **Validation** — Pre-release checks: version consistency, branch constraints, clean Git tree, and snapshot format verification.
- **Version extraction** — Determining the current version from `gradle.properties` or Git tags.
- **Cherry-pick** — Safely cherry-picking a commit from `master` onto a release branch for patch releases.

### Running Locally

```bash
cd release-manager/scripts

python3 validate.py        --type minor --version 1.2.0
python3 extract_version.py
python3 cherry_pick.py     abc123def 1.0
```

### Running from GitHub Actions

```yaml
- name: Extract version
  run: |
    version=$(python3 release-manager/scripts/extract_version.py)
    echo "version=$version" >> $GITHUB_OUTPUT

- name: Validate
  run: python3 release-manager/scripts/validate.py --type snapshot --version ${{ env.VERSION }}
```


> **Status:** 🚧 Under development. Script implementations will be added incrementally.

## Prerequisites

- Python ≥ 3.10
- Git

