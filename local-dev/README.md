# Developing LittleHorse

We appreciate any contributions to our codebase. Please note that by contributing to LittleHorse you accept the terms of the AGPLv3 License.

- [Developing LittleHorse](#developing-littlehorse)
  - [Repository Inventory](#repository-inventory)
  - [Setting Up Your Local Env](#setting-up-your-local-env)
    - [Prerequisites](#prerequisites)
    - [Setup Pre-commit](#setup-pre-commit)
    - [Setting up Dev Dependencies](#setting-up-dev-dependencies)
  - [Running the Server Natively](#running-the-server-natively)
    - [Running the Dashboard](#running-the-dashboard)
    - [Debug Cycle and Hard Cleanup](#debug-cycle-and-hard-cleanup)
    - [Running Multiple LH Servers](#running-multiple-lh-servers)
    - [Testing with OAuth2](#testing-with-oauth2)
    - [Testing with mTLS](#testing-with-mtls)
  - [Building Locally](#building-locally)
    - [Building the Docker Images](#building-the-docker-images)
    - [Compiling the Protobuf](#compiling-the-protobuf)
  - [Release a New Version](#release-a-new-version)
  - [Writing a Pull Request](#writing-a-pull-request)

## Repository Inventory

The LittleHorse repository contains the following components:

- [`server`](../server)
    - Code for LittleHorse Server.
- [`sdk-java`](../sdk-java)
    - Library for creating `WfSpec`'s in Java.
    - Library for executing `TaskRun`'s in Java.
- [`sdk-go`](../sdk-go)
    - Library for creating `WfSpec`'s in GoLang.
    - Library for executing `TaskRun`'s in GoLang.
- [`sdk-python`](../sdk-python)
    - Library for creating `WfSpec`'s in Python.
    - Library for executing `TaskRun`'s in Python.
- [`dashboard`](../dashboard)
  - Web UI for LH.
- [`canary`](../canary)
  - Synthetic Monitoring Tool for LH Server.
- [`lhctl`](../lhctl)
    - The `lhctl` command line interface.
- [`test-utils`](../test-utils/)
  - A new framework for running end-to-end tests in our pipeline.
- [`examples`](../examples)
    - A series of examples with different level of complexity.
- [`schemas`](../schemas)
    - The LittleHorse Protocol Buffer Specification.
- [`docker`](../docker)
    - Code for building the LittleHorse docker images.

## Setting Up Your Local Env

### Prerequisites

This repository requires the following system dependencies:

- `java`
    - [sdk-java](sdk-java): Java 11
    - [server](server): Java 21
- `gradle`, preferably version 8 or later.
- `docker` and `docker-compose-plugin`.
- `go`, `protoc`, `protoc-gen-go`, `protoc-gen-go-grpc` and `protoc-gen-grpc-java`.
    - [Protocol Buffer Compiler](https://grpc.io/docs/protoc-installation/)
    - [Java gRPC compiler plugin](https://github.com/grpc/grpc-java/blob/master/compiler/README.md)
    - This needs to be put somewhere in your `PATH`.
- `pre-commit` (this is a dev dependency)
- `python` and [poetry](https://python-poetry.org/).
    - [sdk-python](sdk-python): >= 3.9
- `nvm` and `node` >= 20

### Setup Pre-commit

https://pre-commit.com/ is a framework for managing and maintaining multi-language pre-commit hooks.

```bash
pre-commit install
```

- Only java:

  ```
  export SKIP=install-python,install-dashboard,install-sdk-js,format-python,format-go,format-sdk-js,lint-python,mypy,go-tests,python-tests,dashboard-build,dashboard-tests,sdk-js-tests
  ```

- Only go:

  ```
  export SKIP=install-python,install-dashboard,install-sdk-js,format-python,format-java,format-sdk-js,lint-python,mypy,python-tests,dashboard-build,dashboard-tests,sdk-js-tests,java-build,javadoc
  ```

- Only python:

  ```
  export SKIP=install-dashboard,install-sdk-js,format-go,format-java,format-sdk-js,dashboard-build,dashboard-tests,sdk-js-tests,java-build,javadoc,go-tests
  ```

- Only js:

  ```
  export SKIP=install-python,format-java,format-python,format-go,lint-python,mypy,java-build,javadoc,go-tests,python-tests
  ```

### Setting up Dev Dependencies

LittleHorse depends upon Kafka as its backing data store. You can set up Kafka via:

```
./local-dev/setup.sh
```

Note that this will also set up a Keycloak container in case you want to test LittleHorse's OAuth capabilities.

## Running the Server Natively

The LH Server depends on Kafka as a backend database. To start Kafka using docker compose, you can run:

```
./local-dev/setup.sh
```

Next, you can start the LH server itself. The server can be started in a single command:

```
./local-dev/do-server.sh
```

You can confirm that the Server is running via:

```
lhctl search wfSpec
```

Result:

```
{
  "results": []
}
```

### Running the Dashboard

To run the dashboard, you can do the following (it requires `npm` to be installed).

```
cd dashboard
npm install
npm run dev
```

The dashboard will be accessible on `localhost:3000` by default. This assumes that you have the LH Server accessible and running on `localhost:2023` with no authentication. That can be done as described above.

### Debug Cycle and Hard Cleanup

While debugging, if you want to clear all data in Kafka and RocksDB and clean your gradle cache, you can:

1. Stop all LH Server processes.
2. Run `./local-dev/setup.sh --refresh`.
3. Start the LH Servers again.

When done developing, you can do a hard clean up (i.e. stop Kafka and delete the data from the state directory) as follows:

```
./local-dev/setup.sh --clean
```

### Running Multiple LH Servers

LittleHorse is a distributed system in which the different LH Server Instances (Brokers) need to communicate with each
other. For example (among many others), all GET requests on the API use Interactive Queries, which involves requests
between the different Brokers. Therefore, you'll need to be able to test with multiple brokers running at once.

Running two brokers is slightly tricky as you must configure the ports, advertised hostnames, and Kafka group instance
ID's correctly.

However, you can start two Brokers in your terminal as follows:

```
# The first server has an external API port of 2023
./local-dev/do-server.sh

# <In another terminal>
# The second server has an external API port of 2033
./local-dev/do-server.sh server-2
```

### Testing with OAuth2

> You need to install [httpie](https://httpie.io/cli)

Run keycloak and creates clients:

```
./local-dev/setup.sh --keycloak
```

Clients:

| Client Id | Client Secret                    | Description                                                      |
|-----------|----------------------------------|------------------------------------------------------------------|
| server    | 3bdca420cf6c48e2aa4f56d46d6327e0 | Server Introspection                                             |
| worker    | 40317ab43bd34a9e93499c7ea03ad398 | For Workers to issue access tokens (Client Credentials FLow)     |
| canary    | 8b629ff9b2684014b8c62d4da8cc371e | For LH Canary to issue access tokens (Client Credentials FLow)   |
| dashboard | 74b897a0b5804ad3879b2117e1d51015 | For LH Dasboard to issue access tokens (Client Credentials FLow) |
| lhctl     | N/A                              | For lhctl to issue access tokens (Authorization Code Flow)       |

Run the server:

```
./local-dev/do-server.sh oauth
```

> Check file [oauth.config](configs/oauth.config)

Open Keycloak:

http://localhost:8888

- User: `admin`
- Password: `admin`


### Testing with mTLS

1. Create a tenant
```bash
lhctl put tenant <your tenant name>
```

2. Create the principal you wish you authenticate through:
```bash
lhctl put principal <your principal name> --acl "acl_workflow:read" --tenantId <your tenant name> --overwrite
```
*Replace `<your principal name>` with your desired Principal name*

3. Update your `issue-certificates.sh` file
	1. Replace `CN=localhost` with `CN=<your principal name>`

4. Generate your certificates
```bash
./local-dev/issue-certificates.sh
```

5. Ensure you have the following configuration settings in your LittleHorse server `/local-dev/mlts.config` file:
```
LHS_LISTENERS=MTLS:2023
LHS_LISTENERS_PROTOCOL_MAP=MTLS:MTLS

LHS_CA_CERT=local-dev/certs/ca/ca.crt
LHS_LISTENER_MTLS_CERT=local-dev/certs/server/server.crt LHS_LISTENER_MTLS_KEY=local-dev/certs/server/server.key
```

6. Set up your LittleHorse Worker configuration file:
```
LHW_TASK_WORKER_VERSION=local.dev
LHC_API_HOST=localhost
LHC_API_PORT=2023
LHC_API_PROTOCOL=TLS
LHC_CLIENT_CERT=/<path to your workspace>/littlehorse/local-dev/certs/client/client.crt
LHC_CLIENT_KEY=/<path to your workspace>/littlehorse/local-dev/certs/client/client.key
LHC_CA_CERT=/<path to your workspace>/littlehorse/local-dev/certs/ca/ca.crt
```
*Note: Replace `<path to your workspace>` with the full path to your LittleHorse repository folder to properly locate your certificates and keys*

7. Run LittleHorse server with the mTLS Config
```bash
./local-dev/do-server.sh mtls
```

8. Check which Principal you are authenticating as
```
lhctl whoami
```

## Building Locally

### Building the Docker Images

To build the `littlehorse-server` image for local development utilizing the local gradle cache, you can run:

> It creates `littlehorse/lh-server:latest`.

```
./local-dev/build.sh
```

To build the dashboard image:

```
./local-dev/build.sh --dashboard
```

> It creates `littlehorse/lh-dashboard:latest`.

To build the canary image:

```
./local-dev/build.sh --canary
```

> It creates `littlehorse/lh-canary:latest`.

### Compiling the Protobuf

If you make a change to anything in the `schemas` directory, you will need to compile the protobuf:

```
./local-dev/compile-proto.sh
```

## Release a New Version

To release a new version, you should push a properly-formed Git Tag, and our pipeline will take care of it.

```
git tag -am "Release X.Y.Z" vX.Y.Z
git push --follow-tag
```
Note that we follow [Semantic Versioning](https://semver.org) and that `X` is the major version, `Y` is the minor version, and `Z` is the patch.

While SemVer frowns upon the `v` prefix, we need it because of how Go module releases work. Our pipeline removes the `v` from all generated artifacts (eg. `sdk-java` on maven, `sdk-python` on pypi, our docker images, etc).

## Writing a Pull Request

Here at LittleHorse, we use the [Conventional Commits v1.0.0](https://www.conventionalcommits.org/en/v1.0.0/) specification for writing pull request messages.

Conventional Commits provides a standardized format for describing proposed changes and provides semantic versioning support for certain *types* of changes.

The basic structure of a conventional commit message is as follows.

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

Certain *types* of commits correlate to semantic versioning updates:

- fix: PATCH
- feat: MINOR
- a commit with the footer `BREAKING CHANGE:` or a `!` after the type/scope

You should reference the Conventional Commits v1.0.0 [website](https://www.conventionalcommits.org/en/v1.0.0/) for additional *types*, detailed specification rules, examples, and FAQs about the practice before making a pull request.