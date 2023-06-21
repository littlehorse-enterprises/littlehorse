#!/bin/bash

set -e

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
WORK_DIR=$(cd "$SCRIPT_DIR/.." && pwd)

cd "$WORK_DIR"

URL="http://localhost:9999"
# for future references https://next.sonarqube.com/sonarqube/web_api/api/users
./gradlew sonar -Dsonar.host.url="$URL" -Dsonar.verbose=false -Dsonar.login=admin -Dsonar.password=admin

xdg-open "$URL"
