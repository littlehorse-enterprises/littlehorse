#!/bin/bash

set -e

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
WORK_DIR=$(cd "$SCRIPT_DIR/.." && pwd)

cd "$WORK_DIR"

publish_maven() {
    read -p "This script should only be used in special situations. Do you want to continue? [yes/no] " answer

    if [ "yes" != "$answer" ]
    then
        echo "Terminated" 
        exit 1
    fi

    read -p "GPG signing key location: " signingKey
    read -s -p "GPG signing key password: " signingPassword
    echo
    read -p "OSSRH username: " ossrhUsername
    read -s -p "OSSRH password: " ossrhPassword
    echo
    
    export ORG_GRADLE_PROJECT_signingKey="$(<$signingKey)"
    export ORG_GRADLE_PROJECT_signingPassword="$signingPassword"
    export ORG_GRADLE_PROJECT_ossrhUsername="$ossrhUsername"
    export ORG_GRADLE_PROJECT_ossrhPassword="$ossrhPassword"

    ./gradlew sdk-java:publish test-utils:publish test-utils-container:publish
}

case $1 in
  --maven) publish_maven;;
  *) echo "Invalid option.";; 
esac