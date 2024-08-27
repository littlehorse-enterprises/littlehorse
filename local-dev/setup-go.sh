#!/bin/bash

# get current path
SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
CONTEXT_DIR=$(cd "$SCRIPT_DIR/.." && pwd)

set -e

cd $CONTEXT_DIR/lhctl

# if go.work file exists, remove it
if [ -f go.work ]; then
    rm go.work
fi
go work init

go work use ../sdk-go
go work use .
