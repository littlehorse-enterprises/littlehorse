#!/bin/bash
set -e

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd $SCRIPT_DIR/../..

./build/build.sh && kind load docker-image --name littlehorse littlehorse:latest
kubectl delete po -lapp=lh-server -ndefault
