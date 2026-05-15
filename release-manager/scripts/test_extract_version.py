"""Unit tests for extract_version.py."""

import unittest
from unittest.mock import MagicMock, patch

import extract_version


def mock_git_result(stdout: str):
    result = MagicMock()
    result.stdout = stdout
    return result


GRADLE_SNAPSHOT = "version=1.1.0-SNAPSHOT\ngroup=io.littlehorse\n"
GRADLE_STABLE = "version=1.1.0\ngroup=io.littlehorse\n"


class TestGetTagAtHead(unittest.TestCase):
    @patch("extract_version.subprocess.run")
    def test_no_tags(self, mock_run):
        mock_run.return_value = mock_git_result("")
        self.assertIsNone(extract_version.get_tag_at_head())

    @patch("extract_version.subprocess.run")
    def test_version_tag(self, mock_run):
        mock_run.return_value = mock_git_result("v1.0.0\n")
        self.assertEqual(extract_version.get_tag_at_head(), "v1.0.0")

    @patch("extract_version.subprocess.run")
    def test_rc_tag(self, mock_run):
        mock_run.return_value = mock_git_result("v1.0.0-RC1\n")
        self.assertEqual(extract_version.get_tag_at_head(), "v1.0.0-RC1")

    @patch("extract_version.subprocess.run")
    def test_non_version_tag_ignored(self, mock_run):
        mock_run.return_value = mock_git_result("some-random-tag\n")
        self.assertIsNone(extract_version.get_tag_at_head())

    @patch("extract_version.subprocess.run")
    def test_multiple_tags_picks_version(self, mock_run):
        mock_run.return_value = mock_git_result("some-tag\nv2.0.0\n")
        self.assertEqual(extract_version.get_tag_at_head(), "v2.0.0")


class TestStripVPrefix(unittest.TestCase):
    def test_with_prefix(self):
        self.assertEqual(extract_version.strip_v_prefix("v1.0.0"), "1.0.0")

    def test_without_prefix(self):
        self.assertEqual(extract_version.strip_v_prefix("1.0.0"), "1.0.0")

    def test_rc_tag(self):
        self.assertEqual(extract_version.strip_v_prefix("v1.0.0-RC1"), "1.0.0-RC1")


class TestExtractVersion(unittest.TestCase):
    @patch("extract_version.get_gradle_version", return_value="1.1.0-SNAPSHOT")
    @patch("extract_version.get_current_branch", return_value="master")
    def test_main_returns_gradle_version(self, _branch, _gradle):
        self.assertEqual(extract_version.extract_version(), "1.1.0-SNAPSHOT")

    @patch("extract_version.get_gradle_version")
    @patch("extract_version.get_tag_at_head", return_value="v1.0.0")
    @patch("extract_version.get_current_branch", return_value="1.0")
    def test_release_branch_with_tag(self, _branch, _tag, mock_gradle):
        self.assertEqual(extract_version.extract_version(), "1.0.0")
        mock_gradle.assert_not_called()

    @patch("extract_version.get_gradle_version", return_value="1.0.1-SNAPSHOT")
    @patch("extract_version.get_tag_at_head", return_value=None)
    @patch("extract_version.get_current_branch", return_value="1.0")
    def test_release_branch_without_tag(self, _branch, _tag, _gradle):
        self.assertEqual(extract_version.extract_version(), "1.0.1-SNAPSHOT")

    @patch("extract_version.get_gradle_version")
    @patch("extract_version.get_tag_at_head", return_value="v2.0.0-RC1")
    @patch("extract_version.get_current_branch", return_value="2.0")
    def test_release_branch_with_rc_tag(self, _branch, _tag, mock_gradle):
        self.assertEqual(extract_version.extract_version(), "2.0.0-RC1")
        mock_gradle.assert_not_called()


class TestGetGradleVersion(unittest.TestCase):
    @patch("extract_version.VERSION_FILE")
    def test_reads_version(self, mock_file):
        mock_file.read_text.return_value = GRADLE_SNAPSHOT
        self.assertEqual(extract_version.get_gradle_version(), "1.1.0-SNAPSHOT")

    @patch("extract_version.VERSION_FILE")
    def test_reads_stable_version(self, mock_file):
        mock_file.read_text.return_value = GRADLE_STABLE
        self.assertEqual(extract_version.get_gradle_version(), "1.1.0")

    @patch("extract_version.VERSION_FILE")
    def test_missing_version_exits(self, mock_file):
        mock_file.read_text.return_value = "group=io.littlehorse\n"
        with self.assertRaises(SystemExit):
            extract_version.get_gradle_version()


if __name__ == "__main__":
    unittest.main()

