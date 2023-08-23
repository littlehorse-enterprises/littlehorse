from enum import Enum
from pathlib import Path
import sys
import click
from semver import Version
import shlex
import subprocess

from jproperties import Properties
from rich.console import Console
import toml

CONTEXT_SETTINGS = dict(help_option_names=["-h", "--help"])


def print_help():
    with click.Context(main) as ctx:
        click.echo(main.get_help(ctx))


class Release(Enum):
    MAJOR = "major"
    MINOR = "minor"
    PATCH = "patch"


class PreRelease(Enum):
    ALPHA = "alpha"
    BETA = "beta"
    RC = "rc"


class Bump:
    def __init__(self, debug=False):
        self.console = Console()
        self.debug = debug
        self.current = self.current_version()

    def run_command(self, command, error_message=None):
        if self.debug:
            self.console.print(f"[bright_black italic]{command}[/]")
        result = subprocess.run(shlex.split(command), capture_output=True)
        if result.returncode != 0:
            self.console.print(
                f"[red]Error executing: [bold italic]{command}[/][/]",
            )
            if error_message:
                self.console.print(f"[orange3]{error_message}[/]")
            else:
                self.console.print(
                    f"[orange3]{result.stderr.decode('utf-8').strip()}[/]"
                )
            sys.exit(result.returncode)
        return result.stdout.decode("utf-8").strip()

    def current_version(self):
        return Version.parse(self.run_command("git describe --abbrev=0 --tag"))

    def next_version(self, release, prerelease):
        next_version = Version.parse(str(self.current))

        if self.current.prerelease:
            if release:
                raise ValueError(
                    "Current version is a prerelease option 'release' is not"
                    " allowed, pass 'prerelease' or empty"
                )
            if prerelease:
                if prerelease not in self.current.prerelease:
                    if prerelease[0] > self.current.prerelease[0]:
                        next_version = next_version.replace(
                            prerelease=prerelease + ".0"
                        )
                    else:
                        raise ValueError(
                            "It's not possible to bump to a lower"
                            " version 'alpha' < 'beta' < 'rc'"
                        )
                return next_version.bump_prerelease(prerelease)
            return next_version.finalize_version()

        if not release:
            raise ValueError(
                "You have to provide an option 'release' or both"
                " 'release' and 'prerelease'"
            )

        next_version = self.current.next_version(part=release)

        if prerelease:
            next_version = next_version.bump_prerelease(prerelease)

        return next_version

    def bump(self, release, prerelease):
        self.console.print(f"Current version: [dodger_blue1]{self.current}[/]")

        project_path = Path(__file__, "../../../").resolve()
        gradle_properties_path = project_path.joinpath("gradle.properties").resolve()
        python_toml_path = project_path.joinpath("sdk-python/pyproject.toml").resolve()

        try:
            # validate
            self.run_command("git diff --exit-code", "There are unstaged changes")
            self.run_command(
                "git diff --staged --exit-code", "There are staged changes"
            )
            self.run_command(
                "git diff --exit-code master origin/master", "First push your commits"
            )

            # get next version
            next_version = self.next_version(release, prerelease)

            # get confirmation
            confirmation = self.console.input(
                "[bold]Do you want to release a new version"
                f" [dodger_blue1]{next_version}[/]"
                " of LH :racehorse: ([green]yes[/]/[red]no[/])? [/]"
            )

            if not confirmation.startswith("y"):
                self.console.print("Aborting!")
                sys.exit(0)

            # save java version
            java_properties = Properties()

            with open(gradle_properties_path, "rb") as f:
                java_properties.load(f, "utf-8")

            java_properties["version"] = str(next_version)

            with open(gradle_properties_path, "wb") as f:
                java_properties.store(f, encoding="utf-8", timestamp=False)

            # save python version
            toml_data = toml.load(python_toml_path)
            toml_data["tool"]["poetry"]["version"] = str(next_version)

            with open(python_toml_path, "w") as f:
                toml.dump(toml_data, f)

            # commit changes
        except Exception as e:
            self.console.print(f"[red]ERROR![/] [orange3]{e}[/]")
            self.run_command("git checkout " + str(project_path))


@click.command(context_settings=CONTEXT_SETTINGS)
@click.option("--debug", is_flag=True, help="Show the executed internal commands")
@click.option(
    "--prerelease",
    "-p",
    type=click.Choice([pr.value for pr in PreRelease]),
    help="Define if it is a prerelease version.",
)
@click.option(
    "--release",
    "-r",
    type=click.Choice([r.value for r in Release]),
    help="Define the next release version.",
)
def main(release, prerelease, debug):
    """\b
    Examples:
        If current version is a 'prerelease' (0.1.0-alpha.1):
            python -m scripts.bump --prerelease alpha
                (it returns 0.1.0-alpha.2)
            python -m scripts.bump
                (it returns 0.1.0)
        If current version is a 'release' (0.1.0):
            python -m scripts.bump --release minor
                (it returns 0.2.0)
            python -m scripts.bump --release minor --prerelease alpha
                (it returns 0.2.0-alpha.1)
    \b
    Precedence example:
        1.0.0-alpha.1 < 1.0.0-beta.2 < 1.0.0-beta.11 < 1.0.0-rc.1 < 1.0.0

    More info at https://github.com/python-semver/python-semver
    and https://semver.org/.
    """
    bump = Bump(debug)
    bump.bump(release, prerelease)


if __name__ == "__main__":
    main()
