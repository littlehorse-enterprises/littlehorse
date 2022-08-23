#!/bin/bash
set -e

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd $SCRIPT_DIR/../..

kubectl exec -nkafka lh-kafka-kafka-0 -it -- bash -c '
export TOPICS=$(bin/kafka-topics.sh --bootstrap-server localhost:9092 --list | grep -v "__")
if [ ${#TOPICS[@]} -eq 0 ]
then
    echo "Nothing to do!"
else
    bin/kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic $(echo $TOPICS | tr " " "|")
    echo "Deleted topics!!"
fi
' &

kubectl delete deploy --selector io.littlehorse/deployedBy &
kubectl delete sts lh-server &
kubectl delete sts lh-scheduler &
kubectl delete pvc --selector littlehorse.io/kafka-state-dir=true &

kubectl delete po -nkafka -lapp=postgres &

./build/build.sh && kind load docker-image --name littlehorse littlehorse:latest &

wait

wait

kubectl apply -f ${SCRIPT_DIR}/core-api-1.yaml

kubectl wait sts/lh-server --for=condition=ready --timeout=120s &

echo "Waiting for server to come alive"

wait

kubectl port-forward svc/lh-server 5000:5000 -ndefault &

kubectl get po -w --namespace default
