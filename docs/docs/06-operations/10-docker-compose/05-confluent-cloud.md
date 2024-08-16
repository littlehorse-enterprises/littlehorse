# Using Confluent Cloud

LittleHorse requires [Apache Kafka](https://kafka.apache.org) as its only hard dependency. Kafka is the source-of-truth for the state of a LittleHorse Cluster as all data is stored in Kafka Streams state stores (therefore, the durability of the Changelog Topics determines the durability of the LittleHorse Cluster's data).

Managing Kafka in production is difficult; so users of our open-source community may wish to use a hosted SaaS version of Kafka. The leading Kafka Cloud Service is provided by [Confluent](https://confluent.io), a company founded by the creators of Apache Kafka.

This example shows you how to connect your LittleHorse cluster to Confluent Cloud.

## Docker Compose File

Confluent Cloud uses the `SASL_SSL` protocol with the `PLAIN` Sasl mechanism. before running this example, you must:

1. Create a Confluent Cloud cluster and an API key. You can do so on [Confluent's Cloud Console](https://confluent.cloud/).
2. Download the API Key file. It should look like the following:

```
=== Confluent Cloud API key: lkc-0595zp ===

API key:
XXXXXXXXXXXXXXXX

API secret:
xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx+xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

Bootstrap server:
pkc-n98pk.us-west-2.aws.confluent.cloud:9092
```

LittleHorse will expect the `LHS_SASL_JAAS_CONFIG_FILE` environment variable, which points to a flat file on the LH Server's file system whose contents are the required `sasl.jaas.config`. Your `sasl.jaas.config` should look like:

```
org.apache.kafka.common.security.plain.PlainLoginModule required username='<API key>' password='<API secret>';
```

Let's do the following:

```
API_KEY=<your api key from cflt cloud console>
API_SECRET=<your api secret from cflt cloud console>

# This is for demo purposes. In production please do something more secure.
mkdir /tmp/cflt-creds
echo "org.apache.kafka.common.security.plain.PlainLoginModule required username='$API_KEY' password='$API_SECRET';" > /tmp/cflt-creds/sasl-jaas-config.txt
```

Replace the `<API key>` and `<API secret>` with the values you downloaded from Confluent Cloud, and then run the above script.

Save the following docker-compose file and run: `docker compose up -d`.

:::caution
Remember to substitute your bootstrap servers!
:::

```yaml
services:
  littlehorse:
    container_name: lh-server
    image: ghcr.io/littlehorse-enterprises/littlehorse/lh-server:0.10.1
    volumes:
      # Mount a volume to give the LH Server container access to your Confluent creds.
      - /tmp/cflt-creds:/secrets
    environment:
      # NOTE: Please replace your bootstrap servers
      LHS_KAFKA_BOOTSTRAP_SERVERS: <your-bootstrap-servers-from-confluent>

      # That this comes from where you mount the volume a few lines above
      LHS_KAFKA_SASL_JAAS_CONFIG_FILE: /secrets/sasl-jaas-config.txt
      LHS_KAFKA_SASL_MECHANISM: PLAIN
      LHS_KAFKA_SECURITY_PROTOCOL: SASL_SSL
      LHS_SHOULD_CREATE_TOPICS: "true"
      LHS_HEALTH_SERVICE_PORT: "1822"

      LHS_REPLICATION_FACTOR: "3"
    restart: on-failure
    healthcheck:
      test: curl -f localhost:1822/liveness
      interval: 5s
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
    ports:
      - "8080:3000"
```

## Configuring the Clients

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
