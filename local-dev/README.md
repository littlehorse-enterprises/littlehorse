# LH Local Development Tools

This tools are intended to be use for development purposes.

## Running LH

The LH Server depends on Kafka as a backend database. To start Kafka using docker compose, you can run:

```
./local-dev/setup.sh --kafka
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

## Hard Cleanup

You can clean up (i.e. stop Kafka and delete the data from the state directory) as follows:

```
./local-dev/setup.sh --clean
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

You can build the `littlehorse-server` and `littlehorse-standalone` docker images by running:

```
./local-dev/build.sh
```

To build the `littlehorse-server` image for local development utilizing the local gradle cache, you can run:

```
./local-dev/build.sh --quick
```

Run server with docker (default config `local-dev/server-1.config`):

```
./local-dev/do-server.sh --docker
```

Run server with docker and specific config:

```
./local-dev/do-server.sh --docker <config-name>

# Example
./local-dev/do-server.sh --docker server-2
```

## Compile Schemas

```
./local-dev/compile-proto.sh
```

## Configuring OAuth2

> You need to install [httpie](https://httpie.io/cli)

Run keycloak and creates clients:

```
./local-dev/setup.sh --keycloak --kafka
```

Clients:

| Client Id | Client Secret                    | Description                                                    |
| --------- |----------------------------------| -------------------------------------------------------------- |
| server    | 3bdca420cf6c48e2aa4f56d46d6327e0 | Server Introspection                                           |
| worker    | 40317ab43bd34a9e93499c7ea03ad398 | For Workers to issue access tokens (Client Credentials FLow)   |
| lhctl     | N/A                              | For lhctl cli to issue access tokens (Authorization Code Flow) |

Run the server:

```
./local-dev/do-server.sh oauth
```

Open Keycloak:

http://localhost:8443

- User: `admin`
- Password: `admin`
