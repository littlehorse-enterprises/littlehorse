# Little Horse Dashboard

## What's inside?

This Turborepo includes the following packages/apps:

### Apps and Packages

- `web`: another [Next.js](https://nextjs.org/) app
- `ui`: a stub React component library shared by both `web` and `docs` applications
- `eslint-config-custom`: `eslint` configurations (includes `eslint-config-next` and `eslint-config-prettier`)
- `tsconfig`: `tsconfig.json`s used throughout the monorepo

Each package/app is 100% [TypeScript](https://www.typescriptlang.org/).

### Utilities

This Turborepo has some additional tools already setup for you:

- [TypeScript](https://www.typescriptlang.org/) for static type checking
- [ESLint](https://eslint.org/) for code linting
- [Prettier](https://prettier.io) for code formatting

### Environment Variables
If running the app without Docker, you need to fill in the environment variables in the `.env` file inside `apps/web`. The `.env` file in the root folder is not being read by the app.

### Build

To build all apps and packages, run the following command:

```
pnpm build
```

### Develop

To develop all apps and packages, run the following command:

```
pnpm dev
```


## Docker
### Build Docker Image
In root rename `.env.sample` to `.env`

Fill the values ​​of the variables. 

The first three variables are required.
- `AUTH_SECRET` random string 
- `API_URL` the URL of the Little Horse Core 
- `NEXTAUTH_URL` the url (domain) in which the dashboard will run (required for some authentication methods)

The following variable blocks are required only if you want to use that Authentication Provider.

```
AUTH_SECRET=""
API_URL=
NEXTAUTH_URL=""


GOOGLE_ID=""
GOOGLE_SECRET=""

GITHUB_ID=""
GITHUB_SECRET=""

# AZURE_AD_CLIENT_ID= 
# AZURE_AD_CLIENT_SECRET= 
# AZURE_AD_TENANT_ID= 


# OKTA_CLIENT_ID= 
# OKTA_CLIENT_SECRET= 
# OKTA_ISSUER_URI=
```
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

# Compile protos
## Libraries
We are using the following libraries:
* `ts-proto`: library for the generation of the TypeScript files, we are using the options: 
    * outputServices=nice-grpc -> ts-proto will output server and client stubs for nice-grpc.
    * outputServices=generic-definitions -> ts-proto will output generic (framework-agnostic) service definitions. Required to work with nice-grpc.
    * useDate=string -> Protobuff Timestamps are compiled as TypeScript strings, this will avoid us to be dealing with JS Date types.
    * esModuleInterop=true -> As Next JS make use only of ES Modules, we need this option so our ts proto files import modules like `import something from something` instead of using `require`.
    * stringEnums=true -> Enums are represented as strings instead of numbers.
* `nice-grpc`: Library that provides a good interface from the developer perspective when making GRPC calls.


## Initializing Sub Modules
We have configured littlehorse core project as a submodule to have access to the protobuff definitions that we need to make GRPC calls to the LittheHorse public API.

If the littlehorse folder is empty, please execute:

```
g submodule init
g submodule update
```

Execute the following script to generate the TypeScript proto files.
```
./compile-protos.sh
```

## Generating TypeScript proto files
Execute:
```
./compile-proto.sh
```

The TypeScript proto files will be available under `/littlehorse-public-api`
 