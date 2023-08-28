#!/bin/bash

set -e

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
LH_SERVER_WORK_DIR=$(cd "$SCRIPT_DIR/../docker/server" && pwd)
LH_STANDALONE_WORK_DIR=$(cd "$SCRIPT_DIR/../docker/standalone" && pwd)
CONTEXT_DIR=$(cd "$SCRIPT_DIR/.." && pwd)

cd "$CONTEXT_DIR"

echo "Building server"
docker build --file ${LH_SERVER_WORK_DIR}/Dockerfile \
    --tag littlehorse/littlehorse-server:latest ${CONTEXT_DIR}

echo "Building standalone"
docker build --file ${LH_STANDALONE_WORK_DIR}/Dockerfile \
    --tag littlehorse/littlehorse-standalone:latest ${CONTEXT_DIR}
