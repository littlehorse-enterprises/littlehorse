# Workflow Metrics

**Authors:** Eduwer Camacaro, Christian Caicedo


## Motivation

LittleHorse Server orchestrates workflows that represent real business processes and enables engineers to implement durable technical solutions. However, once workflows are running, there is currently no built-in way to observe how they behave over time.

Teams lack visibility into basic questions such as execution rates, latency, failure patterns, and workflow health. As a result, performance issues, regressions, and business-impacting delays are often detected late or inferred indirectly through logs and external systems.

This proposal introduces **workflow metrics** as a first-class feature in LittleHorse, enabling both technical and business users to understand workflow behavior using structured, queryable data.

### Out of scope

* **Counted Tags** — A separate proposal will handle count-at-a-single-instant queries (e.g., how many NodeRuns are currently in TASK_SCHEDULED). These require a different architectural treatment.
* **P95 approximations** — While desirable, approximate P95s via mergeable sketches (DDSketch) are left for a follow-up as they add significant complexity to the pipeline.
* **Exporting MetricReadings** — The MetricReadings will be retrievable via the gRPC API. This proposal does not include building additional exporters (e.g., Datadog); users may build their own exporters using the gRPC clients. The LH Dashboard will provide a user-friendly interface to explore the metrics.


## Current Problem

### Lack of Observability

Today, LittleHorse provides no native mechanism to answer questions like:

* How many workflows are running or completing per unit of time?
* How long does a workflow or task usually take?
* Which node or task is the main bottleneck?
* Are error rates increasing after a deployment?

### External Workarounds

To answer these questions, teams typically rely on:

* Application logs
* Custom instrumentation inside workers
* External monitoring systems

These approaches are:

* Inconsistent across teams
* Hard to correlate with workflow structure
* Operationally expensive
* Detached from LittleHorse’s execution model


## Proposed Solution

We propose introducing **workflow metrics** with a server-defined set of default `MetricSpec`s that are loaded at startup. Clients can query collected metrics via read-only APIs (gRPC), but cannot create or mutate metric definitions at runtime;

Metrics are:

* Pre-defined in `DefaultMetricsRegistry` (server-side)
* Aggregated over mergeable time windows
* Computed internally by the LittleHorse Server
* Stored and queried using native APIs

No external systems or dependencies are required for collection (users can export data via Kafka output topics or build exporters). For future scope, the server could optionally publish aggregated metric windows directly to a Kafka output topic to enable real-time consumption and integration with external analytics or monitoring pipelines.

## Key Decisions

* **No public metric spec mutation**: There will be no public RPC to create or mutate `MetricSpec`s. The system ships with server-side defaults in `DefaultMetricsRegistry` which is loaded at startup.
* **Default windowing**: Default windows are 5 minute tumbling windows; windows are mergeable and persisted for a configurable retention (default 14 days).
* **Metric recording levels**: `INFO`, `NONE`, and `DEBUG` recording levels control which metrics are collected; flamegraph/debug-level metrics are only enabled at `DEBUG`.
* **Read-only query surface**: Clients query metrics through read-only gRPC endpoints; exports to external systems should be done via Kafka output topics or custom exporters.
* **Tenant-scoped metrics**: All metrics are tenant-scoped for multi-tenancy isolation. Global metrics apply cluster-wide within a single tenant, while scoped metrics target specific workflows or tasks within that tenant.


## Scope

### Supported Metric Types

This proposal introduces **two metric types**:

1. **Rate metrics**
   Measure how often an event occurs in a time window
   *Example: number of `TaskRun`s in the last 5 minutes*

2. **Latency metrics**
   Measure time between two status transitions
   *Example: time from `TASK_SCHEDULED` → `TASK_SUCCESS`*



### Supported Entities

Metrics can be collected for:

* `WfRun`
* `ThreadRun`
* `NodeRun`
* `TaskRun`
* `UserTaskRun`

Metrics can be scoped at:

* Workflow level (`WfSpec`)
* Thread level
* Node level
* Task level



## Design Principles

### Explicit Metric Definition

