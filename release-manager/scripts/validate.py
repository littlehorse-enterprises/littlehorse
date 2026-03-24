"""Pre-release validation: version format, branch, clean tree, version file consistency."""

import argparse
import re
import subprocess
import sys

from config import REPO_ROOT, VERSION_FILES

RELEASE_TYPES = ["snapshot", "rc", "major", "minor", "patch"]

SNAPSHOT_RE = re.compile(r"^\d+\.\d+\.\d+-SNAPSHOT$")
RC_RE = re.compile(r"^\d+\.\d+\.\d+-RC\d+$")
STABLE_RE = re.compile(r"^\d+\.\d+\.\d+$")


def get_current_branch() -> str:
    """Return the current Git branch name."""
    result = subprocess.run(
        ["git", "rev-parse", "--abbrev-ref", "HEAD"],
        capture_output=True,
        text=True,
        cwd=REPO_ROOT,
    )
    return result.stdout.strip()


def parse_semver(version: str) -> tuple[int, int, int]:
    """Extract (major, minor, patch) from a version string, ignoring any suffix."""
    match = re.match(r"^(\d+)\.(\d+)\.(\d+)", version)
    if not match:
        raise ValueError(f"Cannot parse semver from '{version}'")
    return int(match.group(1)), int(match.group(2)), int(match.group(3))


def expected_branch(version: str) -> str:
    """Return the expected release branch name for a version (e.g. '1.0' for '1.0.0-RC1')."""
    major, minor, _ = parse_semver(version)
    return f"{major}.{minor}"


def check_git_clean() -> bool:
    """Verify the Git working tree is clean."""
    result = subprocess.run(
        ["git", "status", "--porcelain"],
        capture_output=True,
        text=True,
        cwd=REPO_ROOT,
    )
    if result.stdout.strip():
        print("ERROR: Git working tree is not clean:", file=sys.stderr)
        print(result.stdout, file=sys.stderr)
        return False
    return True


def check_version_files_exist() -> bool:
    """Verify all version files exist."""
    ok = True
    for name, path in VERSION_FILES.items():
        if not path.exists():
            print(f"ERROR: {name} file not found at {path}", file=sys.stderr)
            ok = False
    return ok


def check_branch_is(expected: str) -> bool:
    """Verify the current branch matches the expected name."""
    current = get_current_branch()
    if current != expected:
        print(
            f"ERROR: Expected branch '{expected}', but currently on '{current}'",
            file=sys.stderr,
        )
        return False
    return True


def check_branch_is_not(forbidden: str) -> bool:
    """Verify the current branch is NOT the given name."""
    current = get_current_branch()
    if current == forbidden:
        print(
            f"ERROR: This release type must not be run on '{forbidden}'",
            file=sys.stderr,
        )
        return False
    return True


def check_version_format(version: str, pattern: re.Pattern, description: str) -> bool:
    """Verify the version string matches the expected pattern."""
    if not pattern.match(version):
        print(
            f"ERROR: Version '{version}' does not match {description} format",
            file=sys.stderr,
        )
        return False
    return True


def check_gradle_snapshot() -> bool:
    """Verify gradle.properties version ends with -SNAPSHOT."""
    content = VERSION_FILES["gradle"].read_text()
    for line in content.splitlines():
        if line.startswith("version="):
            value = line.split("=", 1)[1].strip()
            if not value.endswith("-SNAPSHOT"):
                print(
                    f"ERROR: gradle.properties version is '{value}', expected -SNAPSHOT suffix",
                    file=sys.stderr,
                )
                return False
            return True
    print("ERROR: No 'version=' line found in gradle.properties", file=sys.stderr)
    return False


def validate_snapshot(version: str) -> bool:
    ok = True
    ok = check_version_format(version, SNAPSHOT_RE, "X.Y.Z-SNAPSHOT") and ok
    branch = get_current_branch()
    if branch != "main" and branch != expected_branch(version):
        print(
            f"ERROR: Snapshots must be on 'main' or '{expected_branch(version)}', "
            f"but currently on '{branch}'",
            file=sys.stderr,
        )
        ok = False
    ok = check_gradle_snapshot() and ok
    return ok


def validate_rc(version: str) -> bool:
    ok = True
    ok = check_version_format(version, RC_RE, "X.Y.Z-RC<N>") and ok
    ok = check_branch_is_not("main") and ok
    if ok:
        ok = check_branch_is(expected_branch(version)) and ok
    return ok


def validate_major(version: str) -> bool:
    ok = True
    ok = check_version_format(version, STABLE_RE, "X.Y.Z") and ok
    if ok:
        major, minor, patch = parse_semver(version)
        if minor != 0 or patch != 0:
            print(
                f"ERROR: Major release version must be X.0.0, got '{version}'",
                file=sys.stderr,
            )
            ok = False
    ok = check_branch_is_not("main") and ok
    if ok:
        ok = check_branch_is(expected_branch(version)) and ok
    return ok


def validate_minor(version: str) -> bool:
    ok = True
    ok = check_version_format(version, STABLE_RE, "X.Y.Z") and ok
    if ok:
        _, _, patch = parse_semver(version)
        if patch != 0:
            print(
                f"ERROR: Minor release version must be X.Y.0, got '{version}'",
                file=sys.stderr,
            )
            ok = False
    ok = check_branch_is("main") and ok
    return ok


def validate_patch(version: str) -> bool:
    ok = True
    ok = check_version_format(version, STABLE_RE, "X.Y.Z") and ok
    if ok:
        _, _, patch = parse_semver(version)
        if patch == 0:
            print(
                f"ERROR: Patch release version must have Z > 0, got '{version}'",
                file=sys.stderr,
            )
            ok = False
    ok = check_branch_is_not("main") and ok
    if ok:
        ok = check_branch_is(expected_branch(version)) and ok
    return ok


VALIDATORS = {
    "snapshot": validate_snapshot,
    "rc": validate_rc,
    "major": validate_major,
    "minor": validate_minor,
    "patch": validate_patch,
}


def validate(release_type: str, version: str) -> None:
    print(f"Validating {release_type} release: {version}")
    ok = True

    ok = check_version_files_exist() and ok
    ok = check_git_clean() and ok
    ok = VALIDATORS[release_type](version) and ok

    if ok:
        print("All checks passed.")
    else:
        print("Validation failed.", file=sys.stderr)
        sys.exit(1)


def main() -> None:
    parser = argparse.ArgumentParser(description="Run pre-release validation checks")
    parser.add_argument(
        "--type",
        required=True,
        choices=RELEASE_TYPES,
        help="Release type (snapshot, rc, major, minor, patch)",
    )
    parser.add_argument(
        "--version",
        required=True,
        help="Target version (e.g. 1.1.0-SNAPSHOT, 1.0.0-RC1, 1.1.0)",
    )
    args = parser.parse_args()
    validate(args.type, args.version)


if __name__ == "__main__":
    main()

