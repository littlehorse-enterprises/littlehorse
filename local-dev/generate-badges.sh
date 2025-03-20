#!/bin/bash

set -e

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
WORK_DIR=$(cd "$SCRIPT_DIR/.." && pwd)
DOCKER_IMAGE_NAME="lh-generate-badges"

cd "$WORK_DIR"

verify_dependencies() {
    if
        ! command -v docker >/dev/null 2>&1
    then
        echo "'docker' command not found."
        exit 1
    fi
}

verify_folder() {
    mkdir -p img/badges
}

build() {
    docker build -q -t $DOCKER_IMAGE_NAME -f - . <<EOF

FROM node:latest
WORKDIR /badge-maker
RUN npm install badge-maker
RUN cat <<EOT >> badge-generator.js

const { makeBadge, ValidationError } = require('badge-maker')
const fs = require('fs');

function base64_encode(file) {
    var bitmap = fs.readFileSync(file);
    return new Buffer.from(bitmap).toString('base64');
}

const logo = \\\`data:image/svg+xml;base64,\\\${base64_encode('img/white-logo.svg')}\\\`

const badges = [
{
    name: 'black',
    format: {
        message: 'LittleHorse',
        color: 'black',
        logoBase64: logo
    }
},
{
    name: 'gray',
    format: {
        message: 'LittleHorse',
        color: 'gray',
        logoBase64: logo
    }
}
]

badges.forEach((badge) => {
    try {
        fs.writeFileSync(\\\`img/badges/\\\${badge.name}.svg\\\`, makeBadge(badge.format));
    } catch (err) {
        console.error(err);
    }
});

EOT
EOF
}

run() {
    docker run --rm -v $(pwd)/img:/badge-maker/img $DOCKER_IMAGE_NAME badge-generator.js
}

verify_dependencies
verify_folder
build
run
