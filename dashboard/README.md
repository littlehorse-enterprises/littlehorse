# LH Dashboard

## Table of Contents

- [Requirements](#requirements)
- [Environment Variables](#environment-variables)
- [Development](#development)
  - [LH Server without authentication](#lh-server-without-authentication)
  - [LH Server with authentication](#lh-server-with-authentication)
- [Start the Dashboard with Docker](#start-the-dashboard-with-docker)
  - [Authentication Disabled](#authentication-disabled)
  - [Authentication Enabled](#authentication-enabled)
- [SSL termination](#ssl-termination)
- [Running local with Oauth](#running-local-with-oauth)
  - [Running local LH Server with OAuth](#running-local-lh-server-with-oauth)
- [Linting](#linting)
  - [Configuring your IDE to have Linter Live Feedback](#configuring-your-ide-to-have-linter-live-feedback)
    - [Intellij](#intellij)
    - [VSCode](#vscode)
  - [Linter configuration](#linter-configuration)
- [Testing](#testing)
  - [Environment variables](#environment-variables-1)

## Requirements

- This project requires Node v20. You can install [`nvm`](https://github.com/nvm-sh/nvm/blob/master/README.md#intro) and run the following command in the dashboard directory:

```shell
nvm use
```

## Environment Variables

If running the app without Docker, you need to fill in the environment variables in the `.env` file inside `apps/web`. The `.env` file in the root folder is not being read by the app.

- `LHC_API_HOST` littlehorse hostname
- `LHC_API_PORT` littlehorse port
- `LHC_CA_CERT` To specify the path to the self signed certificate that the dashboard needs to connect to a LittleHorse server configured to work with OAuth.
- `LHC_API_PROTOCOL` specify the communication protocol PLAINTEXT or TLS. If not provided it defaults to PLAINTEXT.
- `LHD_OAUTH_ENABLED` enable oauth authentication
- `LHD_OAUTH_ENCRYPT_SECRET` random string that will be used to encrypt the secrets and also the JWT token
- `LHD_OAUTH_CALLBACK_URL` the url (domain) in which the dashboard will run (required for some authentication methods). For your local you can use: `http:/localhost:3001/`
- `LHD_OAUTH_CALLBACK_URL_INTERNAL` the internal url (domain) in which the dashboard server will run (required for some authentication methods). Should only be set when the `LHD_OAUTH_CALLBACK_URL` cannot be reached by the dashboard server.
- `LHD_OAUTH_CLIENT_ID` the client id configured in keycloack
- `LHD_OAUTH_CLIENT_SECRET` the client secret configured in keycloack
- `LHD_OAUTH_ISSUER_URI` the keycloack

## Development

Create a copy of `.env-sample` as `.env-local` and modify it accordingly to your littlehorse-server configuration.

Then simply run

```shell
npm install
npm run dev
```

The application will start with watch mode on [http://localhost:3000](http://localhost:3000)

### LH Server without authentication

If you don't have a lh-server running, you can use this command:

```shell
docker run --rm -d -p 2023:2023 ghcr.io/littlehorse-enterprises/littlehorse/lh-standalone:master
```

Asuming your lh-server is running on `localhost:2023` create a `.env.local` file with

```env
LHC_API_HOST=localhost
LHC_API_PORT=2023
```

### LH Server with authentication

- The LH Dashboard can use Keycloack provider as an SSO mechanism for a user to login into the dashboard and use it. For that to work you have to enable the toggle `LHD_OAUTH_ENABLED` in the needed environments.

- You need to your LittleHorse server running in OAuth mode for this feature to work correclty.

- For the needed Environment Variables please refer to the corresponding section in this README.

- [Here a detail of the implemented authentication flow for this project](https://link.excalidraw.com/readonly/5sxfddEgSEFTEQLF3WAG)

## Start the Dashboard with Docker

The Dashboard docker image is under `docker/dashboard`, in order to run it please do the following:

1. Go under the `dashboard` directory, execute: `npm install`

2. Build the docker image

```sh
.././local-dev/build.sh --dashboard
```

Execute either of the following:

### Authentication Disabled:

```bash
docker run --rm \
  -p 3000:3000 \
  --env LHC_API_HOST='localhost' \
  --env LHC_API_PORT='2023' \
  --network host \
  ghcr.io/littlehorse-enterprises/littlehorse/lh-dashboard:master
```

### Authentication Enabled:

```bash
docker run --rm \
  --env LHC_API_HOST='localhost' \
  --env LHC_API_PORT='2023' \
  --env LHD_OAUTH_ENABLED='true' \
  --env LHD_OAUTH_CLIENT_ID='{a-client-id}' \
  --env LHD_OAUTH_CLIENT_SECRET='{a-client-secret}' \
  --env LHD_OAUTH_ISSUER_URI='{https://keycloack-env}/realms/lh' \
  --env LHD_OAUTH_CALLBACK_URL='localhost:3000' \
  --env LHD_OAUTH_ENCRYPT_SECRET='{a-secret-to-encrypt}' \
  --network host \
  ghcr.io/littlehorse-enterprises/littlehorse/lh-dashboard:master
```

## SSL termination

Assuming you have a folder `./ssl` containing `tls.crt` and `tls.key`

```bash
docker run --rm \
  --env SSL='true' \
  --env LHC_API_HOST='localhost' \
  --env LHC_API_PORT='2023' \
  --network host \
  -v ./ssl:/ssl \
  ghcr.io/littlehorse-enterprises/littlehorse/lh-dashboard:master
```

## Running local LH Server with Oauth

1. Generate certificates:

```shell
.././local-dev/issue-certificates.sh
```

2. Configure your Dashboard `.env.local` file:

_Replace `path-to-ca-cert` with your path to this repo's `./local-dev/certs/ca/ca.crt`_

```env
LHC_API_HOST=localhost
LHC_API_PORT=2023
LHC_API_PROTOCOL=TLS
LHD_OAUTH_ENABLED=true
LHC_CA_CERT=path-to-ca-cert
NEXTAUTH_SECRET=anyrandomstring
KEYCLOAK_CLIENT_ID=dashboard
KEYCLOAK_CLIENT_SECRET=74b897a0b5804ad3879b2117e1d51015
KEYCLOAK_ISSUER_URI=http://localhost:8888/realms/lh
NEXTAUTH_URL=http://localhost:3000
```

3. Setup Keycloak:

```shell
.././local-dev/setup.sh --keycloak
```

4. Run the LH server with OAuth:

```shell
.././local-dev/do-server.sh oauth
```

5. (Optional) Configure `lhctl` by setting `~/.config/littlehorse.config`:

_Replace `path-to-ca-cert` with your path to this repo's `./local-dev/certs/ca/ca.crt`_

```
LHC_API_HOST=localhost
LHC_API_PORT=2023
LHC_API_PROTOCOL=TLS
LHC_OAUTH_CLIENT_ID=lhctl
LHC_OAUTH_SERVER_URL=http://localhost:8888/realms/lh
LHC_CA_CERT=/<PATH TO YOUR LH REPO>/local-dev/certs/ca/ca.crt
```

**Login Credentials:**

- Keycloak admin (localhost:8888): `admin` / `admin`
- Dashboard user: `user` / `password`

## Linting

We are using ESLint as the linter for project. Given that we have a mono-repo structure we have the main command for the linter in the root package.json file.

You can run the linter by:

```
npm run lint
```

If you wanna ESLint to try to fix the issues automatically, run:

```
npm run lint:fix
```

### Configuring your IDE to have Linter Live Feedback

#### Intellij

- Go to the ESLint configuration
- Choose Manual Configuration
- For ESLint package pick the one in the root folder: `<your workskpace dir>/lh-dashboard/node_modules/eslint`
- For Working directories use:`<your workskpace dir>/lh-dashboard/apps/web;<your workskpace dir>/lh-dashboard/packages/ui`
- For the Configuration file use: `<your workskpace dir>/lh-dashboard/.eslintrc.js`
- With the above config you will receive live feedback in intellij on regards to the linting rules that are configured
- While configuring the ESLint plugin you can enable the option: `run eslint --fix on save`
- The reFormat code shortcut will _NOT_ use the ESLint rules, you should do:`Fix ESLint problems` by opening the IntelliJ`Actions` menu.

#### VSCode

- Install the extension called `ESLint'
- Configure the VSCode IDE with the following options:

```
{
    "files.autoSave": "afterDelay",
    "eslint.format.enable": true,
    "editor.codeActionsOnSave": {
      "source.fixAll.eslint": true
    },
    "eslint.workingDirectories": ["<your workskpace dir>/lh-dashboard/apps/web",
      "<your workskpace dir>/lh-dashboard/packages/ui"]
}
```

- On the actions menu choose: `Developer: Reload Window`
- You should start seeing the linter errors in the code

### Linter configuration

The configuration for the entire mono-repo is under `/packages/eslint-config-custom/next.js`. On the mentioned file you can configure the rules that apply
for the entire `lh-dashboard` mono-repo.

## Testing

Jest is being used as the testing framework, any file that has the pattern `*.test.*` in its name is considered a test and will be executed by Jest.
To run the tests please execute:

```
npm run test
```

If you wanna watch your tests while developing execute:

```
npm run test --watch
```

### Environment variables

You need to create a `env.test.local` file to contain any env variable you might for your tests.
