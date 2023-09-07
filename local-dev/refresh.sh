#!/bin/bash

set -e

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
WORK_DIR=$SCRIPT_DIR

kafka_topics_sh="docker exec lh-server-kafka bash /opt/bitnami/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092"

TOPICS=$($kafka_topics_sh --list)

if [ -n "$TOPICS" ]; then
    echo "Topics to be deleted:"
    echo "$TOPICS"
    $kafka_topics_sh --delete --topic ".*"
fi

rm -rf /tmp/kafkaState*
