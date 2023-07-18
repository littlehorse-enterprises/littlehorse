<p align="center">
<img alt="LH" src="https://littlehorse.dev/img/logo.jpg" width="50%">
</p>

# LittleHorse

<a href="https://littlehorse.dev/"><img alt="littlehorse.dev" src="https://img.shields.io/badge/-LittleHorse.dev-7f7aff"></a>

This repository contains the code for the core LittleHorse Components.

For a description of the architecture, see the [architecture docs](docs/ARCH.md).

## LH Repository Inventory

The LittleHorse project currently has multiple components, described below:

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

## Quickstart

- [Java Quickstart](docs/QUICKSTART_JAVA.md)
- [Go Quickstart](docs/QUICKSTART_GO.md)

## Configurations

- [Server Configurations](docs/CONFIGURATIONS_SERVER.md)
- [Workers/Clients Configurations](docs/CONFIGURATIONS_CLIENTS.md)

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


### Setup Pre-commit

https://pre-commit.com/ is a framework for managing and maintaining multi-language pre-commit hooks.

```bash
pre-commit install
```

### Components

LittleHorse ecosystem has different component, for information about how to develop go to:

- [`server`](server)
- [`sdk-java`](sdk-java)
- [`sdk-go`](sdk-go)
- [`lhctl`](lhctl)
- [`e2e-tests`](e2e-tests)
- [`examples`](examples)


## License

<a href="https://spdx.org/licenses/SSPL-1.0.html"><img alt="SSPL LICENSE" src="https://img.shields.io/badge/cover%20by-SSPL%201.0-blue"></a>

Source code in this repository is covered by the [Server
Side Public License version 1](https://spdx.org/licenses/SSPL-1.0.html), unless the header specifies another license. 