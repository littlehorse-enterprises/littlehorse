#!/bin/bash

set -e

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
LH_SERVER_WORK_DIR=$(cd "$SCRIPT_DIR/../docker/server" && pwd)
LH_STANDALONE_WORK_DIR=$(cd "$SCRIPT_DIR/../docker/standalone" && pwd)
CONTEXT_DIR=$(cd "$SCRIPT_DIR/.." && pwd)

cd "$CONTEXT_DIR"

CONTAINER=lh-quickbuilder

if [ "$1" == "--quick" ]; then
    # The quick build compiles the jar using gradle on the host machine, which
    # enables usage of the gradle cache. This is much faster than building from
    # scratch in a fresh container, and is suitable for local development.
    echo "Building server image using host machine's gradle cache"
    cd ${CONTEXT_DIR}
    gradle server:shadowJar -x test

    docker build -f ${SCRIPT_DIR}/util/Dockerfile.server-quick \
        --tag littlehorse/littlehorse-server:latest ${CONTEXT_DIR}
else
    # Build from scratch using the same Dockerfiles used in our CI/CD
    # pipeline. Also, we build the standalone image as well.
    echo "Building server and standalone images from scratch inside Docker environment"

    echo "Building server"
    docker build --file ${LH_SERVER_WORK_DIR}/Dockerfile \
        --tag littlehorse/littlehorse-server:latest ${CONTEXT_DIR}

    echo "Building standalone"
    docker build --file ${LH_STANDALONE_WORK_DIR}/Dockerfile \
        --tag littlehorse/littlehorse-standalone:latest ${CONTEXT_DIR}
fi
