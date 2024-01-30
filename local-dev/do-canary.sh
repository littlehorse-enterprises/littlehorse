#!/bin/bash

set -e

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
WORK_DIR=$(cd "$SCRIPT_DIR/.." && pwd)

cd "$WORK_DIR"

./gradlew canary:clean canary:build -x test -x spotlessJavaCheck
java -jar canary/build/libs/canary-*-all.jar canary/canary.properties
