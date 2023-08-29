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
PUBLIC_PROTOS=$(ls "$WORK_DIR"/schemas | grep -v -E "^internal")
INTERNAL_PROTOS=$(ls "$WORK_DIR"/schemas/internal)
docker run --rm -it -v ${WORK_DIR}:/littlehorse protoc protoc \
    --java_out=/littlehorse/sdk-java/src/main/java \
    --python_out=/littlehorse/sdk-python/littlehorse/model \
    --pyi_out=/littlehorse/sdk-python/littlehorse/model \
    --go_out=/littlehorse/sdk-go/common/model \
    --grpc-java_out=/littlehorse/sdk-java/src/main/java \
    --go-grpc_out=/littlehorse/sdk-go/common/model \
    -I=/littlehorse/schemas \
    $PUBLIC_PROTOS

# compile internal
echo "Compiling internal protobuf objects"
docker run --rm -it -v ${WORK_DIR}:/littlehorse protoc protoc \
    --java_out=/littlehorse/server/src/main/java \
    --grpc-java_out=/littlehorse/server/src/main/java \
    -I=/littlehorse/schemas:/littlehorse/schemas/internal \
    $INTERNAL_PROTOS

# grpc in python
echo "Compiling python grpc"
docker run --rm -it -v ${WORK_DIR}:/littlehorse protoc python3 -m grpc_tools.protoc \
    --grpc_python_out=/littlehorse/sdk-python/littlehorse/model \
    -I=/littlehorse/schemas/ \
    service.proto

# fix python packages, no option python_package https://github.com/protocolbuffers/protobuf/issues/7061
echo "Fixing python objects"
for i in $(ls "$WORK_DIR"/schemas | grep -v -E "^internal" | sed 's/.proto/_pb2/'); do
    sed -i "s/^import ${i}/import littlehorse.model.${i}/" "${WORK_DIR}"/sdk-python/littlehorse/model/*
done

echo "The Force will be with you. Always."
