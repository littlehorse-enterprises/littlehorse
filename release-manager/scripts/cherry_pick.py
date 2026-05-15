"""Cherry-pick a commit from master onto a release branch."""

import argparse
import re
import subprocess
import sys

from config import REPO_ROOT

RELEASE_BRANCH_RE = re.compile(r"^\d+\.\d+$")


def run_git(*args: str) -> subprocess.CompletedProcess:
    return subprocess.run(
        ["git"] + list(args),
        capture_output=True,
        text=True,
        cwd=REPO_ROOT,
    )


def check_commit_on_master(commit: str) -> bool:
    """Verify the commit exists on the master branch."""
    result = run_git("merge-base", "--is-ancestor", commit, "master")
    if result.returncode != 0:
        print(
            f"ERROR: Commit '{commit}' is not an ancestor of master",
            file=sys.stderr,
        )
        return False
    return True


def check_release_branch_exists(branch: str) -> bool:
    """Verify the release branch exists in origin."""
    result = run_git("rev-parse", "--verify", f"origin/{branch}")
    if result.returncode != 0:
        print(f"ERROR: Release branch '{branch}' does not exist in origin", file=sys.stderr)
        return False
    return True


def check_release_branch_format(branch: str) -> bool:
    """Verify the branch name matches the X.Y release branch format."""
    if not RELEASE_BRANCH_RE.match(branch):
        print(
            f"ERROR: '{branch}' is not a valid release branch name (expected X.Y format)",
            file=sys.stderr,
        )
        return False
    return True


def cherry_pick(commit: str, branch: str) -> None:
    """Cherry-pick a commit onto the given release branch."""
    ok = True
    ok = check_release_branch_format(branch) and ok
    ok = check_commit_on_master(commit) and ok
    ok = check_release_branch_exists(branch) and ok

    if not ok:
        sys.exit(1)

    # Checkout the release branch
    result = run_git("checkout", branch)
    if result.returncode != 0:
        print(f"ERROR: Failed to checkout '{branch}': {result.stderr.strip()}", file=sys.stderr)
        sys.exit(1)

    # Cherry-pick the commit
    result = run_git("cherry-pick", commit)
    if result.returncode != 0:
        print(f"ERROR: Cherry-pick of '{commit}' onto '{branch}' failed due to conflicts", file=sys.stderr)
        sys.exit(1)

    short = run_git("rev-parse", "--short", commit).stdout.strip()
    print(f"Successfully cherry-picked {short} onto {branch}")


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Cherry-pick a commit from master onto a release branch",
    )
    parser.add_argument("commit", help="Commit ID to cherry-pick (must be on master)")
    parser.add_argument("branch", help="Target release branch (e.g. 1.0)")
    args = parser.parse_args()
    cherry_pick(args.commit, args.branch)


if __name__ == "__main__":
    main()

