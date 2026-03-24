"""Bump version strings across all version files in the monorepo."""

import argparse
import sys

from config import VERSION_FILES


def bump_version(version: str) -> None:
    # TODO: implement version bumping for each file
    for name, path in VERSION_FILES.items():
        if not path.exists():
            print(f"WARNING: {name} file not found at {path}", file=sys.stderr)
            continue
        print(f"TODO: bump {name} -> {version} ({path})")


def main() -> None:
    parser = argparse.ArgumentParser(description="Bump version across all version files")
    parser.add_argument("--version", required=True, help="Target version (e.g. 1.1.0, 1.1.0-RC1)")
    args = parser.parse_args()
    bump_version(args.version)


if __name__ == "__main__":
    main()

