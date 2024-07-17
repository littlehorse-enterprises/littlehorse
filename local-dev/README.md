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

To "reset" the LittleHorse cluster, you need to delete the data in Kafka and also delete the KafkaStreams RocksDB state.
That can be done as follows:

1. Stop all LH Server processes.
2. Run `./local-dev/setup.sh --refresh`.
3. Start the LH Servers again.

## Running Multiple LH Servers

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

## Release a new version

Upgrade to a new version:

```bash
./local-dev/bump.sh --help
```

## Building the Docker Image

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

## Compile Schemas

```
./local-dev/compile-proto.sh
```

## Configuring OAuth2

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


## Configuring mTLS

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
LHW_SERVER_CONNECT_LISTENER=MTLS
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