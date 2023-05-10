#!/bin/bash

set -e

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
CONFIG_NAME="server-1"

if [ -n "$1" ]
then
  CONFIG_NAME="$1"
fi

CONFIG_PATH="${SCRIPT_DIR}/${CONFIG_NAME}.config"

if [ ! -f "$CONFIG_PATH" ]
then
    echo "$CONFIG_PATH does not exist."
    exit 1
fi

"$SCRIPT_DIR/build.sh"
echo "Host config file: $CONFIG_PATH"
docker run --rm -it --network host --volume "$CONFIG_PATH":/lh/server.config littlehorse.io/littlehorse-server:latest
