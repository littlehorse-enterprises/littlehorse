#!/bin/bash
set -x

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd $SCRIPT_DIR

docker exec broker bash -c '
export TOPICS=$(/bin/kafka-topics --bootstrap-server localhost:9092 --list)
if [ -z "$TOPICS" ]
then
    echo "Nothing to do!"
else
    /bin/kafka-topics --bootstrap-server localhost:9092 --delete --topic $(echo $TOPICS | tr " " "|")
    echo "Deleted topics!!"
fi
' &

# $SCRIPT_DIR/cleanup.sh
# $SCRIPT_DIR/setup.sh
docker stop postgres
docker rm postgres
docker-compose up -d

rm -r /tmp/kafkaState

wait