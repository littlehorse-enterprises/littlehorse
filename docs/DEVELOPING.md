## Development

This document details how to set up your laptop to develop LittleHorse.

### Prerequisites

This repository requires the following system dependencies:

- `java`
    - [sdk-java](sdk-java): Java 11
    - [server](server): Java 17
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

### Setting up Dev Dependencies

LittleHorse depends upon Kafka as its backing data store. You can set up Kafka via:

```
./local-dev/setup.sh --kafka
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