Metrics are only collected when explicitly defined via `MetricSpec`, unless auto-metrics are enabled.

This avoids:

* Unbounded storage growth
* Unnecessary computation
* Collecting metrics that are never queried



### No External Dependencies

All metric collection and aggregation happens inside LittleHorse using:

* Kafka Streams
* Native state stores
* Repartitioning for global aggregation



### Minimal Performance Impact

* Metrics are aggregated in time windows
* Computation happens per partition first
* Only finalized events produce metric updates
* **Minimum window length**: 1 minute




### Non-Intrusive Execution

Metric collection does **not** affect workflow semantics or execution order.
Existing workflows continue to work unchanged.


### Mergeable windows & retention

* Default windows are **5 minute windows** aligned to the epoch for operational simplicity.
* Each window is persisted for **14 days** (configurable) to allow for historical queries.
* Metric windows are **mergeable** — we store totals (e.g. `total_latency_ms` and `count`) instead of `latency_avg` so windows can be added together to form larger window.

### Metric recording levels

By default metrics are collected at a conservative level to avoid performance regressions. We recommend the following levels:

```proto
enum MetricRecordingLevel {
  INFO = 0;  // Collect non-intrusive defaults
  NONE = 1;  // Collect no metrics unless explicitly configured
  DEBUG = 2; // Collect detailed/expensive metrics (e.g. flamegraphs)
}
```

A tenant-level default controls the recording level for that tenant. For example, the `Tenant` proto could include a field such as:

```proto
message Tenant {
  // ... existing fields ...

  // The default level of metrics to record in a given `Tenant`.
  MetricRecordingLevel metrics_level = 4;
}
```

The server-side `DefaultMetricsRegistry` may also contain per-`WfSpec` or per-`TaskDef` overrides; these overrides are managed by operators via configuration (not public RPCs).

### Metric Recording Level Resolution Logic

Metric recording levels are resolved hierarchically for each metric collection event:

1. **Check for specific overrides**: Look up `MetricLevelOverride` objects for the specific `WfSpec`, `TaskDef`, or `Node` involved.
2. **Fall back to tenant default**: If no specific override exists, use the `Tenant.metrics_level` field.
3. **Fall back to server default**: If no tenant default is set, use the server-wide default ( `INFO`).

This allows granular control: e.g., enable `DEBUG` metrics for a problematic workflow while keeping others at `INFO`.


## Metric Model

### Rate Metrics

Count how many events occur in a window.

**Examples**

* Number of completed `WfRun`s
* Number of failed `TaskRun`s



### Latency Metrics

Measure time between two status transitions.
Latency metrics are only available for entities with a status field.

#### Supported Status-Based Entities

* `TaskRun`
* `UserTaskRun`
* `WfRun`
* `ThreadRun`
* `NodeRun`



#### Example: TaskRun Latency

| From Status      | To Status      | Meaning            |
| - | -------- | ------------------ |
| `TASK_SCHEDULED` | `TASK_RUNNING` | Queue latency      |
| `TASK_RUNNING`   | `TASK_SUCCESS` | Execution latency  |
| `TASK_SCHEDULED` | `TASK_SUCCESS` | Total task latency |


## Metrics configuration & Public API

**Important:** Metric *specs* are *not* created by end-users at runtime. Instead, LittleHorse ships with a server-side class containing the default set of metrics (e.g. `DefaultMetricsRegistry`) which is loaded when the server starts. Operators may customize that set via configuration or code before startup, but there is no public RPC that allows clients to create or mutate metric specs at runtime. This reduces surface area, avoids unbounded/incorrect metric definitions, and ensures sane defaults for all deployments.

### Query API (read-only)

The following RPCs remain public so that clients (dashboards, operators) can read the collected metrics and historical windows.

```proto
rpc ListMetrics(ListMetricsRequest) returns (MetricList);
// (optional) rpc GetMetric(MetricId) returns (Metric);
```

### Override API (admin/operator-only)

To allow operators to customize metric recording levels without server restarts, we expose an admin API for managing `MetricLevelOverride` objects:

