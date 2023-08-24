import unittest

from scripts.bump import Bump
from semver import Version


class TestLHConfig(unittest.TestCase):
    def test_bump_release(self):
        bump = Bump()
        current = Version.parse("0.1.0")

        self.assertEqual(
            bump.next_version(current, release="minor", prerelease=None),
            Version.parse("0.2.0"),
        )

        self.assertEqual(
            bump.next_version(current, release="patch", prerelease=None),
            Version.parse("0.1.1"),
        )

        self.assertEqual(
            bump.next_version(current, release="major", prerelease=None),
            Version.parse("1.0.0"),
        )

    def test_bump_release_to_prerelease(self):
        bump = Bump()
        current = Version.parse("0.1.0")

        with self.assertRaises(ValueError) as exception_context:
            bump.next_version(current, None, "rc")
        self.assertEqual(
            "You have to provide the option 'release'"
            ", or both 'release' and 'prerelease'",
            str(exception_context.exception),
        )

    def test_raise_error_if_version_is_prerelease_and_pass_release(self):
        bump = Bump()
        current = Version.parse("0.1.0-alpha.1")

        with self.assertRaises(ValueError) as exception_context:
            bump.next_version(current, "minor", None)

        self.assertEqual(
            "Current version is a prerelease, option 'release'"
            " is not allowed, pass 'prerelease' or empty",
            str(exception_context.exception),
        )

    def test_bump_release_and_prerelease(self):
        bump = Bump()
        current = Version.parse("0.1.0")

        self.assertEqual(
            bump.next_version(current, release="major", prerelease="alpha"),
            Version.parse("1.0.0-alpha.1"),
        )
        self.assertEqual(
            bump.next_version(current, release="minor", prerelease="alpha"),
            Version.parse("0.2.0-alpha.1"),
        )
        self.assertEqual(
            bump.next_version(current, release="patch", prerelease="alpha"),
            Version.parse("0.1.1-alpha.1"),
        )

    def test_bump_prerelease_to_final(self):
        bump = Bump()
        current = Version.parse("0.1.0-alpha.1")

        self.assertEqual(
            bump.next_version(current, release=None, prerelease=None),
            Version.parse("0.1.0"),
        )

    def test_bump_higher_prerelease(self):
        bump = Bump()
        current = Version.parse("0.1.0-alpha.1")

        self.assertEqual(
            bump.next_version(current, release=None, prerelease="alpha"),
            Version.parse("0.1.0-alpha.2"),
        )

        self.assertEqual(
            bump.next_version(current, release=None, prerelease="beta"),
            Version.parse("0.1.0-beta.1"),
        )

        self.assertEqual(
            bump.next_version(current, release=None, prerelease="rc"),
            Version.parse("0.1.0-rc.1"),
        )

        current = Version.parse("0.1.0-beta.1")

        self.assertEqual(
            bump.next_version(current, release=None, prerelease="beta"),
            Version.parse("0.1.0-beta.2"),
        )

        self.assertEqual(
            bump.next_version(current, release=None, prerelease="rc"),
            Version.parse("0.1.0-rc.1"),
        )

    def test_raise_error_if_bump_lower_prerelease(self):
        bump = Bump()
        current = Version.parse("0.1.0-rc.1")
        msg = "It's not possible to bump to a lower version, 'alpha' < 'beta' < 'rc'"

        with self.assertRaises(ValueError) as exception_context:
            bump.next_version(current, None, "alpha")

        self.assertEqual(
            msg,
            str(exception_context.exception),
        )

        with self.assertRaises(ValueError) as exception_context:
            bump.next_version(current, None, "beta")

        self.assertEqual(
            msg,
            str(exception_context.exception),
        )

        current = Version.parse("0.1.0-beta.1")

        with self.assertRaises(ValueError) as exception_context:
            bump.next_version(current, None, "alpha")

        self.assertEqual(
            msg,
            str(exception_context.exception),
        )


if __name__ == "__main__":
    unittest.main()
