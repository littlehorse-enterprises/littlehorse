"""Unit tests for validate.py."""

import subprocess
import unittest
from unittest.mock import MagicMock, patch

import validate


def mock_git_branch(branch: str):
    """Return a mock for subprocess.run that simulates `git rev-parse --abbrev-ref HEAD`."""
    result = MagicMock()
    result.stdout = branch + "\n"
    return result


def mock_git_clean():
    """Return a mock for subprocess.run that simulates a clean working tree."""
    result = MagicMock()
    result.stdout = ""
    return result


def mock_git_dirty():
    """Return a mock for subprocess.run that simulates a dirty working tree."""
    result = MagicMock()
    result.stdout = "M  some-file.txt\n"
    return result


def git_side_effect(branch: str, clean: bool):
    """Return a side_effect function that handles both git status and git rev-parse calls."""

    def side_effect(cmd, **kwargs):
        if "status" in cmd:
            return mock_git_clean() if clean else mock_git_dirty()
        if "rev-parse" in cmd:
            return mock_git_branch(branch)
        return MagicMock()

    return side_effect


GRADLE_SNAPSHOT = "version=1.1.0-SNAPSHOT\ngroup=io.littlehorse\n"
GRADLE_STABLE = "version=1.1.0\ngroup=io.littlehorse\n"


class TestParseHelpers(unittest.TestCase):
    def test_parse_semver_stable(self):
        self.assertEqual(validate.parse_semver("1.2.3"), (1, 2, 3))

    def test_parse_semver_snapshot(self):
        self.assertEqual(validate.parse_semver("1.1.0-SNAPSHOT"), (1, 1, 0))

    def test_parse_semver_rc(self):
        self.assertEqual(validate.parse_semver("2.0.0-RC3"), (2, 0, 0))

    def test_parse_semver_invalid(self):
        with self.assertRaises(ValueError):
            validate.parse_semver("not-a-version")

    def test_expected_branch(self):
        self.assertEqual(validate.expected_branch("1.0.0-RC1"), "1.0")
        self.assertEqual(validate.expected_branch("2.3.1"), "2.3")
        self.assertEqual(validate.expected_branch("0.12.0-SNAPSHOT"), "0.12")


class TestVersionFormat(unittest.TestCase):
    def test_snapshot_format_valid(self):
        self.assertTrue(validate.check_version_format("1.1.0-SNAPSHOT", validate.SNAPSHOT_RE, ""))

    def test_snapshot_format_rejects_stable(self):
        self.assertFalse(validate.check_version_format("1.1.0", validate.SNAPSHOT_RE, ""))

    def test_snapshot_format_rejects_rc(self):
        self.assertFalse(validate.check_version_format("1.1.0-RC1", validate.SNAPSHOT_RE, ""))

    def test_rc_format_valid(self):
        self.assertTrue(validate.check_version_format("1.0.0-RC1", validate.RC_RE, ""))
        self.assertTrue(validate.check_version_format("2.3.0-RC12", validate.RC_RE, ""))

    def test_rc_format_rejects_stable(self):
        self.assertFalse(validate.check_version_format("1.0.0", validate.RC_RE, ""))

    def test_rc_format_rejects_snapshot(self):
        self.assertFalse(validate.check_version_format("1.0.0-SNAPSHOT", validate.RC_RE, ""))

    def test_stable_format_valid(self):
        self.assertTrue(validate.check_version_format("1.0.0", validate.STABLE_RE, ""))
        self.assertTrue(validate.check_version_format("10.20.30", validate.STABLE_RE, ""))

    def test_stable_format_rejects_snapshot(self):
        self.assertFalse(validate.check_version_format("1.0.0-SNAPSHOT", validate.STABLE_RE, ""))

    def test_stable_format_rejects_rc(self):
        self.assertFalse(validate.check_version_format("1.0.0-RC1", validate.STABLE_RE, ""))


class TestCheckBranch(unittest.TestCase):
    @patch("validate.subprocess.run")
    def test_branch_is_match(self, mock_run):
        mock_run.return_value = mock_git_branch("master")
        self.assertTrue(validate.check_branch_is("master"))

    @patch("validate.subprocess.run")
    def test_branch_is_mismatch(self, mock_run):
        mock_run.return_value = mock_git_branch("1.0")
        self.assertFalse(validate.check_branch_is("master"))

    @patch("validate.subprocess.run")
    def test_branch_is_not_passes(self, mock_run):
        mock_run.return_value = mock_git_branch("1.0")
        self.assertTrue(validate.check_branch_is_not("master"))

    @patch("validate.subprocess.run")
    def test_branch_is_not_fails(self, mock_run):
        mock_run.return_value = mock_git_branch("master")
        self.assertFalse(validate.check_branch_is_not("master"))


class TestCheckGitClean(unittest.TestCase):
    @patch("validate.subprocess.run")
    def test_clean_tree(self, mock_run):
        mock_run.return_value = mock_git_clean()
        self.assertTrue(validate.check_git_clean())

    @patch("validate.subprocess.run")
    def test_dirty_tree(self, mock_run):
        mock_run.return_value = mock_git_dirty()
        self.assertFalse(validate.check_git_clean())


class TestCheckVersionFileExists(unittest.TestCase):
    @patch("validate.VERSION_FILE")
    def test_file_exists(self, mock_file):
        mock_file.exists.return_value = True
        self.assertTrue(validate.check_version_file_exists())

    @patch("validate.VERSION_FILE")
    def test_file_missing(self, mock_file):
        mock_file.exists.return_value = False
        self.assertFalse(validate.check_version_file_exists())


