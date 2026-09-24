"""Unit tests for release_notes.py."""

import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

import release_notes


class ReleaseNotesTestCase(unittest.TestCase):
    def setUp(self):
        self._tmp = tempfile.TemporaryDirectory()
        self.notes_dir = Path(self._tmp.name)
        patcher = patch.object(release_notes, "RELEASE_NOTES_DIR", self.notes_dir)
        patcher.start()
        self.addCleanup(patcher.stop)
        self.addCleanup(self._tmp.cleanup)

    def write(self, name: str, content: str = "notes") -> Path:
        path = self.notes_dir / name
        path.write_text(content)
        return path


class TestCandidateFiles(ReleaseNotesTestCase):
    def test_patch_release_has_single_candidate(self):
        candidates = release_notes.candidate_files("1.1.2")
        self.assertEqual(candidates, [self.notes_dir / "1.1.2.md"])

    def test_minor_release_falls_back_to_short_name(self):
        candidates = release_notes.candidate_files("1.2.0")
        self.assertEqual(candidates, [self.notes_dir / "1.2.0.md", self.notes_dir / "1.2.md"])

    def test_strips_v_prefix_and_rc_suffix(self):
        candidates = release_notes.candidate_files("v1.2.0-RC1")
        self.assertEqual(candidates, [self.notes_dir / "1.2.0.md", self.notes_dir / "1.2.md"])

    def test_invalid_version_exits(self):
        with self.assertRaises(SystemExit):
            release_notes.candidate_files("not-a-version")


class TestResolve(ReleaseNotesTestCase):
    def test_resolves_patch_notes(self):
        expected = self.write("1.1.2.md")
        self.assertEqual(release_notes.resolve("1.1.2"), expected)

    def test_prefers_exact_over_short_name(self):
        expected = self.write("1.2.0.md")
        self.write("1.2.md")
        self.assertEqual(release_notes.resolve("1.2.0"), expected)

    def test_falls_back_to_short_name(self):
        expected = self.write("1.2.md")
        self.assertEqual(release_notes.resolve("1.2.0"), expected)

    def test_rc_resolves_to_final_notes(self):
        expected = self.write("1.2.md")
        self.assertEqual(release_notes.resolve("v1.2.0-RC1"), expected)

    def test_missing_notes_returns_none(self):
        self.assertIsNone(release_notes.resolve("9.9.9"))

    def test_patch_does_not_fall_back(self):
        self.write("1.1.md")
        self.assertIsNone(release_notes.resolve("1.1.2"))


if __name__ == "__main__":
    unittest.main()

