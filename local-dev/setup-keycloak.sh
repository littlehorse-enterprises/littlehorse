#!/bin/bash

set -e

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
cd "$SCRIPT_DIR"

REALM_NAME="lh"
SERVER_CLIENT_ID="server"
SERVER_CLIENT_SECRET="3bdca420cf6c48e2aa4f56d46d6327e0"
WORKER_CLIENT_ID="worker"
WORKER_CLIENT_SECRET="40317ab43bd34a9e93499c7ea03ad398"
CLI_CLIENT_ID="lhctl"
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
    clientId="$SERVER_CLIENT_ID" \
    id="$SERVER_CLIENT_ID" \
    secret="$SERVER_CLIENT_SECRET" \
    serviceAccountsEnabled:=true \
    directAccessGrantsEnabled:=true \
    publicClient:=false

http -b -A bearer -a "$KEYCLOAK_ADMIN_ACCESS_TOKEN" "http://localhost:${KEYCLOAK_PORT}/admin/realms/${REALM_NAME}/clients" \
    protocol=openid-connect \
    clientId="$WORKER_CLIENT_ID" \
    id="$WORKER_CLIENT_ID" \
    secret="$WORKER_CLIENT_SECRET" \
    serviceAccountsEnabled:=true \
    directAccessGrantsEnabled:=true \
    publicClient:=false

http -b -A bearer -a "$KEYCLOAK_ADMIN_ACCESS_TOKEN" "http://localhost:${KEYCLOAK_PORT}/admin/realms/${REALM_NAME}/clients" \
    protocol=openid-connect \
    clientId="$CLI_CLIENT_ID" \
    id="$CLI_CLIENT_ID" \
    directAccessGrantsEnabled:=false \
    publicClient:=true \
    redirectUris:='["http://127.0.0.1:25242/callback"]'
