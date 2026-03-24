"""Shared constants and helpers for release-manager scripts."""

from pathlib import Path

# Repo root is two levels up from this file (release-manager/scripts/ -> repo root)
REPO_ROOT = Path(__file__).resolve().parent.parent.parent

# Version files relative to repo root
VERSION_FILES = {
    "gradle": REPO_ROOT / "gradle.properties",
    "python_project": REPO_ROOT / "sdk-python" / "pyproject.toml",
    "js": REPO_ROOT / "sdk-js" / "package.json",
    "dotnet": REPO_ROOT / "sdk-dotnet" / "LittleHorse.Sdk" / "LittleHorse.Sdk.csproj",
}

