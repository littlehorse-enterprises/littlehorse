#!/bin/bash
set -e

# define variable
SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
WORK_DIR=$(cd "$SCRIPT_DIR" && pwd)
PUBLIC_PROTOS=$(ls "$WORK_DIR"/littlehorse/schemas | grep -v -E "^internal")
docker_run="docker run --rm -it -v ${WORK_DIR}:/lh-dashboard lh-protoc:23.4"

# compile protoc
echo "Compiling docker image 'lh-protoc:23.4'"
docker build -q --tag lh-protoc:23.4 -<<EOF
FROM ubuntu:22.04
ENV PROTOC_VERSION="23.4"
RUN apt update && \
    apt install -y --no-install-recommends wget ca-certificates unzip nodejs npm && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* && \
    wget -q https://github.com/protocolbuffers/protobuf/releases/download/v23.4/protoc-23.4-linux-x86_64.zip -O /tmp/protoc.zip && \
    unzip -d /usr/local/ /tmp/protoc.zip && \
    npm install -g ts-proto && \
    chmod +x /usr/local/bin/* && \
    rm -f /tmp/*
EOF

# check protoc version
echo "Docker image compiled, protoc --version: " $($docker_run protoc --version)

# clean old objects
echo "Cleaning objects"
rm -rf "${WORKDIR}"/littlehorse-public-api/*

# compile protobuf
echo "Compiling protobuf objects"
$docker_run protoc \
    --plugin=/usr/local/lib/node_modules/ts-proto/protoc-gen-ts_proto --ts_proto_opt=outputServices=nice-grpc,outputServices=generic-definitions,useDate=string,esModuleInterop=true,stringEnums=true --ts_proto_out="/lh-dashboard/apps/web/littlehorse-public-api" \
    -I=/lh-dashboard/littlehorse/schemas \
    $PUBLIC_PROTOS

echo "Fixing imports for protobuff JS for more information see the README file"
find $WORK_DIR/apps/web/littlehorse-public-api/ -type f -readable -writable -exec sed -i "s/import _m0/import * as _m0/g" {} \;

echo "The Force will be with you. Always."
