# Three LittleHorse Orchestrators

You can configure multiple LittleHorse Orchestrator brokers to communicate with one another. This provides workflow handling replication ensuring fault tolerance and high availability. For example, if one broker goes down, the remaining brokers can  facilitate a rebalance to keep your Workflows running smoothly.

This example shows how to run LittleHorse with three servers and a dashboard using Docker Compose without authentication.

## Docker Compose File

Save the following docker-compose file and run `docker compose up -d`.

```yaml
services:
  kafka:
    container_name: lh-kafka
    image: apache/kafka:3.8.0
    environment:
      ALLOW_PLAINTEXT_LISTENER: "yes"
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
    container_name: lh-server-1
    image: ghcr.io/littlehorse-enterprises/littlehorse/lh-server:0.10.1
    ports:
      - "2023:2023"
    environment:
      - LHS_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
      - LHS_CLUSTER_ID=my-cluster
      - LHS_INSTANCE_ID=1
      - LHS_SHOULD_CREATE_TOPICS=true
      - LHS_HEALTH_SERVICE_PORT=1822
      - LHS_INTERNAL_BIND_PORT=2011
      - LHS_INTERNAL_ADVERTISED_HOST=lh-server-1
      - LHS_INTERNAL_ADVERTISED_PORT=2011
      - LHS_LISTENERS=PLAIN:2023
      - LHS_ADVERTISED_LISTENERS=PLAIN://localhost:2023
  littlehorse-2:
    container_name: lh-server-2
    image: ghcr.io/littlehorse-enterprises/littlehorse/lh-server:0.10.1
    ports:
      - "2024:2024"
    environment:
      - LHS_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
      - LHS_CLUSTER_ID=my-cluster
      - LHS_INSTANCE_ID=2
      - LHS_SHOULD_CREATE_TOPICS=true
      - LHS_HEALTH_SERVICE_PORT=1822
      - LHS_INTERNAL_BIND_PORT=2021
      - LHS_INTERNAL_ADVERTISED_HOST=lh-server-2
      - LHS_INTERNAL_ADVERTISED_PORT=2021
      - LHS_LISTENERS=PLAIN:2024
      - LHS_ADVERTISED_LISTENERS=PLAIN://localhost:2024
  littlehorse-3:
    container_name: lh-server-3
    image: ghcr.io/littlehorse-enterprises/littlehorse/lh-server:0.10.1
    ports:
      - "2025:2025"
    environment:
      - LHS_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
      - LHS_CLUSTER_ID=my-cluster
      - LHS_INSTANCE_ID=3
      - LHS_SHOULD_CREATE_TOPICS=true
      - LHS_HEALTH_SERVICE_PORT=1822
      - LHS_INTERNAL_BIND_PORT=2031
      - LHS_INTERNAL_ADVERTISED_HOST=lh-server-3
      - LHS_INTERNAL_ADVERTISED_PORT=2031
      - LHS_LISTENERS=PLAIN:2025
      - LHS_ADVERTISED_LISTENERS=PLAIN://localhost:2025
  dashboard:
    container_name: lh-dashboard
    image: ghcr.io/littlehorse-enterprises/littlehorse/lh-dashboard:master
    environment:
      LHC_API_HOST: lh-server-1
      LHC_API_PORT: 2023
      LHD_OAUTH_ENABLED: false
    restart: on-failure
    healthcheck:
      test: curl -f localhost:3000
      interval: 5s
    ports:
      - "8080:3000"
```

## Using the Example

Once you have created the LH Cluster using Docker Compose, the next step is to access it. For example, you can use one of our [quickstarts](../../05-developer-guide/00-install.md#get-started) with the following configurations.

The dashboard can be accessed at [`http://localhost:8080`](http://localhost:8080).

You can connect to any of the three LittleHorse server instances in the cluster. In this example, the dashboard is configured to communicate with the server on port `2023`, but you could also connect with the servers on ports `2024` or `2025`.

### Worker Configs

Workers are configured when you create an `LHConfig` object according to [our documentation](../../05-developer-guide/02-client-configuration.md#creating-the-lhconfig). You need to pass in certain properties (either directly to the `LHConfig` constructor, through a `Properties` file, or through environment variables).

For this example, your workers should be configured as follows:

```
LHC_API_HOST=localhost

// You can address any of the three LittleHorse server ports
LHC_API_PORT=2023

LHW_SERVER_CONNECT_LISTENER=PLAIN
```

:::tip
If you walk through our [quickstarts](../../05-developer-guide/00-install.md#get-started), the code in all three assumes that your configuration is set as environment variables.
:::