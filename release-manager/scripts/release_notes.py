"""Resolve the release notes file for a given version.

Release notes live in the `release_notes/` directory at the repo root and are
named after the version they document:

    release_notes/1.1.2.md   -> patch release 1.1.2
    release_notes/1.2.md     -> the 1.2 minor release (i.e. 1.2.0)

Pre-release qualifiers (e.g. `-RC1`) are stripped, so `1.2.0-RC1` resolves to
the same notes as `1.2.0`.
"""

import argparse
import re
import shutil
import sys
from pathlib import Path
from typing import List, Optional

from config import RELEASE_NOTES_DIR

VERSION_PATTERN = re.compile(r"^v?(\d+)\.(\d+)\.(\d+)(?:[-+].*)?$")


def candidate_files(version: str) -> List[Path]:
    """Return the candidate release notes files for a version, most specific first."""
    match = VERSION_PATTERN.match(version)
    if match is None:
        print(f"ERROR: Invalid version '{version}', expected MAJOR.MINOR.PATCH", file=sys.stderr)
        sys.exit(1)

    major, minor, patch = match.groups()
    candidates = [RELEASE_NOTES_DIR / f"{major}.{minor}.{patch}.md"]
    if patch == "0":
        candidates.append(RELEASE_NOTES_DIR / f"{major}.{minor}.md")
    return candidates


def resolve(version: str) -> Optional[Path]:
    """Return the release notes file for a version, or None if there is none."""
    for candidate in candidate_files(version):
        if candidate.is_file():
            return candidate
    return None


def main() -> None:
    parser = argparse.ArgumentParser(description="Resolve the release notes file for a version")
    parser.add_argument("--version", required=True, help="Version to resolve (e.g. 1.2.0 or v1.2.0-RC1)")
    parser.add_argument("--output", help="Copy the release notes to this path instead of printing the path")
    args = parser.parse_args()

    notes = resolve(args.version)
    if notes is None:
        expected = ", ".join(str(c) for c in candidate_files(args.version))
        print(f"ERROR: No release notes found for {args.version}. Expected one of: {expected}", file=sys.stderr)
        sys.exit(1)

    if args.output:
        shutil.copyfile(notes, args.output)
        print(f"Copied release notes from {notes} to {args.output}", file=sys.stderr)
    else:
        print(notes)


if __name__ == "__main__":
    main()

