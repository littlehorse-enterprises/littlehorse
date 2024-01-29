# Server Configurations
- [Server Configurations](#server-configurations)
  - [Kafka](#kafka)
    - [`LHS_KAFKA_BOOTSTRAP_SERVERS`](#lhs_kafka_bootstrap_servers)
    - [`LHS_KAFKA_KEYSTORE`](#lhs_kafka_keystore)
    - [`LHS_KAFKA_KEYSTORE_PASSWORD`](#lhs_kafka_keystore_password)
    - [`LHS_KAFKA_KEYSTORE_PASSWORD_FILE`](#lhs_kafka_keystore_password_file)
    - [`LHS_KAFKA_TRUSTSTORE`](#lhs_kafka_truststore)
    - [`LHS_KAFKA_TRUSTSTORE_PASSWORD`](#lhs_kafka_truststore_password)
    - [`LHS_KAFKA_TRUSTSTORE_PASSWORD_FILE`](#lhs_kafka_truststore_password_file)
  - [Server Internal Connections](#server-internal-connections)
    - [`LHS_INTERNAL_BIND_PORT`](#lhs_internal_bind_port)
    - [`LHS_INTERNAL_ADVERTISED_HOST`](#lhs_internal_advertised_host)
    - [`LHS_INTERNAL_ADVERTISED_PORT`](#lhs_internal_advertised_port)
    - [`LHS_INTERNAL_SERVER_CERT`](#lhs_internal_server_cert)
    - [`LHS_INTERNAL_SERVER_KEY`](#lhs_internal_server_key)
    - [`LHS_INTERNAL_CA_CERT`](#lhs_internal_ca_cert)
  - [Server External Connections](#server-external-connections)
    - [`LHS_LISTENERS`](#lhs_listeners)
    - [`LHS_LISTENERS_PROTOCOL_MAP`](#lhs_listeners_protocol_map)
    - [`LHS_LISTENER_<LISTENER NAME>_CERT`](#lhs_listener_listener-name_cert)
    - [`LHS_LISTENER_<LISTENER NAME>_KEY`](#lhs_listener_listener-name_key)
    - [`LHS_CA_CERT`](#lhs_ca_cert)
    - [`LHS_LISTENERS_AUTHENTICATION_MAP`](#lhs_listeners_authentication_map)
    - [`LHS_OAUTH_INTROSPECT_URL`](#lhs_oauth_introspect_url)
    - [`LHS_OAUTH_CLIENT_ID`](#lhs_oauth_client_id)
    - [`LHS_OAUTH_CLIENT_ID_FILE`](#lhs_oauth_client_id_file)
    - [`LHS_OAUTH_CLIENT_SECRET`](#lhs_oauth_client_secret)
    - [`LHS_OAUTH_CLIENT_SECRET_FILE`](#lhs_oauth_client_secret_file)
    - [`LHS_ADVERTISED_LISTENERS`](#lhs_advertised_listeners)
  - [Server Behavior](#server-behavior)
    - [`LHS_CLUSTER_ID`](#lhs_cluster_id)
    - [`LHS_INSTANCE_ID`](#lhs_instance_id)
    - [`LHS_RACK_ID`](#lhs_rack_id)
    - [`LHS_SHOULD_CREATE_TOPICS`](#lhs_should_create_topics)
    - [`LHS_REPLICATION_FACTOR`](#lhs_replication_factor)
    - [`LHS_CLUSTER_PARTITIONS`](#lhs_cluster_partitions)
    - [`LHS_CORE_STREAM_THREADS`](#lhs_core_stream_threads)
    - [`LHS_TIMER_STREAM_THREADS`](#lhs_timer_stream_threads)
    - [`LHS_NUM_NETWORK_THREADS`](#lhs_num_network_threads)
    - [`LHS_STREAMS_SESSION_TIMEOUT`](#lhs_streams_session_timeout)
    - [`LHS_CORE_STREAMS_COMMIT_INTERVAL`](#lhs_core_streams_commit_interval)
    - [`LHS_TIMER_STREAMS_COMMIT_INTERVAL`](#lhs_timer_streams_commit_interval)
    - [`LHS_STATE_DIR`](#lhs_state_dir)
    - [`LHS_STREAMS_NUM_STANDBY_REPLICAS`](#lhs_streams_num_standby_replicas)
    - [`LHS_STREAMS_NUM_WARMUP_REPLICAS`](#lhs_streams_num_warmup_replicas)
    - [`LHS_CORE_MEMTABLE_SIZE_BYTES`](#lhs_core_memtable_size_bytes)
    - [`LHS_TIMER_MEMTABLE_SIZE_BYTES`](#lhs_timer_memtable_size_bytes)
    - [`LHS_CORE_STATESTORE_CACHE_BYTES`](#lhs_core_statestore_cache_bytes)
    - [`LHS_TIMER_STATESTORE_CACHE_BYTES`](#lhs_timer_statestore_cache_bytes)
    - [`LHS_ROCKSDB_TOTAL_BLOCK_CACHE_BYTES`](#lhs_rocksdb_total_block_cache_bytes)
    - [`LHS_ROCKSDB_TOTAL_MEMTABLE_BYTES`](#lhs_rocksdb_total_memtable_bytes)
  - [Monitoring](#monitoring)
    - [`LHS_HEALTH_SERVICE_PORT`](#lhs_health_service_port)
    - [`LHS_HEALTH_PATH_METRICS`](#lhs_health_path_metrics)
    - [`LHS_HEALTH_PATH_READINESSS`](#lhs_health_path_readinesss)
    - [`LHS_HEALTH_PATH_LIVENESS`](#lhs_health_path_liveness)

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

### `LHS_CA_CERT`

Optional location of CA Cert file that issued the client side certificates, used by mTLS connections. Refers to the
Certificate Authority to trust to sign certs for the clients. This CA Cert is applied to all the listeners.

Example:

```
LHS_LISTENERS=MY_LISTENER:2023
LHS_LISTENERS_PROTOCOL_MAP=MY_LISTENER:MTLS
LHS_LISTENER_MY_LISTENER_CERT=/tmp/certificate
LHS_LISTENER_MY_LISTENER_KEY=/tmp/key
LHS_CA_CERT=/tmp/ca
...
```

- **Type:** path
- **Default:** null
- **Importance:** medium

---

### `LHS_LISTENERS_AUTHENTICATION_MAP`

Map between listener names and authentication mechanisms. Depends on `LHS_LISTENERS`.

> `LHS_LISTENERS_PROTOCOL_MAP` should be set to `MTLS` for a listener in order to use `MTLS` as the authentication mechanism.

Format: `<LISTENER NAME>:<AUTH PROTOCOL>`

Examples of a legal list: `SECURE:OAUTH,INSECURE:NONE`

Allowed protocols: `NONE`, `OAUTH`, `MTLS`

- **Type:** map
- **Default:** null
- **Importance:** high

---

### `LHS_OAUTH_INTROSPECT_URL`

Optional OAuth server introspection URL. Used by the server to authenticate tokens from incoming calls.
This OAuth server introspection URL is used by all `OAUTH` listeners.

> Needs to be set if any listener specifies `OAUTH` as an authentication mechanism in `LHS_LISTENERS_AUTHENTICATION_MAP`

Example:

```
LHS_LISTENERS=MY_LISTENER:2023
LHS_LISTENERS_AUTHENTICATION_MAP=MY_LISTENER:OAUTH
LHS_OAUTH_INTROSPECT_URL=http://localhost:8888/realms/lh/protocol/openid-connect/token/introspect
...
```

- **Type:** url
- **Default:** null
- **Importance:** medium

---

### `LHS_OAUTH_CLIENT_ID`

Optional OAuth2 Client Id. This OAuth client id is used by all `OAUTH` listeners, and authenticates all incoming calls against the OAuth server.

Example:

```
LHS_LISTENERS=MY_LISTENER:2023
LHS_LISTENERS_AUTHENTICATION_MAP=MY_LISTENER:OAUTH
LHS_OAUTH_CLIENT_ID=server
...
```

- **Type:** string
- **Default:** null
- **Importance:** medium

---

### `LHS_OAUTH_CLIENT_ID_FILE`

Optional OAuth2 Client Id. This OAuth client id is used by all `OAUTH` listeners, and authenticates all incoming calls against the OAuth server.
If it is different to null it overrides the `LHS_OAUTH_CLIENT_ID` config and loads the client id from the file.

Example:

```
LHS_LISTENERS=MY_LISTENER:2023
LHS_LISTENERS_AUTHENTICATION_MAP=MY_LISTENER:OAUTH
LHS_OAUTH_CLIENT_ID_FILE=/temp/client_id
...
```

- **Type:** path
- **Default:** null
- **Importance:** medium

---

### `LHS_OAUTH_CLIENT_SECRET`

Optional OAuth2 Client Secret. This OAuth client secret is used by all `OAUTH` listeners, and authenticates all incoming calls against the OAuth server.

Example:

```
LHS_LISTENERS=MY_LISTENER:2023
LHS_LISTENERS_AUTHENTICATION_MAP=MY_LISTENER:OAUTH
LHS_OAUTH_CLIENT_SECRET=3bdca420cf6c48e2aa4f56d46d6327e0
...
```

- **Type:** string
- **Default:** null
- **Importance:** medium

---

### `LHS_OAUTH_CLIENT_SECRET_FILE`

Optional OAuth2 Client Secret. This OAuth client secret is used by all `OAUTH` listeners, and authenticates all incoming calls against the OAuth server.
If it is different to null it overrides the `LHS_OAUTH_CLIENT_SECRET` config and loads the secret from the file.

Example:

```
LHS_LISTENERS=MY_LISTENER:2023
LHS_LISTENERS_AUTHENTICATION_MAP=MY_LISTENER:OAUTH
LHS_OAUTH_CLIENT_SECRET_FILE=/temp/client_secret
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

An identifier for the stream processing application. Must be unique within the LittleHorse cluster. It is used as 1) the default `client-id` prefix, 2) the `group-id` for membership management, 3) the changelog topic prefix. [Kafka Official](https://kafka.apache.org/documentation/#streamsconfigs_application.id).

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

Provides rack awareness to the cluster. Used in two ways:

* To ensure that standby tasks are scheduled in different rack's than their active tasks ([Kafka Official](https://kafka.apache.org/documentation/#streamsconfigs_rack.aware.assignment.tags)).
* To enable follower fetching for standby tasks. Reduces networking costs without impacting application performance.

- **Type:** string
- **Default:** null
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

The number of partitions in each internal kafka topic. Necessary whether or not `LHS_SHOULD_CREATE_TOPICS` is set.

- **Type:** int
- **Default:** 12
- **Importance:** high

---

### `LHS_CORE_STREAM_THREADS`

The number of threads to execute stream processing in the Core Topology. [Kafka Official](https://kafka.apache.org/documentation/#streamsconfigs_num.stream.threads). For a server with `N` cores, we recommend setting this to `N * 0.6`.

- **Type:** int, >= 1
- **Default:** 1
- **Importance:** medium

---

### `LHS_TIMER_STREAM_THREADS`

The number of threads to execute stream processing in the Timer Topology. [Kafka Official](https://kafka.apache.org/documentation/#streamsconfigs_num.stream.threads). For a server with `N` cores, we recommend setting this to `N * 0.4`.

- **Type:** int, >= 1
- **Default:** 1
- **Importance:** medium

---

### `LHS_NUM_NETWORK_THREADS`

The size of the shared Threadpool used by the grpc listeners.

- **Type:** int
- **Default:** 2
- **Minimum:** 2
- **Importance:** medium

---

### `LHS_STREAMS_SESSION_TIMEOUT`

The timeout used to detect client failures when using Kafka's group management facility. [Kafka Official](https://kafka.apache.org/documentation/#consumerconfigs_session.timeout.ms).

- **Type:** int
- **Default:** 30000 (30 seconds)
- **Importance:** high

---

### `LHS_CORE_STREAMS_COMMIT_INTERVAL`

The frequency in milliseconds with which to commit processing progress on the Core Topology. [Kafka Official](https://kafka.apache.org/documentation/#streamsconfigs_commit.interval.ms). For the Core Topology, we recommend setting it to 5000 milliseconds. A large enough value along with a large value for `LHS_CORE_STATESTORE_CACHE_BYTES` will result in fewer records emitted to the Kafka Streams Changelog topics.

- **Type:** int
- **Default:** 5000
- **Importance:** medium

---

### `LHS_TIMER_STREAMS_COMMIT_INTERVAL`

The frequency in milliseconds with which to commit processing progress on the Timer Topology. [Kafka Official](https://kafka.apache.org/documentation/#streamsconfigs_commit.interval.ms). For the Timer Topology, we recommend setting it to 30000 milliseconds. A large enough value along with a large value for `LHS_TIMER_STATESTORE_CACHE_BYTES` will result in fewer records emitted to the Kafka Streams Changelog topics.

- **Type:** int
- **Default:** 30000
- **Importance:** medium

---

### `LHS_STATE_DIR`

Directory location for state store. This path must be unique for each streams instance sharing the same underlying filesystem. [Kafka Official](https://kafka.apache.org/documentation/#streamsconfigs_state.dir).

- **Type:** path
- **Default:** /tmp/kafkaState
- **Importance:** high

---

### `LHS_STREAMS_NUM_STANDBY_REPLICAS`

The number of standby replicas for each task. Applies to both Core and Timer topologies. [Kafka Official](https://kafka.apache.org/documentation/#streamsconfigs_num.standby.replicas).

- **Type:** int
- **Default:** 0
- **Importance:** high

---

### `LHS_STREAMS_NUM_WARMUP_REPLICAS`

The maximum number of warmup replicas (extra standbys beyond the configured num.standbys) that can be assigned at once for the purpose of keeping the task available on one instance while it is warming up on another instance it has been reassigned to. [Kafka Official](https://kafka.apache.org/documentation/#streamsconfigs_max.warmup.replicas).

The same config is used by both the Core and Timer Topologies. Note that if you set `LHS_STREAMS_NUM_WARMUP_REPLICAS = N`, then there can be up to `2 * N` warmup replicas scheduled.

- **Type:** int
- **Default:** 12
- **Importance:** medium

---

### `LHS_CORE_MEMTABLE_SIZE_BYTES`

The size of a RocksDB Memtable (aka Write Buffer) for the Core Topology.

- **Type:** long
- **Default:** 67108864 (64MB)
- **Importance:** low

---

### `LHS_TIMER_MEMTABLE_SIZE_BYTES`

The size of a RocksDB Memtable (aka Write Buffer) for the Timer Topology.

- **Type:** long
- **Default:** 33554432 (32MB)
- **Importance:** low

---

### `LHS_CORE_STATESTORE_CACHE_BYTES`

The size of the Kafka Streams State Store Cache on the Core Topology. This cache is put in front of RocksDB (i.e. before any writes to the Memtable) and is flushed on every Streams Commit (`LHS_CORE_STREAMS_COMMIT_INTERVAL`). This cache is shared by all Core Topology state stores on a server. A large enough value will result in fewer records emitted to the Kafka Streams Changelog topic.

- **Type:** long
- **Default:** 33554432 (32MB)
- **Importance:** high

---

### `LHS_TIMER_STATESTORE_CACHE_BYTES`

The size of the Kafka Streams State Store Cache on the Timer Topology. This cache is put in front of RocksDB (i.e. before any writes to the Memtable) and is flushed on every Streams Commit (`LHS_TIMER_STREAMS_COMMIT_INTERVAL`). A large enough value will result in fewer records emitted to the Kafka Streams Changelog topic.

- **Type:** long
- **Default:** 67108864 (64MB)
- **Importance:** high

---

### `LHS_ROCKSDB_TOTAL_BLOCK_CACHE_BYTES`

The size of the shared Block Cache for reads into RocksDB. Memory used by this cache is allocated off-heap. If not set, then there is no limit and the Kafka Streams default is used (each RocksDB instance gets its own 50-MB cache).

- **Type:** long
- **Default:** null
- **Importance:** low

---

### `LHS_ROCKSDB_TOTAL_MEMTABLE_BYTES`

The capacity of the Rocksdb Write Buffer Manager. Memory used by the Write Buffer Manager is allocated off-heap. If not set, then there is no limit for off-heap memory allocated to memtables.

- **Type:** long
- **Default:** null
- **Importance:** high

## Monitoring

### `LHS_HEALTH_SERVICE_PORT`

The port that the healthchecks and prometheus metrics are exposed on.

- **Type:** int
- **Default:** 1822
- **Importance:** low

---

### `LHS_HEALTH_PATH_METRICS`

The path to scrape metrics from.

- **Type:** string
- **Default:** /metrics
- **Importance:** low

---

### `LHS_HEALTH_PATH_READINESSS`

The path upon which application readiness (the ability to serve requests) is exposed.

- **Type:** string
- **Default:** /readiness
- **Importance:** low

---

### `LHS_HEALTH_PATH_LIVENESS`

The path upon which application liveness (the ability to ).

- **Type:** string
- **Default:** /metrics
- **Importance:** low
