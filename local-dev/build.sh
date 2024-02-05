#!/bin/bash

set -e

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
CONTEXT_DIR=$(cd "$SCRIPT_DIR/.." && pwd)

cd "${CONTEXT_DIR}"

echo "Building server image using host machine's gradle cache"
./gradlew server:shadowJar -x test  -x spotlessJavaCheck
docker build -t littlehorse/lh-server:latest -f docker/server/Dockerfile .
