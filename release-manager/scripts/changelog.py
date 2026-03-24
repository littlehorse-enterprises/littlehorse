"""Generate changelog via git-cliff."""

import argparse
import subprocess
import sys

from config import REPO_ROOT


def generate_changelog(version: str) -> None:
    # TODO: implement changelog generation
    print(f"TODO: generate changelog for {version} using git-cliff at {REPO_ROOT}")


def main() -> None:
    parser = argparse.ArgumentParser(description="Generate changelog for a release")
    parser.add_argument("--version", required=True, help="Target version (e.g. 1.1.0)")
    args = parser.parse_args()
    generate_changelog(args.version)


if __name__ == "__main__":
    main()

