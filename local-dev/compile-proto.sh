#!/bin/bash

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
WORK_DIR=$(cd "$SCRIPT_DIR/.." && pwd)

set -e

cd "${WORK_DIR}"

# First, re-compile the public client proto
rm -rf "${WORK_DIR}/sdk-java/src/main/java/io/littlehorse/sdk/common/proto/"
rm -rf "${WORK_DIR}/sdk-go/common/model/*.pb.go"
rm -rf "${WORK_DIR}/sdk-python/littlehorse/model/*"

protoc --grpc-java_out="${WORK_DIR}/sdk-java/src/main/java/" \
    --java_out="${WORK_DIR}/sdk-java/src/main/java/" \
    --go-grpc_out="${WORK_DIR}/sdk-go/common/model" \
    --go_out="${WORK_DIR}/sdk-go/common/model" \
    -I="$WORK_DIR/schemas/" \
    "$WORK_DIR/schemas/service.proto"

python3 -m grpc_tools.protoc --python_out="${WORK_DIR}/sdk-python/littlehorse/model" \
    --pyi_out="${WORK_DIR}/sdk-python/littlehorse/model" \
    --grpc_python_out="${WORK_DIR}/sdk-python/littlehorse/model" \
    -I="$WORK_DIR/schemas/" \
    "$WORK_DIR/schemas/service.proto"

# There is not option python_package https://github.com/protocolbuffers/protobuf/issues/7061
sed -i 's/service_pb2/littlehorse.model.service_pb2/' sdk-python/littlehorse/model/service_pb2_grpc.py

# Next, compile the internal-server proto
rm -rf server/src/main/java/io/littlehorse/common/proto/

protoc --grpc-java_out="${WORK_DIR}/server/src/main/java/" \
    --java_out="${WORK_DIR}/server/src/main/java/" \
    -I="${WORK_DIR}/schemas" \
    "${WORK_DIR}/schemas/internal_server.proto"
