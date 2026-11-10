#!/usr/bin/env python3
"""Package both crates and check the packaged SDK against the packaged macro dependency.

A local Cargo patch models publishing the derive crate first. It changes only the
verification command, never the manifests shipped to consumers. Nothing is uploaded.
"""
import argparse
import os
import shutil
from pathlib import Path
import subprocess
import tarfile
import tempfile
import tomllib

ROOT = Path(__file__).resolve().parents[1]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--allow-dirty", action="store_true")
    arguments = parser.parse_args()
    version = tomllib.loads((ROOT / "Cargo.toml").read_text())["package"]["version"]
    flags = ["--allow-dirty"] if arguments.allow_dirty else []
    with tempfile.TemporaryDirectory(prefix="littlehorse-package-") as directory:
        stage = Path(directory)
        workspace = stage / "workspace"
        shutil.copytree(ROOT, workspace, ignore=shutil.ignore_patterns(".git", "target", "upstream", "__pycache__"))
        package_environment = dict(os.environ, CARGO_TARGET_DIR=str(workspace / "target"))
        package_environment.pop("LITTLEHORSE_SCHEMA_DIR", None)
        for package in ["littlehorse-derive", "littlehorse"]:
            command = ["cargo", "package", "-p", package, "--no-verify", *flags]
            if package == "littlehorse":
                command += ["--config", f'patch.crates-io.littlehorse-derive.path="{(stage / f"littlehorse-derive-{version}").as_posix()}"']
            subprocess.run(command, cwd=workspace, env=package_environment, check=True)
            archive = workspace / "target/package" / f"{package}-{version}.crate"
            artifacts = ROOT / "target/package"
            artifacts.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(archive, artifacts / archive.name)
            with tarfile.open(archive) as crate:
                crate.extractall(stage, filter="data")
        sdk = stage / f"littlehorse-{version}"
        patch = f'patch.crates-io.littlehorse-derive.path="{(stage / f"littlehorse-derive-{version}").as_posix()}"'
        environment = dict(os.environ, CARGO_TARGET_DIR=str(ROOT / "target"))
        environment.pop("LITTLEHORSE_SCHEMA_DIR", None)
        subprocess.run(["cargo", "check", "--manifest-path", str(sdk / "Cargo.toml"), "--lib", "--locked", "--config", patch], env=environment, check=True)
    print("Both crate archives package correctly; the packaged SDK compiles with the packaged derive crate. No publication performed.")


if __name__ == "__main__":
    main()
