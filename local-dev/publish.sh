#!/bin/bash

set -e

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
WORK_DIR=$(cd "$SCRIPT_DIR/.." && pwd)

cd "$WORK_DIR"

verify_java_version(){
    java_version="$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')"
    java_major_version=${java_version:0:2}

    if [ "11" != "$java_major_version" ]; then
        echo "Change to Java 11 first"
        exit 1
    fi
}

publish_maven() {
    read -p "This script should only be used in special situations. Do you want to continue? [yes/no] " answer

    if [ "yes" != "$answer" ]; then
        echo "Terminated"
        exit 1
    fi

    if git describe --exact-match --tags >/dev/null 2>&1; then
        TAG=$(git describe --exact-match --tags)
        read -p "Tag '$TAG' was found. Do you want to continue? [yes/no] " answer

        if [ "yes" != "$answer" ]; then
            echo "Terminated"
            exit 1
        fi

        sed -i "s/version=.*/version=${TAG}/g" gradle.properties
    else
      echo "No tags found"
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
    export ORG_GRADLE_PROJECT_sonatypeUsername="$ossrhUsername"
    export ORG_GRADLE_PROJECT_sonatypePassword="$ossrhPassword"

    ./gradlew \
      sdk-java:publishToSonatype \
      test-utils:publishToSonatype \
      test-utils-container:publishToSonatype \
      closeSonatypeStagingRepository

    echo "This is a manual step. Go to https://central.sonatype.com and to publish."
}

case $1 in
--maven)
  verify_java_version
  publish_maven
;;
*)
  echo "Invalid option."
;;
esac
