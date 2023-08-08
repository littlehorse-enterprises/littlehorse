# Server Configurations

## Kafka

### `LHS_KAFKA_BOOTSTRAP_SERVERS`

A list of host/port pairs to use for establishing the initial connection to the Kafka cluster. [Kafka Official](https://kafka.apache.org/documentation/#streamsconfigs_bootstrap.servers).

- **Type:** string list
- **Default:** localhost:9092
- **Importance:** high

---

### `LHS_KAFKA_KEYSTORE`

The location of the key store file. This is optional for client and can be used for two-way authentication for client. [Kafka Official](https://kafka.apache.org/documentation/#brokerconfigs_ssl.keystore.location).

- **Type:** path
- **Default:** null
- **Importance:** medium

---

### `LHS_KAFKA_KEYSTORE_PASSWORD`

The store password for the key store file. This is optional. [Kafka Official](https://kafka.apache.org/documentation/#brokerconfigs_ssl.keystore.password).

- **Type:** string
- **Default:** null
- **Importance:** medium

---

### `LHS_KAFKA_KEYSTORE_PASSWORD_FILE`

The store password for the key store file. If it is different to null it overrides the `LHS_KAFKA_KEYSTORE_PASSWORD` config
and load the password from the file. This is optional.
[Kafka Official](https://kafka.apache.org/documentation/#brokerconfigs_ssl.keystore.password).

- **Type:** path
- **Default:** null
- **Importance:** medium

---

### `LHS_KAFKA_TRUSTSTORE`

The location of the trust store file. This is optional. [Kafka Official](https://kafka.apache.org/documentation/#brokerconfigs_ssl.truststore.location).

- **Type:** path
- **Default:** null
- **Importance:** medium

---

### `LHS_KAFKA_TRUSTSTORE_PASSWORD`

The password for the trust store file. This is optional. [Kafka Official](https://kafka.apache.org/documentation/#brokerconfigs_ssl.truststore.password).

- **Type:** string
- **Default:** null
- **Importance:** medium

---

### `LHS_KAFKA_TRUSTSTORE_PASSWORD_FILE`

The password for the trust store file. If it is different to null it overrides the `LHS_KAFKA_TRUSTSTORE_PASSWORD` config
and load the password from the file. This is optional. [Kafka Official](https://kafka.apache.org/documentation/#brokerconfigs_ssl.truststore.password).

- **Type:** path
- **Default:** null
- **Importance:** medium

## Server Internal Connections

### `LHS_INTERNAL_BIND_PORT`

Listening port for internal gRPC communications. It is use by the server instances to shared distributed data through
interactive queries. Should be unique.

- **Type:** int
- **Default:** 2011
- **Importance:** high

---

### `LHS_INTERNAL_ADVERTISED_HOST`

Ensures all server instances over the network can successfully connect to each other. It represents the
hostname that other server instances should us to connect to this server.

- **Type:** string
- **Default:** localhost
- **Importance:** high

---

### `LHS_INTERNAL_ADVERTISED_PORT`

Listening advertise port for internal communications. It represents the
TPC port that other server instances should us to connect to this server.

- **Type:** int
- **Default:** `${LHS_INTERNAL_BIND_PORT}`
- **Importance:** high

---

### `LHS_INTERNAL_SERVER_CERT`

Optional location of Server Cert file for TLS/mTLS connection.

- **Type:** path
- **Default:** null
- **Importance:** medium

---

### `LHS_INTERNAL_SERVER_KEY`

Optional location of Server Key file for TLS/mTLS connection.

- **Type:** path
- **Default:** null
- **Importance:** medium

---

### `LHS_INTERNAL_CA_CERT`

Optional location of CA Cert file that issued the client side certificates, used by mTLS connection.

- **Type:** path
- **Default:** null
- **Importance:** medium

## Server External Connections

### `LHS_LISTENERS`

List of comma-separated named ports the gRPC API will listen on.

Format: `<NAME>:<TPC PORT>`

Examples of legal listener lists: `MY_FIRST_LISTENER:2023,MY_SECOND_LISTENER:2024`

- **Type:** map
- **Default:** PLAIN:2023
- **Importance:** high

---

### `LHS_LISTENERS_PROTOCOL_MAP`

Map between listener names and security protocols. Depends on `LHS_LISTENERS`.

Format: `<LISTENER NAME>:<PROTOCOL>`

Examples of a legal list: `INSECURE:PLAIN,SECURE:TLS`

Allowed protocols: `PLAIN`, `TLS`, `MTLS`

- **Type:** map
- **Default:** PLAIN:PLAIN
- **Importance:** high

---

### `LHS_LISTENER_<LISTENER NAME>_CERT`

Optional location of Server Cert file for TLS/mTLS connection. Depends on `LHS_LISTENERS`.

Example:

```
LHS_LISTENERS=MY_LISTENER:2023
LHS_LISTENERS_PROTOCOL_MAP=MY_LISTENER:MTLS
LHS_LISTENER_MY_LISTENER_CERT=/tmp/certificate
...
```

- **Type:** path
- **Default:** null
- **Importance:** medium

---

### `LHS_LISTENER_<LISTENER NAME>_KEY`

Optional location of Server Key file for TLS/mTLS connection. Depends on `LHS_LISTENERS`.

Example:

```
LHS_LISTENERS=MY_LISTENER:2023
LHS_LISTENERS_PROTOCOL_MAP=MY_LISTENER:MTLS
LHS_LISTENER_MY_LISTENER_CERT=/tmp/certificate
LHS_LISTENER_MY_LISTENER_KEY=/tmp/key
...
```

- **Type:** path
- **Default:** null
- **Importance:** medium

---

### `LHS_LISTENER_<LISTENER NAME>_CA_CERT`

Optional location of CA Cert file that issued the client side certificates, used by mTLS connection. Depends on `LHS_LISTENERS`.

Example:

```
LHS_LISTENERS=MY_LISTENER:2023
LHS_LISTENERS_PROTOCOL_MAP=MY_LISTENER:MTLS
LHS_LISTENER_MY_LISTENER_CERT=/tmp/certificate
LHS_LISTENER_MY_LISTENER_KEY=/tmp/key
LHS_LISTENER_MY_LISTENER_CA_CERT=/tmp/ca
...
```

- **Type:** path
- **Default:** null
- **Importance:** medium

---

### `LHS_LISTENERS_AUTHORIZATION_MAP`

Map between listener names and authorization protocols. Depends on `LHS_LISTENERS`.

Format: `<LISTENER NAME>:<AUTH PROTOCOL>`

Examples of a legal list: `SECURE:OAUTH,INSECURE:NONE`

Allowed protocols: `NONE`, `OAUTH`

- **Type:** map
- **Default:** null
- **Importance:** high

---

### `LHS_LISTENER_<LISTENER NAME>_AUTHORIZATION_SERVER`

Optional Authorization Server URL. Used by the server to know the OpenId Connect endpoints.
Depends on `LHS_LISTENERS`.

Example:

```
LHS_LISTENERS=MY_LISTENER:2023
LHS_LISTENERS_AUTHORIZATION_MAP=MY_LISTENER:OAUTH
LHS_LISTENER_MY_LISTENER_AUTHORIZATION_SERVER=http://localhost:8888/realms/lh
...
```

- **Type:** url
- **Default:** null
- **Importance:** medium

---

### `LHS_LISTENER_<LISTENER NAME>_CLIENT_ID`

Optional OAuth2 Client Id. Used by the Worker to identify itself at an Authorization Server. Client Credentials Flow.
Depends on `LHS_LISTENERS`.

Example:

```
LHS_LISTENERS=MY_LISTENER:2023
LHS_LISTENERS_AUTHORIZATION_MAP=MY_LISTENER:OAUTH
LHS_LISTENER_MY_LISTENER_CLIENT_ID=server
...
```

- **Type:** string
- **Default:** null
- **Importance:** medium

---

### `LHS_LISTENER_<LISTENER NAME>_CLIENT_ID_FILE`

Optional OAuth2 Client Id. Used by the Worker to identify itself at an Authorization Server. Client Credentials Flow.
Depends on `LHS_LISTENERS`. If it is different to null it overrides the `LHS_LISTENER_<LISTENER NAME>_CLIENT_ID` config
and load the client id from the file.

Example:

```
LHS_LISTENERS=MY_LISTENER:2023
LHS_LISTENERS_AUTHORIZATION_MAP=MY_LISTENER:OAUTH
LHS_LISTENER_MY_LISTENER_CLIENT_ID_FILE=/temp/client_id
...
```

- **Type:** path
- **Default:** null
- **Importance:** medium

---

### `LHS_LISTENER_<LISTENER NAME>_CLIENT_SECRET`

Optional OAuth2 Client Secret. Used by the Worker to identify itself at an Authorization Server. Client Credentials Flow.
Depends on `LHS_LISTENERS`.

Example:

```
LHS_LISTENERS=MY_LISTENER:2023
LHS_LISTENERS_AUTHORIZATION_MAP=MY_LISTENER:OAUTH
LHS_LISTENER_MY_LISTENER_CLIENT_SECRET=3bdca420cf6c48e2aa4f56d46d6327e0
...
```

- **Type:** string
- **Default:** null
- **Importance:** medium

---

### `LHS_LISTENER_<LISTENER NAME>_CLIENT_SECRET_FILE`

Optional OAuth2 Client Secret. Used by the Worker to identify itself at an Authorization Server. Client Credentials Flow.
Depends on `LHS_LISTENERS`. If it is different to null it overrides the `LHS_LISTENER_<LISTENER NAME>_CLIENT_SECRET` config
and load the secret from the file.

Example:

```
LHS_LISTENERS=MY_LISTENER:2023
LHS_LISTENERS_AUTHORIZATION_MAP=MY_LISTENER:OAUTH
LHS_LISTENER_MY_LISTENER_CLIENT_SECRET_FILE=/temp/client_secret
...
```

- **Type:** path
- **Default:** null
- **Importance:** medium

---

### `LHS_ADVERTISED_LISTENERS`

It represents the hostnames and ports that clients and workers should us to connect to this server.

Format: `<NAME>://<HOSTNAME>:<TPC PORT>`

Examples of legal advertised listener lists: `FOR_WORKERS://localhost:2023,FOR_LHCTL://127.0.0.1:2023`

- **Type:** string
- **Default:** PLAIN://localhost:2023
- **Importance:** high

## Server Behavior

### `LHS_CLUSTER_ID`

An identifier for the stream processing application. Must be unique within the LittleHorse cluster. It is used as 1) the default `client-id` prefix, 2) the `userGroup-id` for membership management, 3) the changelog topic prefix. [Kafka Official](https://kafka.apache.org/documentation/#streamsconfigs_application.id).

- **Type:** string
- **Default:** cluster1
- **Importance:** high

---

### `LHS_INSTANCE_ID`

A unique identifier of the consumer instance provided by the end user. [Kafka Official](https://kafka.apache.org/documentation/#consumerconfigs_group.instance.id).

- **Type:** string
- **Default:** server1
- **Importance:** high

---

### `LHS_RACK_ID`

Provides rack awareness to the cluster. [Kafka Official](https://kafka.apache.org/documentation/#streamsconfigs_rack.aware.assignment.tags).

- **Type:** string
- **Default:** unset-rack-id
- **Importance:** medium

---

### `LHS_SHOULD_CREATE_TOPICS`

Defines if the server should create its own topics. It is useful for development environments. *Disable this for production environments* `LHS_SHOULD_CREATE_TOPICS=false`.

- **Type:** boolean
- **Default:** true
- **Importance:** low

---

### `LHS_REPLICATION_FACTOR`

The replication factor for change log topics and repartition topics created by the stream processing application. [Kafka Official](https://kafka.apache.org/documentation/#streamsconfigs_replication.factor). Disabled if `LHS_SHOULD_CREATE_TOPICS=false`.

- **Type:** int
- **Default:** 1
- **Importance:** low

---

### `LHS_CLUSTER_PARTITIONS`

The number of partitions in each internal kafka topic. Disabled if `LHS_SHOULD_CREATE_TOPICS=false`.

- **Type:** int
- **Default:** 72
- **Importance:** low

---

### `LHS_KAFKA_TOPIC_PREFIX`

Prefix for each internal topic. Disabled if `LHS_SHOULD_CREATE_TOPICS=false`.

- **Type:** string
- **Default:** `${LHS_CLUSTER_ID}-`
- **Importance:** low

---

### `LHS_STREAMS_NUM_THREADS`

The number of threads to execute stream processing. [Kafka Official](https://kafka.apache.org/documentation/#streamsconfigs_num.stream.threads).

- **Type:** int
- **Default:** 1
- **Importance:** medium

---

### `LHS_STREAMS_SESSION_TIMEOUT`

The timeout used to detect client failures when using Kafka's userGroup management facility. [Kafka Official](https://kafka.apache.org/documentation/#consumerconfigs_session.timeout.ms).

- **Type:** int
- **Default:** 30000 (30 seconds)
- **Importance:** high

---

### `LHS_STREAMS_COMMIT_INTERVAL`

The frequency in milliseconds with which to commit processing progress. [Kafka Official](https://kafka.apache.org/documentation/#streamsconfigs_commit.interval.ms).

- **Type:** int
- **Default:** 100
- **Importance:** low

---

### `LHS_STATE_DIR`

Directory location for state store. This path must be unique for each streams instance sharing the same underlying filesystem. [Kafka Official](https://kafka.apache.org/documentation/#streamsconfigs_state.dir).

- **Type:** path
- **Default:** /tmp/kafkaState
- **Importance:** high

---

### `LHS_STREAMS_NUM_STANDBY_REPLICAS`

The number of standby replicas for each task. [Kafka Official](https://kafka.apache.org/documentation/#streamsconfigs_num.standby.replicas).

- **Type:** int
- **Default:** 0
- **Importance:** high

---

### `LHS_STREAMS_NUM_WARMUP_REPLICAS`

The maximum number of warmup replicas (extra standbys beyond the configured num.standbys) that can be assigned at once for the purpose of keeping the task available on one instance while it is warming up on another instance it has been reassigned to. [Kafka Official](https://kafka.apache.org/documentation/#streamsconfigs_max.warmup.replicas).

- **Type:** int
- **Default:** 12
- **Importance:** medium

---

### `LHS_DEFAULT_WFRUN_RETENTION_HOURS`

Default total hours of life that a `WfRun` will live in the system in case this value was not set when
creating the `WfSpec`.

- **Type:** int
- **Default:** 168
- **Importance:** low

---

### `LHS_DEFAULT_EXTERNAL_EVENT_RETENTION_HOURS`

Default total hours of life that a `ExternalEvent` will live in the system in case this value was not set when
creating the `ExternalEventDef`.

- **Type:** int
- **Default:** 168
- **Importance:** low

## Monitoring

### `LHS_PROMETHEUS_EXPORTER_PORT`

The port to scrape metrics from.

- **Type:** int
- **Default:** 1822
- **Importance:** low

---

### `LHS_PROMETHEUS_EXPORTER_PATH`

The path to scrape metrics from.

- **Type:** string
- **Default:** /metrics
- **Importance:** low
