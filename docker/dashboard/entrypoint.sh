#!/bin/sh

set -e

# Env variables to translate here
# LHD_OAUTH_CLIENT_ID
# LHD_OAUTH_CLIENT_SECRET
# LHD_OAUTH_ISSUER_URI
# LHD_OAUTH_ENCRYPT_SECRET

if [ -z "${LHC_API_HOST}" ]; then
    echo "Provide the LHC_API_HOST env variable"
    exit 1
fi

if [ -z "${LHC_API_PORT}" ]; then
    echo "Provide the LHC_API_PORT env variable"
    exit 1
fi

# Check if any OAuth variable is set
if [ -n "${LHD_OAUTH_CLIENT_ID}" ] || [ -n "${LHD_OAUTH_CLIENT_SECRET}" ] || [ -n "${LHD_OAUTH_ISSUER_URI}" ] || [ -n "${LHD_OAUTH_ENCRYPT_SECRET}" ]; then
    # If any is set, check that all are set
    missing_vars=""
    
    if [ -z "${LHD_OAUTH_CLIENT_ID}" ]; then
        missing_vars="${missing_vars}LHD_OAUTH_CLIENT_ID "
    fi
    
    if [ -z "${LHD_OAUTH_CLIENT_SECRET}" ]; then
        missing_vars="${missing_vars}LHD_OAUTH_CLIENT_SECRET "
    fi
    
    if [ -z "${LHD_OAUTH_ISSUER_URI}" ]; then
        missing_vars="${missing_vars}LHD_OAUTH_ISSUER_URI "
    fi
    
    if [ -z "${LHD_OAUTH_ENCRYPT_SECRET}" ]; then
        missing_vars="${missing_vars}LHD_OAUTH_ENCRYPT_SECRET "
    fi
    
    if [ -n "${missing_vars}" ]; then
        echo "Authentication is enabled but the following OAuth configuration variables are missing: ${missing_vars}"
        echo "Please refer to our documentation https://littlehorse.io/docs/server/operations/dashboard-configuration"
        exit 1
    fi
fi


export AUTH_SECRET=${LHD_OAUTH_ENCRYPT_SECRET}
export AUTH_TRUST_HOST=true # https://authjs.dev/getting-started/deployment#docker

export KEYCLOAK_CLIENT_ID=${LHD_OAUTH_CLIENT_ID}
export KEYCLOAK_CLIENT_SECRET=${LHD_OAUTH_CLIENT_SECRET}
export KEYCLOAK_ISSUER_URI=${LHD_OAUTH_ISSUER_URI}


/entrypoint.sh
