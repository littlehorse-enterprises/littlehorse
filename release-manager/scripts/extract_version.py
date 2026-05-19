"""Extract the current version based on branch and Git tag context."""

import argparse
import re
import subprocess
import sys
from typing import Optional

from config import MAIN_BRANCH, REPO_ROOT, VERSION_FILE


def get_current_branch() -> str:
    """Return the current Git branch name."""
    result = subprocess.run(
        ["git", "rev-parse", "--abbrev-ref", "HEAD"],
        capture_output=True,
        text=True,
        cwd=REPO_ROOT,
    )
    return result.stdout.strip()


def get_tag_at_head() -> Optional[str]:
    """Return the tag pointing at HEAD, or None if there is no tag."""
    result = subprocess.run(
        ["git", "tag", "--points-at", "HEAD"],
        capture_output=True,
        text=True,
        cwd=REPO_ROOT,
    )
    tags = result.stdout.strip().splitlines()
    # Return the first version-like tag (vX.Y.Z...)
    for tag in tags:
        if re.match(r"^v\d+\.\d+\.\d+", tag):
            return tag
    return None


def get_gradle_version() -> str:
    """Read the version from gradle.properties."""
    content = VERSION_FILE.read_text()
    for line in content.splitlines():
        if line.startswith("version="):
            return line.split("=", 1)[1].strip()
    print("ERROR: No 'version=' line found in gradle.properties", file=sys.stderr)
    sys.exit(1)


def strip_v_prefix(tag: str) -> str:
    """Remove the leading 'v' from a Git tag."""
    return tag[1:] if tag.startswith("v") else tag


def strip_patch_from_snapshot(version: str) -> str:
    """Convert X.Y.Z-SNAPSHOT to X.Y-SNAPSHOT."""
    match = re.match(r"^(\d+\.\d+)\.\d+-SNAPSHOT$", version)
    if match:
        return f"{match.group(1)}-SNAPSHOT"
    return version


def extract_version(snapshot: bool = False) -> str:
    """Determine the current version based on branch and tag context."""
    branch = get_current_branch()

    if branch == MAIN_BRANCH:
        version = get_gradle_version()
        return strip_patch_from_snapshot(version) if snapshot else version

    # On a release branch: use the tag if HEAD is tagged, otherwise gradle.properties
    tag = get_tag_at_head()
    if tag is not None:
        return strip_v_prefix(tag)

    version = get_gradle_version()
    return strip_patch_from_snapshot(version) if snapshot else version


def main() -> None:
    parser = argparse.ArgumentParser(description="Extract the current version")
    parser.add_argument(
        "--snapshot",
        action="store_true",
        help="Strip the patch version for snapshot publishing (e.g. 1.2.0-SNAPSHOT -> 1.2-SNAPSHOT)",
    )
    args = parser.parse_args()
    version = extract_version(snapshot=args.snapshot)
    print(version)


if __name__ == "__main__":
    main()
