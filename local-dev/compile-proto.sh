#!/bin/bash
set -e
PROTOC_VERSION=31.1
# define variable
SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
WORK_DIR=$(cd "$SCRIPT_DIR/.." && pwd)
PUBLIC_PROTOS=$(ls "$WORK_DIR"/schemas/littlehorse | grep -v -E "^internal")
INTERNAL_PROTOS=$(ls "$WORK_DIR"/schemas/internal)
docker_run="docker run --user $(id -u):$(id -g) --rm -it -v ${WORK_DIR}:/littlehorse lh-protoc:$PROTOC_VERSION"

# compile protoc
echo "Compiling docker image 'lh-protoc:$PROTOC_VERSION'"
docker build -q --tag lh-protoc:$PROTOC_VERSION -f - "${SCRIPT_DIR}" <<EOF

FROM ubuntu:24.04

ENV PROTOC_VERSION           23.4
ENV PROTO_GEN_JAVA           1.57.2
ENV PROTO_GEN_GO             1.31.0
ENV PROTO_GEN_GO_GRPC        1.3.0
ENV PROTO_GEN_PYTHON         1.69.0
ENV PROTO_GEN_JS             1.181.2

ENV GOBIN /usr/local/bin

RUN apt update && apt install -y --no-install-recommends \
  python3 \
  git \
  pip \
  wget \
  ca-certificates \
  unzip \
  golang \
  nodejs \
  npm && \
  apt-get clean && \
  rm -rf /var/lib/apt/lists/*

RUN set -x; \
  dpkgArch="\$(uname -m)" && \
  case "\$dpkgArch" in \
  x86_64) ARCH='x86_64';; \
  aarch64) ARCH='aarch_64';; \
  *) echo >&2 "error: unsupported architecture: \$dpkgArch"; exit 1 ;; \
  esac && \
  wget -q https://github.com/protocolbuffers/protobuf/releases/download/v\${PROTOC_VERSION}/protoc-\${PROTOC_VERSION}-linux-\${ARCH}.zip -O /tmp/protoc.zip && \
  unzip /tmp/protoc.zip -d /usr/local && \
  rm /tmp/protoc.zip && \
  wget -q https://repo1.maven.org/maven2/io/grpc/protoc-gen-grpc-java/\${PROTO_GEN_JAVA}/protoc-gen-grpc-java-\${PROTO_GEN_JAVA}-linux-\${ARCH}.exe -O /usr/local/bin/protoc-gen-grpc-java

RUN chmod +x /usr/local/bin/* && \
  rm -f /tmp/*

RUN go install google.golang.org/protobuf/cmd/protoc-gen-go@v\${PROTO_GEN_GO} && \
  go install google.golang.org/grpc/cmd/protoc-gen-go-grpc@v\${PROTO_GEN_GO_GRPC}

RUN pip3 install grpcio-tools==\${PROTO_GEN_PYTHON} --break-system-packages

RUN npm install -g ts-proto@\${PROTO_GEN_JS}

EOF

# check protoc version
echo "Docker image compiled, protoc --version: " $($docker_run protoc --version)

# clean old objects
echo "Cleaning objects"
rm -rf "${WORK_DIR}"/sdk-java/src/main/java/io/littlehorse/sdk/common/proto/*
rm -rf "${WORK_DIR}"/sdk-go/lhproto/*
rm -rf "${WORK_DIR}"/sdk-python/littlehorse/model/*
rm -rf "${WORK_DIR}"/server/src/main/java/io/littlehorse/common/proto/*

# compile protobuf
echo "Compiling protobuf objects"
$docker_run protoc \
    --java_out=/littlehorse/sdk-java/src/main/java \
    --python_out=/littlehorse/sdk-python/littlehorse/model \
    --pyi_out=/littlehorse/sdk-python/littlehorse/model \
    --go_out=/littlehorse/sdk-go/lhproto \
    --grpc-java_out=/littlehorse/sdk-java/src/main/java \
    --go-grpc_out=/littlehorse/sdk-go/lhproto \
    -I=/littlehorse/schemas/littlehorse \
    $PUBLIC_PROTOS

# compile internal
echo "Compiling internal protobuf objects"
$docker_run protoc \
    --java_out=/littlehorse/server/src/main/java \
    --grpc-java_out=/littlehorse/server/src/main/java \
    -I=/littlehorse/schemas/littlehorse:/littlehorse/schemas/internal \
    $INTERNAL_PROTOS

# grpc in python
echo "Compiling python grpc"
$docker_run python3 -m grpc_tools.protoc \
    --grpc_python_out=/littlehorse/sdk-python/littlehorse/model \
    -I=/littlehorse/schemas/littlehorse \
    service.proto

# fix python packages, no option python_package https://github.com/protocolbuffers/protobuf/issues/7061
echo "Fixing python objects"
for i in $(ls "$WORK_DIR"/schemas/littlehorse | grep -v -E "^internal" | sed 's/.proto/_pb2/'); do
    sed -i.bak "s/^import ${i}/import littlehorse.model.${i}/" "${WORK_DIR}"/sdk-python/littlehorse/model/*
done

rm -f "${WORK_DIR}"/sdk-python/littlehorse/model/*.bak

find "${WORK_DIR}/sdk-python/littlehorse/model" -type f -name "*.py" -print0 | sort -z | xargs -0 -I {} sh -c 'echo "from .$(basename $1 .py) import *" >> $(dirname $1)/__init__.py' _ {}

# compile js protobuf
echo "Compiling protobuf sdk-js"

# This segment is for the sdk-js
$docker_run protoc \
	--plugin=/usr/local/lib/node_modules/ts-proto/protoc-gen-ts_proto \
	--ts_proto_out /littlehorse/sdk-js/src/proto \
	--ts_proto_opt=env=node,outputServices=nice-grpc,outputServices=generic-definitions,outputJsonMethods=true,esModuleInterop=true,useDate=string,stringEnums=true,useExactTypes=false,exportCommonSymbols=false,oneof=unions-value \
	-I /littlehorse/schemas/littlehorse \
    service.proto

echo "The Force will be with you. Always."
