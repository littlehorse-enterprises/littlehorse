# Basic Deployment

This example shows how to run LittleHorse with a single Server and the dashboard, without authentication, using Docker Compose, accessible on `localhost:2023`.

## Docker Compose File

Save the following docker-compose file and run: `docker compose up -d`.

```yaml
services:
  kafka:
    container_name: lh-kafka
    image: apache/kafka:3.8.0
    environment:
      KAFKA_LISTENERS: CONTROLLER://:29092,EXTERNAL://:19092,INTERNAL://:9092
      KAFKA_ADVERTISED_LISTENERS: EXTERNAL://localhost:19092,INTERNAL://kafka:9092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: CONTROLLER:PLAINTEXT,EXTERNAL:PLAINTEXT,INTERNAL:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: INTERNAL
      KAFKA_BROKER_ID: "1"
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: "1"
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: "1"
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: "1"
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@localhost:29092
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_NODE_ID: "1"
      KAFKA_KRAFT_CLUSTER_ID: abcdefghijklmnopqrstuv
    restart: on-failure
    healthcheck:
      test: kafka-topics.sh --bootstrap-server kafka:9092 --list > /dev/null 2>&1
      interval: 5s
  littlehorse:
    container_name: lh-server
    image: ghcr.io/littlehorse-enterprises/littlehorse/lh-server:0.10.1
    environment:
      LHS_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
      LHS_SHOULD_CREATE_TOPICS: "true"
      LHS_HEALTH_SERVICE_PORT: "1822"
    restart: on-failure
    healthcheck:
      test: curl -f localhost:1822/liveness
      interval: 5s
    depends_on:
      kafka:
        condition: service_healthy
    ports:
      - "2023:2023"
  dashboard:
    container_name: lh-dashboard
    image: ghcr.io/littlehorse-enterprises/littlehorse/lh-dashboard:0.10.1
    environment:
      LHC_API_HOST: littlehorse
      LHC_API_PORT: 2023
      LHC_OAUTH_ENABLED: false
    restart: on-failure
    healthcheck:
      test: curl -f localhost:8080
      interval: 5s
    ports:
      - "8080:8080"
```

## Using the Example

Once you have created the LH Cluster using Docker Compose, the next step is to access it. For example, you can use one of our [quickstarts](../../05-developer-guide/00-install.md#get-started) with the following configurations.

The dashboard can be accessed at [`http://localhost:8080`](http://localhost:8080).

### CLI Configs

The `lhctl` CLI is configured using (by default) the `${HOME}/.config/littlehorse.config` file.

```
LHC_API_HOST=localhost
LHC_API_PORT=2023
```

:::tip
You can override the location of your configuration file for `lhctl` by using the `--configFile` option.
:::

### Worker Configs

Workers are configured when you create an `LHConfig` object according to [our documentation](../../05-developer-guide/02-client-configuration.md#creating-the-lhconfig). You need to pass in certain properties (either directly to the `LHConfig` constructor, through a `Properties` file, or through environment variables).

For this example, your workers should be configured as follows:

```
LHC_API_HOST=localhost
LHC_API_PORT=2023
LHW_SERVER_CONNECT_LISTENER=PLAIN
```

:::tip
If you walk through our [quickstarts](../../05-developer-guide/00-install.md#get-started), the code in all three assumes that your configuration is set as environment variables.
:::
