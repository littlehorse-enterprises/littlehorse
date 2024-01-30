#!/bin/bash

set -e

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
WORK_DIR=$(cd "$SCRIPT_DIR/.." && pwd)

cd "$WORK_DIR"

./gradlew canary:installDist -x shadowJar -x test
./canary/build/install/canary/bin/canary canary/canary.properties