```proto
rpc PutMetricLevelOverride(PutMetricLevelOverrideRequest) returns (MetricLevelOverride);
rpc DeleteMetricLevelOverride(DeleteMetricLevelOverrideRequest) returns (google.protobuf.Empty);
rpc ListMetricLevelOverrides(ListMetricLevelOverridesRequest) returns (MetricLevelOverridesList);
```

### Server-side default registry

A server-side class (for example, `DefaultMetricsRegistry`) will contain the canonical list of `MetricSpec` definitions that are loaded at server startup. Operators can provide their own registry by overriding the class.

Java example (pseudo):

```java
public class DefaultMetricsRegistry {
    public static List<MetricSpec> builtIn() {
        return List.of(
            MetricSpec.builder("workflow-completed-5m")
                .aggregation(COUNT)
                .scope(globalScope())
                .transition(WF_RUN, "RUNNING", "COMPLETED")
                .window(5m)
                .build(),
            // more defaults...
        );
    }
}
```

> **Note:** If we later decide to allow limited user-defined metrics we can implement an admin-only or operator-only API; for now metrics are configured via the server-side registry.


## The Dashboard Experience

### Metrics Overview

On the dashboard front page, users should see time-series line charts for the Workflow and Task metrics. Users must be able to select arbitrary time ranges, the dashboard will be responsible for aggregating adjacent 5-minute windows into larger windows for display.

### Workflow Metrics

The `WfSpec` overview page should show started/completed/error/exception series with selectable windows (5m, 1h, 24h). When `DEBUG` metrics are enabled (see Recording Levels), provide a "view heat map" action to show node-level performance and failure hotspots.



## Protobuf Changes



```proto
enum AggregationType {
  COUNT = 0;
  LATENCY = 1;
}

enum MetricRecordingLevel {
  INFO = 0;  // Collect non-intrusive defaults
  NONE = 1;  // Collect no metrics unless explicitly configured
  DEBUG = 2; // Collect detailed/expensive metrics (e.g. flamegraphs)
}

enum EntityType {
  WF_RUN = 0;
  TASK_RUN = 1;
  USER_TASK_RUN = 2;
  NODE_RUN = 3;
  THREAD_RUN = 4;
}

message StatusTransition {
  EntityType entity = 1;
  string from_status = 2;
  string to_status = 3;
}

message MetricScope {
  oneof type {
    WfSpecId wf_spec = 1;
    TaskDefId task_def = 2;
    NodeReference node = 3;
    bool global = 4; // for cluster-wide metrics
  }
}

message NodeReference {
  WfSpecId wf_spec = 1;
  optional string thread_name = 2;
  optional string node_name = 3;
  optional TaskDefId task_def = 4;
}

message MetricSpec {
  string id = 1;
  google.protobuf.Timestamp created_at = 2;
  AggregationType aggregation_type = 3;
  MetricScope scope = 4;
  StatusTransition transition = 5;
  google.protobuf.Duration window_length = 6;
}
```

### Query API

```proto
message ListMetricsRequest {
  string metric_spec_id = 1;
  google.protobuf.Duration window_length = 2;
  AggregationType aggregation_type = 3;
  google.protobuf.Timestamp start_time = 4;
  google.protobuf.Timestamp end_time = 5;
}

message MetricList {
  repeated MetricWindow windows = 1;
}
```

### Runtime Metric Windows

```proto
message CountAndTiming {
  int32 count = 1;
  int64 min_latency_ms = 2;
  int64 max_latency_ms = 3;
  int64 total_latency_ms = 4;
}

message MetricWindowId {
  EntityType entity = 1;
  string entity_id = 2; // e.g., wf_spec name, task_def name
  google.protobuf.Timestamp window_start = 3;
}

message MetricWindow {
  MetricWindowId id = 1;
  int32 total_started = 2; // for applicable entities
  CountAndTiming completed = 3;
  CountAndTiming halted = 4;
  CountAndTiming error = 5;
  CountAndTiming exception = 6;
  map<string, CountAndTiming> custom = 7; // for exceptions, errors by type, etc.
  // Additional fields for TaskMetric specifics
  CountAndTiming scheduled_to_running = 8;
  CountAndTiming running_to_success = 9;
  int32 timeouts = 10;
}
```

