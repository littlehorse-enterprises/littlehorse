#!/bin/bash

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd ${SCRIPT_DIR}

set -e

docker-compose -p docker-test-harness -f docker-compose.pg.yml up -d

# echo "If you just edited db_schema.py, I hope you already ran 'alembic revision --autogenerate'"

sleep 5

cd ${SCRIPT_DIR}/..

alembic revision --autogenerate
echo "Installing db schema now..."
alembic upgrade head
