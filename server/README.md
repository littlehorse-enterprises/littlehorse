# LittleHorse Server

## Running LH

The LH Server depends on Kafka as a backend database.

### With your own kafka

Write a `server.config` file with next settings:

```
LHS_KAFKA_BOOTSTRAP_SERVERS=path-to-kafka:9092
LHS_SHOULD_CREATE_TOPICS=true
```

Then:

```
./gradlew run --args="server.config"
```

### Using Scripts

Start a local kafka cluster:

```
../local-dev/setup.sh
```

Next, you can start the LH server itself. The server can be started in a single command:

```
../local-dev/do-server.sh
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

## Protobuf Compilation

```
../local-dev/compile-proto.sh
```

## Configurations

- [Server Configurations](../docs/SERVER_CONFIGURATIONS.md)

## gRPC Server Metrics

The Prometheus endpoint exports these metrics for public gRPC methods:

- `grpc_server_processing_duration_seconds_bucket`, `_count`, `_sum`, and `_max`: server processing duration in seconds, labeled by `lh_cluster_id`, `service`, `method`, `methodType`, and `statusCode`.
- `grpc_server_requests_received_messages_total` and `grpc_server_responses_sent_messages_total`: request and response message counts, labeled by `lh_cluster_id`, `service`, `method`, and `methodType`.

The duration histogram has finite buckets at 5ms, 10ms, 25ms, 50ms, 100ms, 250ms, 500ms, 1s, 2s, 5s, 10s, 30s, and 60s. Prometheus also exports the `+Inf` bucket. Each observed method/status label set therefore has 14 bucket series plus the existing count, sum, and max series, 17 series total.

Durations measure server-side call processing, not client round-trip latency. For streaming RPCs, the duration covers the lifetime of the stream until it closes; it is not a per-message duration.

Aggregate histogram buckets across server instances before calculating percentiles. For example:

```promql
histogram_quantile(0.50, sum by (lh_cluster_id, service, method, le) (rate(grpc_server_processing_duration_seconds_bucket[5m])))
histogram_quantile(0.95, sum by (lh_cluster_id, service, method, le) (rate(grpc_server_processing_duration_seconds_bucket[5m])))
histogram_quantile(0.99, sum by (lh_cluster_id, service, method, le) (rate(grpc_server_processing_duration_seconds_bucket[5m])))
```
