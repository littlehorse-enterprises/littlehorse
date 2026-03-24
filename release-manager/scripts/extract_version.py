"""Extract the current version based on branch and Git tag context."""

import argparse
import re
import subprocess
import sys
from typing import Optional

from config import REPO_ROOT, VERSION_FILE


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


def extract_version() -> str:
    """Determine the current version based on branch and tag context."""
    branch = get_current_branch()

    if branch == "master":
        return get_gradle_version()

    # On a release branch: use the tag if HEAD is tagged, otherwise gradle.properties
    tag = get_tag_at_head()
    if tag is not None:
        return strip_v_prefix(tag)

    return get_gradle_version()


def main() -> None:
    parser = argparse.ArgumentParser(description="Extract the current version")
    parser.parse_args()
    version = extract_version()
    print(version)


if __name__ == "__main__":
    main()

