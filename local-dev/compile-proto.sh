#!/bin/bash

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
WORK_DIR=$(cd "$SCRIPT_DIR/.." && pwd)

set -e

# First, re-compile the public client proto
rm -rf client/src/main/java/io/littlehorse/jlib/common/proto/

protoc --grpc-java_out="${WORK_DIR}/client/src/main/java/" \
    --java_out="${WORK_DIR}/client/src/main/java/" \
    -I="$WORK_DIR/proto/lh-proto/proto/" \
    "$WORK_DIR/proto/lh-proto/proto/service.proto"

# Next, compile the internal-server proto
rm -rf server/src/main/java/io/littlehorse/common/proto/

protoc --grpc-java_out="${WORK_DIR}/server/src/main/java/" \
    --java_out="${WORK_DIR}/server/src/main/java/" \
    -I="${WORK_DIR}/proto/lh-proto/proto:${WORK_DIR}/proto/" \
    "${WORK_DIR}/proto/internal_server.proto"
