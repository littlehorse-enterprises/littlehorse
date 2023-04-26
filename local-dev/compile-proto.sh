#!/bin/bash

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
WORK_DIR=$(cd "$SCRIPT_DIR/.." && pwd)

rm -rf app/src/main/java/io/littlehorse/common/proto/

protoc \
    -I="${WORK_DIR}/proto/lh-proto/proto:${WORK_DIR}/proto/" \
    --grpc-java_out="${WORK_DIR}/app/src/main/java/" \
    --java_out="${WORK_DIR}/app/src/main/java/" \
    --experimental_allow_proto3_optional \
    "${WORK_DIR}/proto/internal_server.proto"
