#!/bin/bash
set -e

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd $SCRIPT_DIR/../..

# kubectl scale deployment lh-server --replicas=0 --namespace default &
# kubectl scale deployment lh-scheduler --replicas=0 --namespace default &
# kubectl delete deploy --selector io.littlehorse/deployedBy &

# kubectl delete po -nkafka -lapp=postgres

kubectl exec -nkafka lh-kafka-kafka-0 -it -- bash -c '
export TOPICS=$(bin/kafka-topics.sh --bootstrap-server localhost:9092 --list)
if [ ${#TOPICS[@]} -eq 0 ]
then
    echo "Nothing to do!"
else
    bin/kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic $(echo $TOPICS | tr " " "|")
    echo "Deleted topics!!"
fi
' &

./build.sh &

wait

docker tag lh-server:latest 834373697194.dkr.ecr.us-east-2.amazonaws.com/lh-server:latest
docker tag lh-worker:latest 834373697194.dkr.ecr.us-east-2.amazonaws.com/lh-worker:latest
docker tag lh-scheduler:latest 834373697194.dkr.ecr.us-east-2.amazonaws.com/lh-scheduler:latest

docker push 834373697194.dkr.ecr.us-east-2.amazonaws.com/lh-server:latest
docker push 834373697194.dkr.ecr.us-east-2.amazonaws.com/lh-worker:latest
docker push 834373697194.dkr.ecr.us-east-2.amazonaws.com/lh-scheduler:latest


# wait

#kubectl scale deployment lh-scheduler --replicas=3 --namespace default
#kubectl scale deployment lh-server --replicas=3 --namespace default

#sleep 5
#kubectl get po
#sleep 3

#kubectl port-forward svc/lh-server 5000:5000 -ndefault &

#kubectl get po -w --namespace default
