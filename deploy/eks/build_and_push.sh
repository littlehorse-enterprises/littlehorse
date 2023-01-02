#!/bin/bash

set -ex

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

cd ${SCRIPT_DIR}/../..

./build/build.sh
docker tag littlehorse:latest 834373697194.dkr.ecr.us-east-2.amazonaws.com/littlehorse:latest
docker push 834373697194.dkr.ecr.us-east-2.amazonaws.com/littlehorse:latest

~/io-littlehorse-jlib/build/build.sh
docker tag lh-example-worker:latest 834373697194.dkr.ecr.us-east-2.amazonaws.com/lh-example-worker:latest
docker push 834373697194.dkr.ecr.us-east-2.amazonaws.com/lh-example-worker:latest
