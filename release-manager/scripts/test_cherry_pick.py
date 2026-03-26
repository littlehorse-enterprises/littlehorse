"""Unit tests for cherry_pick.py."""

import unittest
from unittest.mock import MagicMock, patch, call

import cherry_pick


def mock_result(returncode: int = 0, stdout: str = "", stderr: str = ""):
    result = MagicMock()
    result.returncode = returncode
    result.stdout = stdout
    result.stderr = stderr
    return result


class TestCheckReleaseBranchFormat(unittest.TestCase):
    def test_valid_format(self):
        self.assertTrue(cherry_pick.check_release_branch_format("1.0"))
        self.assertTrue(cherry_pick.check_release_branch_format("12.34"))

    def test_rejects_master(self):
        self.assertFalse(cherry_pick.check_release_branch_format("master"))

    def test_rejects_three_parts(self):
        self.assertFalse(cherry_pick.check_release_branch_format("1.0.0"))

    def test_rejects_prefix(self):
        self.assertFalse(cherry_pick.check_release_branch_format("release/1.0"))


class TestCheckCommitOnMaster(unittest.TestCase):
    @patch("cherry_pick.run_git")
    def test_commit_is_ancestor(self, mock_git):
        mock_git.return_value = mock_result(returncode=0)
        self.assertTrue(cherry_pick.check_commit_on_master("abc123"))
        mock_git.assert_called_once_with("merge-base", "--is-ancestor", "abc123", "master")

    @patch("cherry_pick.run_git")
    def test_commit_not_ancestor(self, mock_git):
        mock_git.return_value = mock_result(returncode=1)
        self.assertFalse(cherry_pick.check_commit_on_master("abc123"))


class TestCheckReleaseBranchExists(unittest.TestCase):
    @patch("cherry_pick.run_git")
    def test_remote_branch_exists(self, mock_git):
        mock_git.return_value = mock_result(returncode=0)
        self.assertTrue(cherry_pick.check_release_branch_exists("1.0"))
        mock_git.assert_called_once_with("rev-parse", "--verify", "origin/1.0")

    @patch("cherry_pick.run_git")
    def test_branch_not_found(self, mock_git):
        mock_git.return_value = mock_result(returncode=1)
        self.assertFalse(cherry_pick.check_release_branch_exists("9.9"))


class TestCherryPick(unittest.TestCase):
    @patch("cherry_pick.run_git")
    @patch("cherry_pick.check_release_branch_exists", return_value=True)
    @patch("cherry_pick.check_commit_on_master", return_value=True)
    @patch("cherry_pick.check_release_branch_format", return_value=True)
    def test_successful_cherry_pick(self, _fmt, _commit, _exists, mock_git):
        mock_git.side_effect = [
            mock_result(returncode=0),               # checkout
            mock_result(returncode=0),               # cherry-pick
            mock_result(returncode=0, stdout="abc1"),  # rev-parse --short
        ]
        cherry_pick.cherry_pick("abc123def", "1.0")
        mock_git.assert_any_call("checkout", "1.0")
        mock_git.assert_any_call("cherry-pick", "abc123def")

    @patch("cherry_pick.run_git")
    @patch("cherry_pick.check_release_branch_exists", return_value=True)
    @patch("cherry_pick.check_commit_on_master", return_value=False)
    @patch("cherry_pick.check_release_branch_format", return_value=True)
    def test_commit_not_on_master_exits(self, _fmt, _commit, _exists, mock_git):
        with self.assertRaises(SystemExit):
            cherry_pick.cherry_pick("bad123", "1.0")

    @patch("cherry_pick.run_git")
    @patch("cherry_pick.check_release_branch_exists", return_value=True)
    @patch("cherry_pick.check_commit_on_master", return_value=True)
    @patch("cherry_pick.check_release_branch_format", return_value=True)
    def test_checkout_failure_exits(self, _fmt, _commit, _exists, mock_git):
        mock_git.return_value = mock_result(returncode=1, stderr="error: pathspec")
        with self.assertRaises(SystemExit):
            cherry_pick.cherry_pick("abc123", "1.0")

    @patch("cherry_pick.run_git")
    @patch("cherry_pick.check_release_branch_exists", return_value=True)
    @patch("cherry_pick.check_commit_on_master", return_value=True)
    @patch("cherry_pick.check_release_branch_format", return_value=True)
    def test_cherry_pick_conflict_exits(self, _fmt, _commit, _exists, mock_git):
        mock_git.side_effect = [
            mock_result(returncode=0),              # checkout
            mock_result(returncode=1, stderr=""),    # cherry-pick
        ]
        with self.assertRaises(SystemExit):
            cherry_pick.cherry_pick("abc123", "1.0")

    @patch("cherry_pick.check_release_branch_exists", return_value=False)
    @patch("cherry_pick.check_commit_on_master", return_value=True)
    @patch("cherry_pick.check_release_branch_format", return_value=True)
    def test_branch_not_found_exits(self, _fmt, _commit, _exists):
        with self.assertRaises(SystemExit):
            cherry_pick.cherry_pick("abc123", "9.9")

    @patch("cherry_pick.check_release_branch_exists", return_value=True)
    @patch("cherry_pick.check_commit_on_master", return_value=True)
    def test_bad_branch_format_exits(self, _commit, _exists):
        with self.assertRaises(SystemExit):
            cherry_pick.cherry_pick("abc123", "release/1.0")


if __name__ == "__main__":
    unittest.main()

