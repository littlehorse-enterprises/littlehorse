#!/bin/bash
set -e

# define variable
SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
WORK_DIR=$(cd "$SCRIPT_DIR/.." && pwd)
PUBLIC_PROTOS=$(ls "$WORK_DIR"/schemas | grep -v -E "^internal")
INTERNAL_PROTOS=$(ls "$WORK_DIR"/schemas/internal)
docker_run="docker run --user $(id -u):$(id -g) --rm -it -v ${WORK_DIR}:/littlehorse lh-protoc:23.4"

# compile protoc
echo "Compiling docker image 'lh-protoc:23.4'"
docker build -q --tag lh-protoc:23.4 -<<EOF
FROM ubuntu:22.04
ENV PROTOC_VERSION="23.4"
RUN apt update && \
    apt install -y --no-install-recommends python3 pip wget ca-certificates unzip golang nodejs npm && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* && \
    wget -q https://github.com/protocolbuffers/protobuf/releases/download/v23.4/protoc-23.4-linux-x86_64.zip -O /tmp/protoc.zip && \
    unzip -d /usr/local/ /tmp/protoc.zip && \
    wget -q https://repo1.maven.org/maven2/io/grpc/protoc-gen-grpc-java/1.57.2/protoc-gen-grpc-java-1.57.2-linux-x86_64.exe -O /usr/local/bin/protoc-gen-grpc-java && \
    GOBIN=/usr/local/bin go install google.golang.org/protobuf/cmd/protoc-gen-go@v1.31.0 && \
    GOBIN=/usr/local/bin go install google.golang.org/grpc/cmd/protoc-gen-go-grpc@v1.3.0 && \
    pip install grpcio-tools==1.57.0 && \
    npm install -g ts-proto && \
    chmod +x /usr/local/bin/* && \
    rm -f /tmp/*
EOF

# check protoc version
echo "Docker image compiled, protoc --version: " $($docker_run protoc --version)

# clean old objects
echo "Cleaning objects"
rm -rf "${WORK_DIR}"/sdk-java/src/main/java/io/littlehorse/sdk/common/proto/*
rm -rf "${WORK_DIR}"/sdk-go/common/model/*
rm -rf "${WORK_DIR}"/sdk-python/littlehorse/model/*
rm -rf "${WORK_DIR}"/server/src/main/java/io/littlehorse/common/proto/*
rm -rf "${WORK_DIR}"/dashboard/apps/web/littlehorse-public-api/*

# compile protobuf
echo "Compiling protobuf objects"
$docker_run protoc \
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
$docker_run protoc \
    --java_out=/littlehorse/server/src/main/java \
    --grpc-java_out=/littlehorse/server/src/main/java \
    -I=/littlehorse/schemas:/littlehorse/schemas/internal \
    $INTERNAL_PROTOS

# grpc in python
echo "Compiling python grpc"
$docker_run python3 -m grpc_tools.protoc \
    --grpc_python_out=/littlehorse/sdk-python/littlehorse/model \
    -I=/littlehorse/schemas/ \
    service.proto

# fix python packages, no option python_package https://github.com/protocolbuffers/protobuf/issues/7061
echo "Fixing python objects"
for i in $(ls "$WORK_DIR"/schemas | grep -v -E "^internal" | sed 's/.proto/_pb2/'); do
    sed -i "s/^import ${i}/import littlehorse.model.${i}/" "${WORK_DIR}"/sdk-python/littlehorse/model/*
done

# compile js protobuf
echo "Compiling protobuf objects"

# This is still being used in the dashboard, we should remove it as soon as there's not more refereces to it
$docker_run protoc \
    --plugin=/usr/local/lib/node_modules/ts-proto/protoc-gen-ts_proto \
    --ts_proto_opt=outputServices=nice-grpc,outputServices=generic-definitions,useDate=string,esModuleInterop=true,stringEnums=true \
    --ts_proto_out="/littlehorse/dashboard/apps/web/littlehorse-public-api" \
    -I=/littlehorse/schemas \
    $PUBLIC_PROTOS

echo "Fixing imports for protobuff JS for more information see the README file"
find "${WORK_DIR}"/dashboard/apps/web/littlehorse-public-api/ -type f -readable -writable -exec sed -i "s/import _m0/import * as _m0/g" {} \;


# This segment is for the sdk-js should replace the dashboard version eventually
$docker_run protoc \
	--plugin=/usr/local/lib/node_modules/ts-proto/protoc-gen-ts_proto \
	--ts_proto_out /littlehorse/sdk-js/src/proto \
	--ts_proto_opt=env=node,outputServices=nice-grpc,outputServices=generic-definitions,outputJsonMethods=false,useExactTypes=false,eslint_disable,esModuleInterop=true,useDate=string,stringEnums=true \
	-I /littlehorse/schemas \
    service.proto

echo "The Force will be with you. Always."
