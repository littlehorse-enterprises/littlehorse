#!/bin/bash

set -e

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
WORK_DIR=$(cd "$SCRIPT_DIR/../docker" && pwd)
CONTEXT_DIR=$(cd "$SCRIPT_DIR/.." && pwd)

cd "$CONTEXT_DIR"
./gradlew clean shadowJar -x test

docker build --file "$WORK_DIR/Dockerfile" \
    --tag littlehorse.io/littlehorse-server:latest "$CONTEXT_DIR"
