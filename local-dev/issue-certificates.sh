#!/bin/bash

set -e

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
cd "$SCRIPT_DIR"

echo "Cleaning old certs"
echo

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
    -addext "subjectAltName = DNS:localhost"

echo

########################################################
# Server Certs
########################################################
echo "Creating Server Private Key"

# Create the Server Private Key and the Certificate Signing Request
openssl req -out "$SERVER_PATH/server.csr" -newkey rsa:2048 -nodes \
    -keyout "$SERVER_PATH/server.key" \
    -subj "/CN=localhost/O=example server" \
    -addext "subjectAltName = DNS:localhost"

echo
echo "Signing Server Certificate"
echo
# Create the Certificate (needs acccess to CA Secret + Cert)
openssl x509 -req -sha256 -days 3650 \
    -CA "$CA_PATH/ca.crt" \
    -CAkey "$CA_PATH/ca.key" \
    -in "$SERVER_PATH/server.csr" \
    -out "$SERVER_PATH/server.crt" \
    -set_serial 0 \
    -extfile <(printf "subjectAltName=DNS:localhost")

echo
echo

########################################################
# Client Certs
########################################################

echo "Creating client private key"

openssl req -newkey rsa:2048 -nodes \
    -out "$CLIENT_PATH/client.csr" \
    -keyout "$CLIENT_PATH/client.key" \
    -subj "/CN=localhost/O=client organization" \
    -addext "subjectAltName = DNS:localhost"
echo

echo "Signing Client Certificate"
openssl x509 -req -sha256 -days 3650 \
    -CA "$CA_PATH/ca.crt" \
    -CAkey "$CA_PATH/ca.key" \
    -in "$CLIENT_PATH/client.csr" \
    -out "$CLIENT_PATH/client.crt" \
    -set_serial 1
echo

MESSAGE="
Add this to your ~/.config/littlehorse.config file
LHC_CLIENT_CERT=$(pwd)/$CLIENT_PATH/client.crt
LHC_CLIENT_KEY=$(pwd)/$CLIENT_PATH/client.key
LHC_CA_CERT=$(pwd)/$CA_PATH/ca.crt
"

echo "$MESSAGE"
