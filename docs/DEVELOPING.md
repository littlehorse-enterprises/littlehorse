## Development

This document details how to set up your laptop to develop LittleHorse.

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

### Setting up Dev Dependencies

LittleHorse depends upon Kafka as its backing data store. You can set up Kafka via:

```
./local-dev/setup.sh
```

Note that this will also set up a Keycloak container in case you want to test LittleHorse's OAuth capabilities.

### Running the Server

In `local-dev` there are various scripts to help debug. The simplest way to run LittleHorse locally is via:

```
./local-dev/do-server.sh
```

You can pass a configuration file name to `do-server.sh` to run the LH Server with a specific configuration. For example, if you want to run two LH Servers together to test their communication via interactive queries, you can:

```
./local-dev/do-server.sh server-1

# open a new terminal
./local-dev/do-server.sh server-2
```

### Configuring your clients

For the standard server, the default client configuration should work. Note that by default, `lhctl` checks `~/.config/littlehorse.config` for configuration options.

```
LHC_API_HOST=localhost
LHC_API_PORT=2023

# For task workers
LHW_SERVER_CONNECT_LISTENER=PLAIN
```
_NOTE: The configurations above are indeed the defaults._