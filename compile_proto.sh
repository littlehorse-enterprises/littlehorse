#!/bin/bash

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

rm -r app/src/main/java/io/littlehorse/common/proto/

protoc --python_out=${SCRIPT_DIR}/lhctl/lh_lib/proto --java_out=${SCRIPT_DIR}/app/src/main/java/ -I=$SCRIPT_DIR/proto --experimental_allow_proto3_optional $SCRIPT_DIR/proto/*.proto
