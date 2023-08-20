#!/bin/bash

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
WORK_DIR=$(cd "$SCRIPT_DIR/.." && pwd)

set -e

# First, re-compile the public client proto
rm -rf ${WORK_DIR}/sdk-java/src/main/java/io/littlehorse/sdk/common/proto/
rm -rf ${WORK_DIR}/sdk-go/common/model/*.pb.go

PUBLIC_PROTOS=$(ls $WORK_DIR/schemas | grep '\.proto' | grep -v internal_server.proto)
# PUBLIC_PROTOS="object_id.proto service.proto enums.proto"

cd $WORK_DIR/schemas

protoc --grpc-java_out="${WORK_DIR}/sdk-java/src/main/java/" \
    --java_out="${WORK_DIR}/sdk-java/src/main/java/" \
    --go-grpc_out=${WORK_DIR}/sdk-go/common/model \
    --go_out=${WORK_DIR}/sdk-go/common/model \
    -I="$WORK_DIR/schemas/" \
    $PUBLIC_PROTOS

# Next, compile the internal-server proto
rm -rf server/src/main/java/io/littlehorse/common/proto/

protoc --grpc-java_out="${WORK_DIR}/server/src/main/java/" \
    --java_out="${WORK_DIR}/server/src/main/java/" \
    -I="${WORK_DIR}/schemas" \
    "${WORK_DIR}/schemas/internal_server.proto"
