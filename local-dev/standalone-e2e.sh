#!/bin/bash

set -e

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
WORK_DIR=$(cd "$SCRIPT_DIR/.." && pwd)

cd "$WORK_DIR"

./gradlew server:testJar
java -cp "./server/build/libs/*" org.junit.platform.console.ConsoleLauncher junit execute --select-package e2e
