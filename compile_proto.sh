#!/bin/bash

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

rm -r app/src/main/java/io/littlehorse/common/proto/

protoc \
    -I=${SCRIPT_DIR}/proto/io-littlehorse-proto/proto:${SCRIPT_DIR}/proto/ \
    --grpc-java_out=${SCRIPT_DIR}/app/src/main/java/ \
    --java_out=${SCRIPT_DIR}/app/src/main/java/ \
    --experimental_allow_proto3_optional \
    ${SCRIPT_DIR}/proto/internal_server.proto
