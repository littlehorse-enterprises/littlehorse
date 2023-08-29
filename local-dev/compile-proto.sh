#!/bin/bash
set -e

# define variable
SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
WORK_DIR=$(cd "$SCRIPT_DIR/.." && pwd)
PROTOC_DOCKER_DIR=$(cd "$SCRIPT_DIR/../docker/protoc" && pwd)

# compile protoc
echo "Compiling protoc" $(docker build -q --file ${PROTOC_DOCKER_DIR}/Dockerfile --tag protoc ${PROTOC_DOCKER_DIR})
echo "Protoc version" $(docker run --rm -it protoc protoc --version)

# clean old objects
echo "Cleaning objects"
rm -rf "${WORK_DIR}"/sdk-java/src/main/java/io/littlehorse/sdk/common/proto/*
rm -rf "${WORK_DIR}"/sdk-go/common/model/*
rm -rf "${WORK_DIR}"/sdk-python/littlehorse/model/*
rm -rf "${WORK_DIR}"/server/src/main/java/io/littlehorse/common/proto/*

# compile protobuf
echo "Compiling protobuf objects"
PUBLIC_PROTOS=$(ls "$WORK_DIR"/schemas | grep -v -E "^(internal|service.proto)")
INTERNAL_PROTOS=$(ls "$WORK_DIR"/schemas/internal | grep -v -E "^(internal)")
docker run --rm -it -v ${WORK_DIR}:/littlehorse protoc protoc \
    --java_out=/littlehorse/sdk-java/src/main/java/ \
    --python_out=/littlehorse/sdk-python/littlehorse/model \
    --pyi_out=/littlehorse/sdk-python/littlehorse/model \
    --go_out=/littlehorse/sdk-go/common/model \
    -I=/littlehorse/schemas/ \
    $PUBLIC_PROTOS

echo "Compiling internal protobuf objects"
docker run --rm -it -v ${WORK_DIR}:/littlehorse protoc protoc \
    --java_out=/littlehorse/server/src/main/java/ \
    -I=/littlehorse/schemas:/littlehorse/schemas/internal \
    $INTERNAL_PROTOS


# cd "${WORK_DIR}"/schemas

# # First, re-compile the public client proto


# PUBLIC_PROTOS=$(ls "$WORK_DIR"/schemas | grep '\.proto')
# INTERNAL_PROTOS=$(ls "$WORK_DIR"/schemas/internal | grep '\.proto')

# cd "${WORK_DIR}"/schemas

# protoc --grpc-java_out="${WORK_DIR}"/sdk-java/src/main/java/ \
#     --java_out="${WORK_DIR}"/sdk-java/src/main/java/ \
#     --go-grpc_out="${WORK_DIR}"/sdk-go/common/model \
#     --go_out="${WORK_DIR}"/sdk-go/common/model \
#     -I="${WORK_DIR}"/schemas/ \
#     $PUBLIC_PROTOS

# python3 -m grpc_tools.protoc --python_out="${WORK_DIR}"/sdk-python/littlehorse/model \
#     --pyi_out="${WORK_DIR}"/sdk-python/littlehorse/model \
#     --grpc_python_out="${WORK_DIR}"/sdk-python/littlehorse/model \
#     -I="$WORK_DIR"/schemas/ \
#     $PUBLIC_PROTOS

# # There is not option python_package https://github.com/protocolbuffers/protobuf/issues/7061
# for i in $(ls -1 *.proto | sed 's/.proto/_pb2/'); do
#     sed -i "s/^import ${i}/import littlehorse.model.${i}/" "${WORK_DIR}"/sdk-python/littlehorse/model/*
# done

# # Next, compile the internal-server proto
# protoc --grpc-java_out="${WORK_DIR}"/server/src/main/java/ \
#     --java_out="${WORK_DIR}"/server/src/main/java/ \
#     -I="${WORK_DIR}"/schemas:"${WORK_DIR}"/schemas/internal \
#     $INTERNAL_PROTOS

# echo "The Force will be with you. Always."
