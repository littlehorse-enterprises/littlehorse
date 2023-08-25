# LH Local Development Tools

This tools are intended to be use for development purposes.

## Running LH

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
{
  "code":  "OK",
  "objectIds":  []
}
```

## Setup and Cleanup Kafka

Run:

```
./local-dev/setup.sh
```

You can clean up (i.e. stop Kafka and delete the data from the state directory) as follows:

```
./local-dev/cleanup.sh
```

## Cleanup Data

To "reset" the LittleHorse cluster, you need to delete the data in Kafka and also delete the KafkaStreams RocksDB state. That can be done as follows:

1. Stop all LH Server processes.
2. Run `./local-dev/refresh.sh`.
3. Start the LH Servers again.

## Running Multiple LH Servers

LittleHorse is a distributed system in which the different LH Server Instances (Brokers) need to communicate with each other. For example (among many others), all GET requests on the API use Interactive Queries, which involves requests between the different Brokers. Therefore, you'll need to be able to test with multiple brokers running at once.

Running two brokers is slightly tricky as you must configure the ports, advertised hostnames, and Kafka group instance ID's correctly.

However, you can start two Brokers in your terminal as follows:

```
# The first server has an external API port of 2023
./local-dev/do-server.sh

# <In another terminal>
# The second server has an external API port of 2033
./local-dev/do-server.sh server-2
```

## Release a new version

Upgrade to a new version:

```bash
./local-dev/bump.sh --help
```

## Building the Docker Image

You can build the `littlehorse` docker image by running:

```
./local-dev/build.sh
```

Run server with docker (default config `local-dev/server-1.config`):

```
./local-dev/do-docker-server.sh
```

Run server with docker and specific config:

```
./local-dev/do-docker-server.sh <config-name>

# Example
./local-dev/do-docker-server.sh server-2
```

## Compile Schemas

```
./local-dev/compile-proto.sh
```

## Configuring OAuth2

> You need to install [httpie](https://httpie.io/cli)

Creates client at keycloak:

```
./local-dev/setup-keycloak.sh
```

Clients:

| Client Id | Client Secret                    | Description                                                    |
| --------- | -------------------------------- | -------------------------------------------------------------- |
| server    | 3bdca420cf6c48e2aa4f56d46d6327e0 | Server Introspection                                           |
| worker    | 40317ab43bd34a9e93499c7ea03ad398 | For Workers to issue access tokens (Client Credentials FLow)   |
| lhctl     | ee96a53af0034437bee816e63944e0f0 | For lhctl cli to issue access tokens (Authorization Code Flow) |

Creates certificates:

```
./local-dev/issue-certificates.sh
```

Update config:

```
LHS_LISTENERS=OAUTH:2023
LHS_LISTENERS_PROTOCOL_MAP=OAUTH:TLS
LHS_LISTENERS_AUTHORIZATION_MAP=OAUTH:OAUTH

LHS_LISTENER_OAUTH_CERT=local-dev/certs/server/server.crt
LHS_LISTENER_OAUTH_KEY=local-dev/certs/server/server.key

LHS_LISTENER_OAUTH_CLIENT_ID=server
LHS_LISTENER_OAUTH_CLIENT_SECRET=3bdca420cf6c48e2aa4f56d46d6327e0
LHS_LISTENER_OAUTH_AUTHORIZATION_SERVER=http://localhost:8888/realms/lh
```

> Check file [oauth.config](configs/oauth.config)


Run the server:

```
./local-dev/do-server.sh oauth
```

Open Keycloak:

http://localhost:8888

- User: `admin`
- Password: `admin`