### Override API Messages

```proto
message MetricLevelOverride {
  string id = 1;
  MetricRecordingLevel new_level = 2;
  oneof target {
    WfSpecId wf_spec = 3;
    TaskDefId task_def = 4;
    NodeReference node = 5;
  }
}

message PutMetricLevelOverrideRequest {
  MetricLevelOverride override = 1;
}

message DeleteMetricLevelOverrideRequest {
  string id = 1;
}

message ListMetricLevelOverridesRequest {
  // Optional filters
  optional WfSpecId wf_spec_filter = 1;
  optional TaskDefId task_def_filter = 2;
}

message MetricLevelOverridesList {
  repeated MetricLevelOverride overrides = 1;
}
```

### Metrics Data Flow

The following describes the system flow and technical implementation of workflow metrics:

#### Metrics Data Flow

1. **Server Startup and Metric Definition**:
   - The server loads predefined `MetricSpec` instances from the `DefaultMetricsRegistry` at startup.
   - Each `MetricSpec` defines what to measure (e.g., `AggregationType.COUNT` for workflow completions), the scope (`MetricScope` - could be a specific `WfSpecId`, `TaskDefId`, or global), the status transition to track (`StatusTransition` with `EntityType`, from/to statuses), and the window length.
   - Metric recording levels are resolved hierarchically at runtime: specific overrides (for WfSpec, TaskDef, or Node) take precedence over tenant defaults, which fall back to server defaults.

2. **Event Processing**:
   - As workflow events occur (e.g., a `WfRun` changes status from `RUNNING` to `COMPLETED`), the server checks if the event matches any `MetricSpec`'s criteria.
   - If matched, the server updates the corresponding `MetricWindow` in the Kafka Streams state store. The `MetricWindowId` identifies the window by `EntityType`, `entity_id` (e.g., workflow name), and `window_start` timestamp.
   - Aggregations are performed using `CountAndTiming`: for COUNT metrics, increment the `count`; for LATENCY metrics, calculate the time difference and update `min_latency_ms`, `max_latency_ms`, and `total_latency_ms`.

3. **Window Management**:
   - Windows are aligned to the epoch and have a fixed length (5minutes).
   - Old windows are cleaned up after the retention period (e.g., 30 days).

4. **Querying Metrics**:
   - Clients (e.g., dashboard) send a `ListMetricsRequest` specifying the `metric_spec_id`, desired `window_length`, `aggregation_type`, and time range (`start_time` to `end_time`).
   - The server retrieves relevant `MetricWindow` instances from the state store.
   - A `MetricList` is returned containing the matching `MetricWindow`s, each with aggregated data like `total_started`, `completed` (a `CountAndTiming`), and other status-specific metrics.

5. **Dashboard Visualization**:
   - The dashboard processes the `MetricList` to display time-series charts, aggregating windows as needed for different time ranges.
   - For example, multiple 5-minute windows can be merged into hourly views by summing `CountAndTiming` fields.

This flow ensures metrics are collected efficiently without affecting workflow execution, stored durably, and queried flexibly via gRPC.

#### Technical Implementation

- **Storage Model**: Metrics are stored as `StoredGetable` objects in Kafka Streams state stores. Aggregation occurs per partition first, with global aggregation achieved through Kafka Streams repartitioning to combine data across the cluster.

- **Ongoing Event Tracking**: An internal `OngoingEvent` object tracks start and end timestamps for workflow entities. Metrics are computed and emitted only when the final status is observed, ensuring accuracy.

- **Locally-Aggregated Updates**: In Kafka Streams, each partition processes events independently. For global metrics (cluster-wide), each partition maintains local time windows with aggregated data (e.g., count of events, total latency). These "partial aggregates" are forwarded periodically to a repartition topic. A repartition processor then merges partials into global `WfSpecMetricsModel` objects by summing cumulative totals (e.g., `total_latency_ms` + `count`) in the partitioned state store.

