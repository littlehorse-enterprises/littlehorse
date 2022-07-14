#!/bin/bash

set -ex

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

cd ${SCRIPT_DIR}/..

gradle fatJar

docker build -f ${SCRIPT_DIR}/Dockerfile.scheduler -t lh-scheduler .
docker build -f ${SCRIPT_DIR}/Dockerfile.worker -t lh-worker .
