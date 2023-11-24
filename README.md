# Little Horse Dashboard


## What's inside?

This Turborepo includes the following packages/apps:

## Requirements
* `NodeJS`. The version 20 is required. In case you need multiple versions of node on you machine, you can install [NVM](https://github.com/nvm-sh/nvm) on your machine. Then you can run:
  `nvm use`
  * Previous command will use the node version defined in the .nvmrc file on the root folder of this project.

* Install pnpm globally
```
npm install pnpm --global 
```

* Install the node packages:
```
pnpm install
```

### Initializing Sub Modules
We have configured littlehorse core project as a submodule to have access to the protobuff definitions that we need to make GRPC calls to the LittheHorse public API.

If the littlehorse folder is empty, please execute:

```
g submodule init
g submodule update
```

Execute the following script to generate the TypeScript proto files.
```
./compile-proto.sh
```

## Compiling the protobuff files
LHDashboard needs the Little Horse protobuff files in order to do operations against its public API. 
To achieve that we have included Littlehorse as a submodule (more details if the above section); with those protobuff files
we need to generate TypeScript code to be used in the NextJS application.

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
Execute:
```
./compile-proto.sh
```

The TypeScript proto files will be available under `/littlehorse-public-api`

### Known Issues
The protobuffjs library does not have a default export because of that you can have an error similar to:

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


## Docker
### Build Docker Image
In root rename `.env.sample` to `.env`, the values you specify in this file will be copied to `apps/web/.env`

To build the docker image execute the next script

```
docker build -t lhd .
```

Note: The Docker file will copy the `.env` file from the root folder to apps/web

### Run Docker Image

```
docker run -p 80:80 -d lhd 
```
Remap the port
```
docker run -p 3001:80 -d lhd 
```


 