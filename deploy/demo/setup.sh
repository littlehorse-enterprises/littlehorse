#!/bin/bash

set -ex

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

kubectl apply -f ${SCRIPT_DIR}/kafka-ns.yaml
kubectl apply -f ${SCRIPT_DIR}/strimzi-crd.yaml
kubectl apply -f ${SCRIPT_DIR}/lh-kafka.yaml

kubectl wait kafka/lh-kafka --for=condition=Ready --timeout=300s -n kafka 


kubectl apply -f ${SCRIPT_DIR}/core-api-0.yaml
sleep 2
kubectl apply -f ${SCRIPT_DIR}/core-api-1.yaml
