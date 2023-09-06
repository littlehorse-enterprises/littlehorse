#!/bin/bash

set -e

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
WORK_DIR=$(cd "$SCRIPT_DIR/../sdk-python" && pwd)

# change path
cd "$WORK_DIR"

poetry run python -m scripts.bump "$@"
