#!/bin/bash

set -ex

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

cd ${SCRIPT_DIR}/../..

./build/build.sh
docker tag littlehorse:latest 834373697194.dkr.ecr.us-west-2.amazonaws.com/littlehorse:latest
docker push 834373697194.dkr.ecr.us-west-2.amazonaws.com/littlehorse:latest
