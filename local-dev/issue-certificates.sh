#!/bin/bash

set -e

if ! command -v openssl &> /dev/null; then
    echo "openssl command could not be found, install https://www.openssl.org/"
    exit 1
fi

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
cd "$SCRIPT_DIR"

CA_PATH=certs/ca
CLIENT_PATH=certs/client
SERVER_PATH=certs/server

rm -rf certs
mkdir -p "$CA_PATH"
mkdir "$CLIENT_PATH"
mkdir "$SERVER_PATH"

########################################################
# CA Cert
########################################################
echo "Creating Root CA"
openssl req -x509 -sha256 -nodes \
    -days 3650 -newkey rsa:2048 \
    -subj '/O=lh server/CN=localhost' \
    -keyout "$CA_PATH/ca.key" \
    -out "$CA_PATH/ca.crt" \
    -addext "subjectAltName = DNS:localhost" > /dev/null 2>&1
########################################################
# Server Certs
########################################################
echo "Creating Server Certificates"
openssl req -out "$SERVER_PATH/server.csr" -newkey rsa:2048 -nodes \
    -keyout "$SERVER_PATH/server.key" \
    -subj "/CN=localhost/O=example server" \
    -addext "subjectAltName = DNS:localhost" > /dev/null 2>&1
openssl x509 -req -sha256 -days 3650 \
    -CA "$CA_PATH/ca.crt" \
    -CAkey "$CA_PATH/ca.key" \
    -in "$SERVER_PATH/server.csr" \
    -out "$SERVER_PATH/server.crt" \
    -set_serial 0 \
    -extfile <(printf "subjectAltName=DNS:localhost") > /dev/null 2>&1
########################################################
# Client Certs
########################################################
echo "Creating Client Certificates"
openssl req -newkey rsa:2048 -nodes \
    -out "$CLIENT_PATH/client.csr" \
    -keyout "$CLIENT_PATH/client.key" \
    -subj "/CN=obiwan/O=client organization" \
    -addext "subjectAltName = DNS:localhost" > /dev/null 2>&1
openssl x509 -req -sha256 -days 3650 \
    -CA "$CA_PATH/ca.crt" \
    -CAkey "$CA_PATH/ca.key" \
    -in "$CLIENT_PATH/client.csr" \
    -out "$CLIENT_PATH/client.crt" \
    -set_serial 1 > /dev/null 2>&1
