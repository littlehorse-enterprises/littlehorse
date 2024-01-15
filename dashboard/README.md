# LittleHorse Dashboard

This directory contains the code for the LittleHorse Dashhboard. The LH Dashboard is an administrative tool that allows you to easily visualize what's going on in your LH Cluster.

## Requirements
* `NodeJS`. The version 20 is required. In case you need multiple versions of node on you machine, you can install
  [NVM](https://github.com/nvm-sh/nvm) on your machine. Then you can run: `nvm use`
* Previous command will use the node version defined in the .nvmrc file on the root folder of this project.

* Install pnpm globally
```
npm install pnpm --global
```

* Install the node packages:
```
pnpm install
```

## Feature Toggles
At this point you can configure the feature toggles through an environment variable that will enable/disabled the feature in any environment.
The name of the environment variable that you need to set as `true` or `false` is `LHD_OAUTH_ENABLED`.

### CHANGING A TOOGLE'S STATUS
* You need to change the environment variable value.
* You need to re-run the application.

### HOW TO USE THEM
#### Server side
On the server side code you can do:
```
if (process.env.LHD_OAUTH_ENABLED === 'true') {
  // ...some code here...
}
```

#### Client side
We have created a React Hook in order to read the feature toggles on the client side components.
```
  const isAuthenticationEnabled = useFeatureToggle('isAuthenticationEnabled')

  if (isAuthenticationEnabled) {
     // do something here
  }
```

### Testing
To make the feature toggles available for the tests the following has been put in place:
* A `testGlobals.js` file which is reading the featureToggles and loading them into the jest context.
* To turn ON/OFF a toggle for the tests, you can either:

  * Enable/Disable it in the `featureToggles` file like indicated in the above sections.
  * In your tests you can overwrite the value of the toggle like:
    ```
    __A_TOGGLE__ = false
    __ANOTHER_TOGGLE__ = true
    ```

## SINGLE SIGN ON FOR THE DASHBOARD
* The LH Dashboard can use Keycloack provider as an SSO mechanism for a user to login into the dashboard and use it. For that to work you have to enable the toggle `LHD_OATUH_ENABLED` in the needed environments.

* You need to your LittleHorse server running in OAuth mode for this feature to work correclty.

* For the needed Environment Variables please refer to the corresponding section in this README.

* [Here a detail of the implemented authentication flow for this project](https://link.excalidraw.com/readonly/5sxfddEgSEFTEQLF3WAG)


### Libraries
We are using the following libraries:
* `ts-proto`: library for the generation of the TypeScript files, we are using the options:
  * outputServices=nice-grpc -> ts-proto will output server and client stubs for nice-grpc.
  * outputServices=generic-definitions -> ts-proto will output generic (framework-agnostic) service definitions. Required to work with nice-grpc.
  * useDate=string -> Protobuff Timestamps are compiled as TypeScript strings, this will avoid us to be dealing with JS Date types.
  * esModuleInterop=true -> As Next JS make use only of ES Modules, we need this option so our ts proto files import modules like `import something from something` instead of using `require`.
  * stringEnums=true -> Enums are represented as strings instead of numbers.
* `nice-grpc`: Library that provides a good interface from the developer perspective when making GRPC calls.

### Generating TypeScript proto files

Protobuf files can be re-compiled by running, _from the root of the repo, NOT this folder_:

```
./local-dev/compile.sh
```

### Known Issues
The protobufjs library does not have a default export because of that you can have an error similar to:

```
if (_m0.util.Long !== Long)
TypeError: Cannot read properties of undefined (reading 'Long')
```
A more detailed discussion about it can be found (here)[https://github.com/stephenh/ts-proto/issues/536#issuecomment-1198154550], the workaround proposed is to replace `import _m0 from 'protobufjs/minimal'` by `import * as _m0 from 'protobufjs/minimal'`.
A new step has been included in the `compile-proto.sh` script to take care of that.


## Build

To build all apps and packages, run the following command:

```
pnpm build
```

## Develop

To start the application on your local machine run the following command:

```
pnpm dev
```

Open [http://localhost:3001](http://localhost:3001) with your browser to see the result.

You can start editing the page by modifying `pages/next.js`. The page auto-updates as you edit the file.

[API routes](https://nextjs.org/docs/api-routes/introduction) can be accessed on [http://localhost:3001/api/hello](http://localhost:3001/api/hello). This endpoint can be edited in `pages/api/hello.js`.

The `pages/api` directory is mapped to `/api/*`. Files in this directory are treated as [API routes](https://nextjs.org/docs/api-routes/introduction) instead of React pages.

### Know Issues
As of September 2023, turbo versions greater that 1.10.4 are causing the following error. [Issue detailed here.](https://github.com/vercel/turbo/issues/5331):

```
root task dev (turbo run dev) looks like it invokes turbo and might cause a loop
```

That's why in the package JSON we have fixed the version to 1.10.4 until the bug is fixed.

## Environment Variables
If running the app without Docker, you need to fill in the environment variables in the `.env` file inside `apps/web`. The `.env` file in the root folder is not being read by the app.

- `AUTH_SECRET` random string that will be used to encrypt the secrets and also the JWT token
- `API_URL` the URL of the Little Horse Core, in your local it will be `localhost:2023`
- `NEXTAUTH_URL` the url (domain) in which the dashboard will run (required for some authentication methods). For your local you can use: `http:/localhost:3001/`
- `KEYCLOAK_CLIENT_ID` the client id configured in keycloack
- `KEYCLOAK_CLIENT_SECRET` the client secret configured in keycloack
- `KEYCLOAK_ISSUER_URI` the keycloack
- `LHC_CA_CERT` To specify the path to the self signed certificate that the dashboard needs to connect to a LittleHorse server configured to work with OAuth.


## Linting
We are using ESLint as the linter for project. Given that we have a mono-repo structure we have the main command for the linter in the root package.json file.

You can run the linter by:
```
pnpm run lint
```

If you wanna ESLint to try to fix the issues automatically, run:
```
pnpm run lint -- --fix
```

### What are we running the linter for:
* apps/web
* packages/ui

### Configuring your IDE to have Linter Live Feedback

#### Intellij
* Go to the ESLint configuration
* Choose Manual Configuration
* For ESLint package pick the one in the root folder: `<your workskpace dir>/lh-dashboard/node_modules/eslint`
* For Working directories use:`<your workskpace dir>/lh-dashboard/apps/web;<your workskpace dir>/lh-dashboard/packages/ui`
* For the Configuration file use: `<your workskpace dir>/lh-dashboard/.eslintrc.js`
* With the above config you will receive live feedback in intellij on regards to the linting rules that are configured
* While configuring the ESLint plugin you can enable the option: `run eslint --fix on save`
* The reFormat code shortcut will *NOT* use the ESLint rules, you should do:`Fix ESLint problems` by opening the IntelliJ`Actions` menu.

#### VSCode
* Install the extension called `ESLint'
* Configure the VSCode IDE with the following options:
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
* On the actions menu choose: `Developer: Reload Window`
* You should start seeing the linter errors in the code

### Linter configuration
The configuration for the entire mono-repo is under `/packages/eslint-config-custom/next.js`. On the mentioned file you can configure the rules that apply
for the entire `lh-dashboard` mono-repo.

## Testing
Jest is being used as the testing framework, any file that has the pattern `*.test.*` in its name is considered a test and will be executed by Jest.
To run the tests please execute:
```
pnpm test
```
If you wanna watch your tests while developing execute:
```
pnpm test --watch
```

### Environment variables
You need to create a `env.test.local` file to contain any env variable you might for your tests.


## Start the Dashboard with Docker

The Dashboard docker image is under `docker/dashboard`, in order to run it please do the following:

1. Build the docker image
```
 docker build -f docker/dashboard/Dockerfile -t a-tag-name .
```

2.1 If you want to run it the Authentication Enabled:
```
docker run --env LHD_OAUTH_ENABLED='true' --env LHD_OAUTH_CLIENT_ID='{a-client-id}' --env LHD_OAUTH_CLIENT_SECRET='{a-client-secret}' --env LHD_OAUTH_SERVER_URL='{https://keycloack-env}/realms/lh' --env LHD_OAUTH_ENCRYPT_SECRET='{a-secret-to-encrypt}' --env LHD_OAUTH_CALLBACK_URL='localhost:8080' --env LHD_API_HOST=localhost --env LHD_API_PORT=2023 --network host a-tag-name
```

2.1 If you want to run it the Authentication Disabled:
```
docker run --env LHD_OAUTH_ENABLED='false' --env LHD_OAUTH_ENCRYPT_SECRET='{a-secret-to-encrypt}' --env LHD_API_HOST=localhost --env LHD_API_PORT=2023 --network host a-tag-name
```
**Note**: For **2.1** and **2.2** please replace the placeholders between the curly braces with the corresponding values.
