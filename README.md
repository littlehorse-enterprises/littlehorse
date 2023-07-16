# LittleHorse Server

This repository contains the code for the core LittleHorse Server.

For a description of the architecture, see the [architecture docs](docs/ARCH.md).

## LH Repository Inventory

The LittleHorse project currently has multiple repositories, described below:

- [`server`](server)
  - Code for LittleHorse Server.
- [`sdk-java`](sdk-java)
  - Library for creating `WfSpec`'s in Java.
  - Library for executing `TaskRun`'s in Java.
- [`sdk-go`](sdk-go)
  - Library for creating `WfSpec`'s in GoLang.
  - Library for executing `TaskRun`'s in GoLang.
- [`lhctl`](lhctl)
  - The `lhctl` command line interface.
- [`e2e-tests`](e2e-tests)
  - A series of system integration tests used to verify the cohesive behavior of
    the LH Server, Java Workflow SDK, and Java Task Library together.
- [`examples`](examples)
  - A series of examples with different level of complexity.
- [`schemas`](schemas)
  - The LittleHorse Protocol Buffer Specification.
- [`docker`](docker)
  - The LittleHorse Docker Image.

## Quick Start

### Prerequisites

This repository requires the following system dependencies:

- `openjdk`, preferably version 17 or later.
- `gradle`, preferably version 7.4 or later.
- `docker` and `docker-compose-plugin`.
- `go`.

### Running Locally

Install `lhctl`:

```
cd lhctl
go install .
```

> Make sure it's on the path <br />
> `export GOPATH="$(go env GOPATH)"` <br />
> `export PATH="$PATH:$GOPATH/bin"`

Verify the installation:

```
lhctl
```

Start a LH Server with:

```
cd docker
docker compose up -d
```

When you run the LH Server according to the command above, the API Host is `localhost` and the API Port is `2023`.
Now configure `~/.config/littlehorse.config`:

```
LHC_API_HOST=localhost
LHC_API_PORT=2023
```

You can confirm that the Server is running via:

```
lhctl search wfSpec
```

Result:

```
{
  "code":  "OK",
  "objectIds":  []
}
```

Now let's run an example:

> More examples at [examples](examples).

```
gradle example-basic:run
```

In another terminal, use `lhctl` to run the workflow:

```
# Here, we specify that the "input-name" variable = "Obi-Wan"
lhctl run example-basic input-name Obi-Wan
```

Now let's inspect the result:

```
# This call shows the result
lhctl get wfRun <wf run id>

# This will show you all nodes in tha run
lhctl get nodeRun <wf run id> 0 1

# This shows the task run information
lhctl get taskRun <wf run id> <task run global id>
```

## Development

This section describes how to run the LittleHorse server in a development environment.

### Prerequisites

This repository requires the following system dependencies:

- `openjdk`, preferably version 17 or later.
- `gradle`, preferably version 7.4 or later.
- `docker` and `docker-compose-plugin`.
- `go`, `protoc`, `protoc-gen-go`, `protoc-gen-go-grpc` and `protoc-gen-grpc-java`.
    - [Protocol Buffer Compiler](https://grpc.io/docs/protoc-installation/)
    - [Java gRPC compiler plugin](https://github.com/grpc/grpc-java/blob/master/compiler/README.md)
    - This needs to be put somewhere in your `PATH`.
- `npm` (this is a dev dependency)
- `pre-commit` (this is a dev dependency)

### Setting Up the Linters

We have decided to use the [Prettier for Java](https://github.com/jhipster/prettier-java) linter. It is the same linter used by most Javascript projects but adapted for Java. Therefore, it requires Node.js as a prerequisite to run.

To install the formatter, all you need to do is first install Node.js as per the website, and then run:

```
npm install  # This uses the package.json
npm run format  # This runs the linters
```

This repository (and all other LittleHorse Java repo's) have the `.vscode` folder checked into source control. The `settings.json` file is configured properly to enable formatting on save, but you first need to install the [Prettier - Code formatter](https://open-vsx.org/extension/esbenp/prettier-vscode) extension.


### Setup Pre-commit

https://pre-commit.com/ is a framework for managing and maintaining multi-language pre-commit hooks.

```bash
pre-commit install
```

### Components

The LittleHorse ecosystem has different component, for information about to develop on each of them go to:

- [`server`](server)
- [`sdk-java`](sdk-java)
- [`sdk-go`](sdk-go)
- [`lhctl`](lhctl)
- [`e2e-tests`](e2e-tests)
- [`examples`](examples)
