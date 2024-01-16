#!/bin/bash

set -e

if [ "$1" = 'dashboard' ]; then
    shift

    # Env variables to translate here
    # LHD_API_HOST
    # LHD_API_PORT
    # LHD_OAUTH_ENABLED
    # LHD_OAUTH_CLIENT_ID
    # LHD_OAUTH_CLIENT_SECRET
    # LHD_OAUTH_SERVER_URL
    # LHD_OAUTH_CALLBACK_URL
    # LHD_OAUTH_ENCRYPT_SECRET

    if [ -z "${LHD_API_HOST}" ]; then
        echo "Provide the LHD_API_HOST env variable"
        exit 1
    fi

    if [ -z "${LHD_API_PORT}" ]; then
        echo "Provide the LHD_API_PORT env variable"
        exit 1
    fi

    export API_URL="${LHD_API_HOST}:${LHD_API_PORT}"
    export LHD_OAUTH_ENABLED=${LHD_OAUTH_ENABLED:-"false"}

    if [ "${LHD_OAUTH_ENABLED}" == "true" ]; then
        if [ -z "${LHD_OAUTH_CLIENT_ID}" ] ||  [ -z "${LHD_OAUTH_CLIENT_SECRET}" ] || [ -z "${LHD_OAUTH_SERVER_URL}" ] || [ -z "${LHD_OAUTH_ENCRYPT_SECRET}" ] || [ -z "${LHD_OAUTH_CALLBACK_URL}" ]; then
            echo "Authentication is enabled and some configuration were not provided. Please refer to our documentation https://github.com/littlehorse-enterprises/littlehorse/blob/master/docs/DASHBOARD_CONFIGURATIONS.md"
            exit 1
        fi

      export NEXTAUTH_URL=${LHD_OAUTH_CALLBACK_URL}
      export KEYCLOAK_CLIENT_ID=${LHD_OAUTH_CLIENT_ID}
      export KEYCLOAK_CLIENT_SECRET=${LHD_OAUTH_CLIENT_SECRET}
      export KEYCLOAK_ISSUER_URI=${LHD_OAUTH_SERVER_URL}
      export AUTH_SECRET=${LHD_OAUTH_ENCRYPT_SECRET}
    else
      export AUTH_SECRET=$(uuidgen)
    fi

    node apps/web/server.js
fi

exec "$@"
