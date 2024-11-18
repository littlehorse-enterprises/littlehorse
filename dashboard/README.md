# LH Dashboard

## Requirements

- It needs Node 20. You can install `NVM` and run on the dashboard folder:

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

## Start the Dashboard with Docker

The Dashboard docker image is under `docker/dashboard`, in order to run it please do the following:

1. Go under the `dashboard` directory, execute: `npm install`

2. Build the docker image

```sh
./local-dev/build.sh --dashboard
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

## Running local with Oauth

Modify the .env file using the following variables

```
LHC_API_HOST=localhost
LHC_API_PORT=2023
LHC_API_PROTOCOL=TLS
LHD_OAUTH_ENABLED=true
LHC_CA_CERT=path-to-ca-cert
NEXTAUTH_SECRET=any random string
KEYCLOAK_CLIENT_ID=dashboard
KEYCLOAK_CLIENT_SECRET=74b897a0b5804ad3879b2117e1d51015
KEYCLOAK_ISSUER_URI=http://localhost:8888/realms/lh
```
