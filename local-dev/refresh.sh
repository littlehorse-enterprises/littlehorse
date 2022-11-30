#!/bin/bash
set -x

kill -9 $(ps aux | grep io.littlehorse | grep -v 'grep' | cut -d ' ' -f3)

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd $SCRIPT_DIR

docker exec lh-kafka bash -c '
export TOPICS=$(/opt/bitnami/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list)
if [ -z "$TOPICS" ]
then
    echo "Nothing to do!"
else
    /opt/bitnami/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic $(echo $TOPICS | tr " " "|")
    echo "Deleted topics!!"
fi
' &

# $SCRIPT_DIR/cleanup.sh
# $SCRIPT_DIR/setup.sh

rm -r /tmp/kafkaState

wait
