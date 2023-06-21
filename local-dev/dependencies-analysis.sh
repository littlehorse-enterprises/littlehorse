#!/bin/bash

set -e

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
WORK_DIR=$(cd "$SCRIPT_DIR/.." && pwd)

cd "$WORK_DIR"

./gradlew dependencyCheckAnalyze

xdg-open server/build/reports/dependency-check-report.html
xdg-open client/build/reports/dependency-check-report.html