- **Performance & Caching Considerations**:
  - **Caching**: Leverages Kafka Streams' built-in State Store Cache (configurable via `cache.max.bytes.buffering` and `commit.interval.ms`) to hold deserialized state in memory, reducing RocksDB reads and batching writes to minimize changelog topic traffic.
  - **Partial Updates**: Calculated locally per partition in state stores; partials are emitted to repartition topics for global aggregation in the partitioned store. Merging occurs during repartition command processing.
  - **Cleanup**: Old windows are removed after 30 days using state store retention.

The `DefaultMetricsRegistry` is a server-side component that defines the set of `MetricSpec` instances loaded at server startup. **Operators can customize this registry** by:

- **Overriding the registry class**: Provide a custom implementation of `DefaultMetricsRegistry` to modify the built-in metrics list.
- **Configuration-based customization**: Use server configuration to enable/disable specific metrics or adjust recording levels per tenant, workflow, or task.
- **Per-entity overrides**: Configure metric recording levels (INFO, NONE, DEBUG) at the tenant level or for specific WfSpecs/TaskDefs.

This ensures metrics are pre-configured and not dynamically created by clients, maintaining performance and security. Operators cannot create or mutate metrics at runtime via public APIs.


### Server-Side Configuration Examples

Below are examples of how to configure metrics in the `DefaultMetricsRegistry`. Each example shows a `MetricSpec` proto that would be included in the registry's list.

#### Cluster-Wide Workflow Completion Count

Tracks the total number of workflows completed across the entire cluster every 5 minutes.

```proto
MetricSpec {
  id: "global-workflow-completed-5m"
  aggregation_type: COUNT
  scope: { global: true }
  transition: {
    entity: WF_RUN
    from_status: "RUNNING"
    to_status: "COMPLETED"
  }
  window_length: { seconds: 300 }
}
```

#### Cluster-Wide Workflow Failure Count

Tracks the total number of workflows that failed across the entire cluster every 5 minutes.

```proto
MetricSpec {
  id: "global-workflow-failed-5m"
  aggregation_type: COUNT
  scope: { global: true }
  transition: {
    entity: WF_RUN
    from_status: "RUNNING"
    to_status: "ERROR"
  }
  window_length: { seconds: 300 }
}
```

#### Cluster-Wide Task Execution Latency

Tracks the average execution time for all tasks across the cluster every 5 minutes.

```proto
MetricSpec {
  id: "global-task-execution-latency-5m"
  aggregation_type: LATENCY
  scope: { global: true }
  transition: {
    entity: TASK_RUN
    from_status: "TASK_RUNNING"
    to_status: "TASK_SUCCESS"
  }
  window_length: { seconds: 300 }
}
```

#### Workflow-Specific: Order Processing Completion Count

Tracks completions for a specific workflow (e.g., "order-processing") every 5 minutes.

```proto
MetricSpec {
  id: "order-processing-completed-5m"
  aggregation_type: COUNT
  scope: {
    wf_spec: { name: "order-processing" }
  }
  transition: {
    entity: WF_RUN
    from_status: "RUNNING"
    to_status: "COMPLETED"
  }
  window_length: { seconds: 300 }
}
```

#### Task-Specific: Send Email Success Count

Tracks successful executions for a specific task definition (e.g., "send-email") every 5 minutes.

```proto
MetricSpec {
  id: "send-email-success-5m"
  aggregation_type: COUNT
  scope: {
    task_def: { name: "send-email" }
  }
  transition: {
    entity: TASK_RUN
    from_status: "TASK_RUNNING"
    to_status: "TASK_SUCCESS"
  }
  window_length: { seconds: 300 }
}
```


## Metric Exposure

Workflow metrics are exposed **exclusively through the gRPC API**, providing a centralized, authenticated access point for all workflow observability data. This differs from technical metrics (JVM, Kafka Streams, RocksDB) which are exposed via Prometheus for operational monitoring. Workflow metrics require multi-tenancy isolation and transactional guarantees that Prometheus cannot support due to its focus on availability over accuracy and lack of tenant-based access control. As an alternative for event-driven distribution, metrics can be published to Kafka output topics for integration with external systems like data warehouses or alerting platforms.


