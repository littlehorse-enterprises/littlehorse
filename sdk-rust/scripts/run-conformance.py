#!/usr/bin/env python3
"""Run the upstream grader in an isolated workspace; never edit the upstream checkout."""
import argparse
import json
from pathlib import Path
import shutil
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--upstream", type=Path, default=ROOT.parent, help="LittleHorse checkout containing sdk-conformance")
    parser.add_argument("--binary", type=Path, default=ROOT / "target/debug/lh-conformance")
    parser.add_argument("--reference-java", action="store_true", help="also grade the built upstream Java testee and cross-compare seeded workflows")
    parser.add_argument("--report", type=Path, default=ROOT / "target/conformance-results.json")
    arguments = parser.parse_args()
    source = arguments.upstream / "sdk-conformance" if arguments.upstream else ROOT / "conformance"
    binary = arguments.binary.resolve()
    if not binary.is_file():
        parser.error("build lh-conformance first with cargo build --features conformance --bin lh-conformance")
    with tempfile.TemporaryDirectory(prefix="littlehorse-conformance-") as directory:
        workspace = Path(directory)
        suite = workspace / "sdk-conformance"
        suite.mkdir()
        shutil.copytree(source / "areas", suite / "areas")
        runner_source = source / "runner" if arguments.upstream else source
        shutil.copytree(runner_source, suite / "runner", ignore=shutil.ignore_patterns("areas"))
        (suite / "results").mkdir()
        (suite / "ledgers").mkdir()
        (suite / "ledgers/rust.yaml").write_text("todo: []\nnot_applicable: []\n")
        testees = {"rust": {"command": [str(binary)]}}
        if arguments.reference_java:
            if not arguments.upstream:
                parser.error("--reference-java requires --upstream")
            java = arguments.upstream.resolve() / "sdk-java/conformance/build/install/sdk-java-conformance/bin/sdk-java-conformance"
            if not java.is_file():
                parser.error("build the upstream Java conformance testee first")
            testees["java"] = {"command": [str(java)]}
            (suite / "ledgers/java.yaml").write_text("todo: []\nnot_applicable: []\n")
        (suite / "testees.json").write_text(json.dumps(testees))
        subprocess.run(["git", "init", "-q", str(workspace)], check=True)
        subprocess.run(["git", "-C", str(workspace), "add", "sdk-conformance/areas"], check=True)
        # The grader requires HEAD's corpus tree hash. A temporary commit creates that
        # tree without touching a user repository or publishing any Git history.
        subprocess.run(["git", "-C", str(workspace), "-c", "user.name=Conformance fixture", "-c", "user.email=fixture@localhost", "commit", "-qm", "Conformance corpus"], check=True)
        result = subprocess.run(["node", str(suite / "runner/run.mjs")], cwd=workspace)
        if result.returncode == 0 and arguments.reference_java:
            result = subprocess.run(["node", str(suite / "runner/fuzz.mjs"), "50", "24"], cwd=workspace)
        report = suite / "results/rust.json"
        if report.exists():
            arguments.report.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(report, arguments.report)
        raise SystemExit(result.returncode)


if __name__ == "__main__":
    main()
