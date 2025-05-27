#!/bin/bash

set -e

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
WORK_DIR=$SCRIPT_DIR

DOCKER_COMPOSE_KAFKA=$(cat <<EOF
services:
  kafka:
    ports:
      - "9092:9092"
    container_name: lh-server-kafka
    image: apache/kafka:4.0.0
EOF
)

DOCKER_COMPOSE_KEYCLOAK=$(cat <<EOF
services:
  keycloak:
    ports:
      - "8888:8888"
    container_name: lh-server-auth
    image: quay.io/keycloak/keycloak:23.0
    command:
      - start-dev
      - --http-port=8888
    environment:
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin
EOF
)

if [ -n "$1" ]; then
    command="$1"
fi

setup_keycloak() {
    echo "Setting Up Keycloak"
    docker compose --file /dev/stdin \
        --project-directory "$WORK_DIR" \
        --project-name lh-server-auth-local-dev \
        up -d <<EOF
${DOCKER_COMPOSE_KEYCLOAK}
EOF

    if ! command -v http &>/dev/null; then
        echo "'http' command not found. Install httpie https://httpie.io/cli"
        exit 1
    fi

    if ! command -v jq &>/dev/null; then
        echo "'jq' command not found. Install jq https://jqlang.github.io/jq/"
        exit 1
    fi

    # clients
    CANARY_CLIENT_ID="canary"
    CANARY_CLIENT_SECRET="8b629ff9b2684014b8c62d4da8cc371e"
    DASHBOARD_CLIENT_ID="dashboard"
    DASHBOARD_CLIENT_SECRET="74b897a0b5804ad3879b2117e1d51015"
    SERVER_CLIENT_ID="server"
    SERVER_CLIENT_SECRET="3bdca420cf6c48e2aa4f56d46d6327e0"
    WORKER_CLIENT_ID="worker"
    WORKER_CLIENT_SECRET="40317ab43bd34a9e93499c7ea03ad398"
    CLI_CLIENT_ID="lhctl"

    # server
    REALM_NAME="lh"
    KEYCLOAK_ADMIN="admin"
    KEYCLOAK_ADMIN_PASSWORD="admin"
    KEYCLOAK_PORT="8888"

    # users
    USER_NAME="user"
    USER_PASSWORD="password"

    while ! curl --silent --fail --output /dev/null "http://localhost:${KEYCLOAK_PORT}"; do
        echo "Waiting for keycloak"
        sleep 5
    done

    KEYCLOAK_ADMIN_ACCESS_TOKEN=$(http --form "http://localhost:${KEYCLOAK_PORT}/realms/master/protocol/openid-connect/token" \
        client_id=admin-cli \
        username="$KEYCLOAK_ADMIN" \
        password="$KEYCLOAK_ADMIN_PASSWORD" \
        grant_type=password | jq -r ".access_token")

    http -q -A bearer -a "$KEYCLOAK_ADMIN_ACCESS_TOKEN" POST "http://localhost:${KEYCLOAK_PORT}/admin/realms" \
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

    echo "Realm '${REALM_NAME}' created"

    create_keycloak_client $SERVER_CLIENT_ID $SERVER_CLIENT_SECRET
    create_keycloak_client $WORKER_CLIENT_ID $WORKER_CLIENT_SECRET
    create_keycloak_client $CANARY_CLIENT_ID $CANARY_CLIENT_SECRET

    http -q -A bearer -a "$KEYCLOAK_ADMIN_ACCESS_TOKEN" POST "http://localhost:${KEYCLOAK_PORT}/admin/realms/${REALM_NAME}/clients" \
        protocol=openid-connect \
        clientId="$CLI_CLIENT_ID" \
        id="$CLI_CLIENT_ID" \
        directAccessGrantsEnabled:=false \
        publicClient:=true \
        redirectUris:='["http://127.0.0.1:25242/callback"]'

    http -q -A bearer -a "$KEYCLOAK_ADMIN_ACCESS_TOKEN" POST "http://localhost:${KEYCLOAK_PORT}/admin/realms/${REALM_NAME}/clients" \
            protocol=openid-connect \
            clientId="$DASHBOARD_CLIENT_ID" \
            id="$DASHBOARD_CLIENT_ID" \
            secret="$DASHBOARD_CLIENT_SECRET" \
            directAccessGrantsEnabled:=true \
            standardFlowEnabled:=true \
            implicitFlowEnabled:=false \
            publicClient:=false \
            authorizationServicesEnabled:=false \
            surrogateAuthRequired:=false \
            frontchannelLogout:=true \
            redirectUris:='["http://localhost:3000/*"]'

    http -b -A bearer -a "${KEYCLOAK_ADMIN_ACCESS_TOKEN}" POST "http://localhost:${KEYCLOAK_PORT}/admin/realms/${REALM_NAME}/users" \
            emailVerified:=true \
            username="$USER_NAME" \
            email="user@littlehorse.io" \
            firstName="local" \
            lastName="dev" \
            enabled:=true \
            credentials[0][type]="password" \
            credentials[0][value]="$USER_PASSWORD" \
            credentials[0][temporary]:=false

    echo "Client '${CLI_CLIENT_ID}' created"

    echo "Keycloak: http://localhost:${KEYCLOAK_PORT}"
}

create_keycloak_client() {
    http -q -A bearer -a "$KEYCLOAK_ADMIN_ACCESS_TOKEN" POST "http://localhost:${KEYCLOAK_PORT}/admin/realms/${REALM_NAME}/clients" \
        protocol=openid-connect \
        clientId="$1" \
        id="$1" \
        secret="$2" \
        serviceAccountsEnabled:=true \
        directAccessGrantsEnabled:=true \
        publicClient:=false
    echo "Client '${1}' created"
}

setup_kafka() {
    echo "Setting Up Kafka"
    docker compose --file /dev/stdin \
        --project-directory "$WORK_DIR" \
        --project-name lh-server-kafka-local-dev \
        up -d <<EOF
${DOCKER_COMPOSE_KAFKA}
EOF
    echo "Kafka bootstrap: localhost:9092"
}

clean() {
    echo "Cleaning"
    docker compose --project-name lh-server-kafka-local-dev down -v
    docker compose --project-name lh-server-auth-local-dev down -v
    rm -rf /tmp/kafka*
    rm -rf /tmp/canary*
    cd "$SCRIPT_DIR/.."
    ./gradlew -q clean
}

refresh() {
    echo "Cleaning"
    docker compose --project-name lh-server-kafka-local-dev down -v
    rm -rf /tmp/kafka*
    rm -rf /tmp/canary*
    cd "$SCRIPT_DIR/.."
    ./gradlew -q clean
}

keycloak=false
clean=false
refresh=false

while [[ $# -gt 0 ]]; do
  case "$1" in
    --clean)
      clean=true
      ;;
    --refresh)
      refresh=true
      ;;
    --keycloak)
      keycloak=true
      ;;
    *)
      echo "Unknown argument: $1"
      exit 1
      ;;
  esac
  shift
done

if [ ${clean} = true ]; then
    clean
    exit 0
fi

if [ ${refresh} = true ]; then
    refresh
fi

if [ ${keycloak} = true ]; then
    setup_keycloak
fi

setup_kafka