class TestCheckGradleSnapshot(unittest.TestCase):
    @patch("validate.VERSION_FILE")
    def test_has_snapshot_suffix(self, mock_file):
        mock_file.read_text.return_value = GRADLE_SNAPSHOT
        self.assertTrue(validate.check_gradle_snapshot())

    @patch("validate.VERSION_FILE")
    def test_missing_snapshot_suffix(self, mock_file):
        mock_file.read_text.return_value = GRADLE_STABLE
        self.assertFalse(validate.check_gradle_snapshot())

    @patch("validate.VERSION_FILE")
    def test_no_version_line(self, mock_file):
        mock_file.read_text.return_value = "group=io.littlehorse\n"
        self.assertFalse(validate.check_gradle_snapshot())


class TestValidateSnapshot(unittest.TestCase):
    @patch("validate.check_gradle_snapshot", return_value=True)
    @patch("validate.subprocess.run")
    def test_valid_snapshot_on_main(self, mock_run, _mock_gradle):
        mock_run.side_effect = git_side_effect("master", clean=True)
        self.assertTrue(validate.validate_snapshot("1.1.0-SNAPSHOT"))

    @patch("validate.check_gradle_snapshot", return_value=True)
    @patch("validate.subprocess.run")
    def test_valid_snapshot_on_release_branch(self, mock_run, _mock_gradle):
        mock_run.side_effect = git_side_effect("2.0", clean=True)
        self.assertTrue(validate.validate_snapshot("2.0.0-SNAPSHOT"))

    @patch("validate.check_gradle_snapshot", return_value=True)
    @patch("validate.subprocess.run")
    def test_snapshot_wrong_branch(self, mock_run, _mock_gradle):
        mock_run.side_effect = git_side_effect("2.0", clean=True)
        self.assertFalse(validate.validate_snapshot("1.1.0-SNAPSHOT"))

    @patch("validate.check_gradle_snapshot", return_value=True)
    @patch("validate.subprocess.run")
    def test_snapshot_bad_format(self, mock_run, _mock_gradle):
        mock_run.side_effect = git_side_effect("master", clean=True)
        self.assertFalse(validate.validate_snapshot("1.1.0"))


class TestValidateRC(unittest.TestCase):
    @patch("validate.subprocess.run")
    def test_valid_rc_on_release_branch(self, mock_run):
        mock_run.side_effect = git_side_effect("1.0", clean=True)
        self.assertTrue(validate.validate_rc("1.0.0-RC1"))

    @patch("validate.subprocess.run")
    def test_rc_on_main_rejected(self, mock_run):
        mock_run.side_effect = git_side_effect("master", clean=True)
        self.assertFalse(validate.validate_rc("1.0.0-RC1"))

    @patch("validate.subprocess.run")
    def test_rc_on_wrong_release_branch(self, mock_run):
        mock_run.side_effect = git_side_effect("2.0", clean=True)
        self.assertFalse(validate.validate_rc("1.0.0-RC1"))

    @patch("validate.subprocess.run")
    def test_rc_bad_format(self, mock_run):
        mock_run.side_effect = git_side_effect("1.0", clean=True)
        self.assertFalse(validate.validate_rc("1.0.0"))


class TestValidateMajor(unittest.TestCase):
    @patch("validate.subprocess.run")
    def test_valid_major_on_release_branch(self, mock_run):
        mock_run.side_effect = git_side_effect("2.0", clean=True)
        self.assertTrue(validate.validate_major("2.0.0"))

    @patch("validate.subprocess.run")
    def test_major_on_main_rejected(self, mock_run):
        mock_run.side_effect = git_side_effect("master", clean=True)
        self.assertFalse(validate.validate_major("2.0.0"))

    @patch("validate.subprocess.run")
    def test_major_with_nonzero_minor(self, mock_run):
        mock_run.side_effect = git_side_effect("2.1", clean=True)
        self.assertFalse(validate.validate_major("2.1.0"))

    @patch("validate.subprocess.run")
    def test_major_with_nonzero_patch(self, mock_run):
        mock_run.side_effect = git_side_effect("2.0", clean=True)
        self.assertFalse(validate.validate_major("2.0.1"))


class TestValidateMinor(unittest.TestCase):
    @patch("validate.subprocess.run")
    def test_valid_minor_on_main(self, mock_run):
        mock_run.side_effect = git_side_effect("master", clean=True)
        self.assertTrue(validate.validate_minor("1.2.0"))

    @patch("validate.subprocess.run")
    def test_minor_wrong_branch(self, mock_run):
        mock_run.side_effect = git_side_effect("1.2", clean=True)
        self.assertFalse(validate.validate_minor("1.2.0"))

    @patch("validate.subprocess.run")
    def test_minor_with_nonzero_patch(self, mock_run):
        mock_run.side_effect = git_side_effect("master", clean=True)
        self.assertFalse(validate.validate_minor("1.2.3"))


class TestValidatePatch(unittest.TestCase):
    @patch("validate.subprocess.run")
    def test_valid_patch_on_release_branch(self, mock_run):
        mock_run.side_effect = git_side_effect("1.0", clean=True)
        self.assertTrue(validate.validate_patch("1.0.1"))

    @patch("validate.subprocess.run")
    def test_patch_on_main_rejected(self, mock_run):
        mock_run.side_effect = git_side_effect("master", clean=True)
        self.assertFalse(validate.validate_patch("1.0.1"))

    @patch("validate.subprocess.run")
    def test_patch_with_zero_patch(self, mock_run):
        mock_run.side_effect = git_side_effect("1.0", clean=True)
        self.assertFalse(validate.validate_patch("1.0.0"))

    @patch("validate.subprocess.run")
    def test_patch_on_wrong_branch(self, mock_run):
        mock_run.side_effect = git_side_effect("2.0", clean=True)
        self.assertFalse(validate.validate_patch("1.0.1"))


if __name__ == "__main__":
    unittest.main()

