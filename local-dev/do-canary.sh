#!/bin/bash

set -e

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
WORK_DIR=$(cd "$SCRIPT_DIR/.." && pwd)
LOG_LEVEL=DEBUG
cd "$WORK_DIR"

./gradlew canary:clean canary:installDist
./canary/build/install/canary/bin/canary canary/canary.properties
