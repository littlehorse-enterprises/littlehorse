#!/bin/bash

set -e

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
cd "$SCRIPT_DIR"

if [ -z "$1" ]; then
    echo "No client id supplied."
    exit 1
fi

if [ -z "$2" ]; then
    echo "No client secret supplied."
    exit 1
fi

REALM_NAME="lh"
CLIENT_ID="$1"
CLIENT_SECRET="$2"
KEYCLOAK_ADMIN="admin"
KEYCLOAK_ADMIN_PASSWORD="admin"
KEYCLOAK_PORT="8888"

KEYCLOAK_ADMIN_ACCESS_TOKEN=$(http -b --form "http://localhost:${KEYCLOAK_PORT}/realms/master/protocol/openid-connect/token" \
                                client_id=admin-cli \
                                username="$KEYCLOAK_ADMIN" \
                                password="$KEYCLOAK_ADMIN_PASSWORD" \
                                grant_type=password | jq -r ".access_token")

http -b -A bearer -a "$KEYCLOAK_ADMIN_ACCESS_TOKEN" "http://localhost:${KEYCLOAK_PORT}/admin/realms" \
    id="$REALM_NAME" \
    realm="$REALM_NAME" \
    displayName="$REALM_NAME" \
    sslRequired=external \
    enabled:=true \
    registrationAllowed:=false \
    loginWithEmailAllowed:=true \
    duplicateEmailsAllowed:=false \
    resetPasswordAllowed:=false \
    editUsernameAllowed:=false \
    bruteForceProtected:=true

http -b -A bearer -a "$KEYCLOAK_ADMIN_ACCESS_TOKEN" "http://localhost:${KEYCLOAK_PORT}/admin/realms/${REALM_NAME}/clients" \
    protocol=openid-connect \
    clientId="$CLIENT_ID" \
    id="$CLIENT_ID" \
    secret="$CLIENT_SECRET" \
    serviceAccountsEnabled:=true \
    directAccessGrantsEnabled:=true \
    publicClient:=false
