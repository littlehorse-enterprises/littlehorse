"""Shared constants and helpers for release-manager scripts."""

from pathlib import Path

# Repo root is two levels up from this file (release-manager/scripts/ -> repo root)
REPO_ROOT = Path(__file__).resolve().parent.parent.parent

# Version file (single source of truth for the project version)
VERSION_FILE = REPO_ROOT / "gradle.properties"

