#!/bin/sh

set -e

# Env variables to translate here
# LHD_OAUTH_ENABLED
# LHD_OAUTH_CLIENT_ID
# LHD_OAUTH_CLIENT_SECRET
# LHD_OAUTH_ISSUER_URI
# LHD_OAUTH_CALLBACK_URL
# LHD_OAUTH_CALLBACK_URL_INTERNAL
# LHD_OAUTH_ENCRYPT_SECRET

if [ -z "${LHC_API_HOST}" ]; then
    echo "Provide the LHC_API_HOST env variable"
    exit 1
fi

if [ -z "${LHC_API_PORT}" ]; then
    echo "Provide the LHC_API_PORT env variable"
    exit 1
fi

export LHD_OAUTH_ENABLED=${LHD_OAUTH_ENABLED:-"false"}

if [ "${LHD_OAUTH_ENABLED}" == "true" ]; then
    if [ -z "${LHD_OAUTH_CLIENT_ID}" ] ||  [ -z "${LHD_OAUTH_CLIENT_SECRET}" ] || [ -z "${LHD_OAUTH_ISSUER_URI}" ] || [ -z "${LHD_OAUTH_ENCRYPT_SECRET}" ] || [ -z "${LHD_OAUTH_CALLBACK_URL}" ]; then
        echo "Authentication is enabled and some configuration were not provided. Please refer to our documentation https://github.com/littlehorse-enterprises/littlehorse/blob/master/docs/DASHBOARD_CONFIGURATIONS.md"
        exit 1
    fi

  export NEXTAUTH_URL=${LHD_OAUTH_CALLBACK_URL}

  if [ -n "${LHD_OAUTH_CALLBACK_URL_INTERNAL}" ]; then
      export NEXTAUTH_URL_INTERNAL=${LHD_OAUTH_CALLBACK_URL_INTERNAL}
  fi

  export KEYCLOAK_CLIENT_ID=${LHD_OAUTH_CLIENT_ID}
  export KEYCLOAK_CLIENT_SECRET=${LHD_OAUTH_CLIENT_SECRET}
  export KEYCLOAK_ISSUER_URI=${LHD_OAUTH_ISSUER_URI}
  export NEXTAUTH_SECRET=${LHD_OAUTH_ENCRYPT_SECRET}
else
  export NEXTAUTH_SECRET=$(uuidgen)
fi

/entrypoint.sh
