#!/bin/bash

# semver documentation https://github.com/npm/node-semver

set -e

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
WORK_DIR=$(cd "$SCRIPT_DIR/.." && pwd)

# validate input
if [ -z "$1" ]
  then
    echo "No argument supplied. Read the documentation at https://github.com/npm/node-semver."
    echo
    npm run --silent semver
    exit 1
fi

# change path
cd "$WORK_DIR"

# get and validate current branch
BRANCH=$(git branch --show-current)

if [[ "$BRANCH" != "master" ]]
then
    echo "To increase the version you must be in master."
    exit 1
fi

# validate pending changes
if ! git diff --exit-code &>/dev/null
then
    echo "There are unstaged changes."
    exit 1
fi

if ! git diff --staged --exit-code &>/dev/null
then
    echo "There are staged changes. Commit them firts."
    exit 1
fi

if ! git diff --exit-code master origin/master &>/dev/null
then
    echo "There are committed changes locally. Push them first."
    exit 1
fi

# validate builds
echo "Validating it builds"
./gradlew --console=plain -q test shadowJar

# load current version from git
if git describe --abbrev=0 --tags &>/dev/null
then
    CURRENT_VERSION=$(git describe --abbrev=0 --tags)
else
    CURRENT_VERSION="0.1.0-alpha.0"
fi

# upgrade version
NEW_VERSION=$(npm run --silent semver -- "$CURRENT_VERSION" "$@")

# validate action
read -rp "Do you wish to upgrade to $NEW_VERSION? " yn

if ! cat gradle.properties |  grep "^version=$CURRENT_VERSION$" &>/dev/null
then
    echo "It is not possible to continue. Current version $CURRENT_VERSION does not match with gradle.properties file."
    exit 1
fi

case $yn in
    [Yy]* )
        # update gradle artifact version
        sed -i "s/version=$CURRENT_VERSION/version=$NEW_VERSION/" ./gradle.properties
        git add --all
        git commit -m "[skip main] New release $NEW_VERSION"
        git tag "$NEW_VERSION"
        git push
        git push --tags
    ;;
    [Nn]* )
        echo "Skipped."
        exit 0
    ;;
    * )
        echo "Please answer yes or no."
        exit 1
    ;;
esac
