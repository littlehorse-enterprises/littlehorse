#!/bin/bash

set -e

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
LH_SERVER_WORK_DIR=$(cd "$SCRIPT_DIR/../docker/lh-server" && pwd)
LH_STANDALONE_WORK_DIR=$(cd "$SCRIPT_DIR/../docker/lh-standalone" && pwd)
CONTEXT_DIR=$(cd "$SCRIPT_DIR/.." && pwd)

cd "$CONTEXT_DIR"

echo "Building lh-server"
docker build --file ${LH_SERVER_WORK_DIR}/Dockerfile \
    --tag littlehorse/lh-server:latest ${CONTEXT_DIR}

echo "Building lh-standalone"
docker build --file ${LH_STANDALONE_WORK_DIR}/Dockerfile \
    --tag littlehorse/lh-standalone:latest ${CONTEXT_DIR}
