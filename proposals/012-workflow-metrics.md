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

### Server-side default registry

A server-side class (for example, `DefaultMetricsRegistry`) will contain the canonical list of `MetricSpec` definitions that are loaded at server startup. Operators can provide their own registry by overriding the class.

Java example (pseudo):

```java
public class DefaultMetricsRegistry {
    public static List<MetricSpec> builtIn() {
        return List.of(
            MetricSpec.builder("workflow-completed-5m").aggregation(COUNT).window(5m).build(),
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

### Flamegraph / Debug Metrics

Flamegraph-style and node-heatmap metrics are expensive and should only be enabled at `DEBUG` level. They allow:

* Visibility into which `Node`s dominate runtime in a `WfSpec`.
* Heat maps and percentage distributions across branches.
* Drill-downs to find where failures and long latencies occur.

These views are optional and only rendered when the server is configured to collect these more detailed metrics.


## Protobuf Changes

### MetricSpec Definition

> **Note:** Metric spec structure exists to describe the server-side registry entries. There is intentionally **no** public `PutMetricSpec` RPC – metric specs are configured via the server-side registry (`DefaultMetricsRegistry`).

```proto
message PutMetricSpecRequest {
  AggregationType aggregation_type = 1;

  oneof reference {
    NodeReference node = 2;
    WfSpecId wf_spec_id = 3;
    ThreadSpecReference thread_spec = 4;
    boolean any_workflow = 5;
  }

  StatusRange status_range = 6;

  google.protobuf.Duration window_length = 7;
}

message StatusRange {
  oneof type {
    LHStatusRange lh_status = 1;
    TaskRunStatusRange task_run = 2;
    UserTaskRunStatusRange user_task_run = 3;
  }
}

message LHStatusRange {
  LHStatus starts = 1;
  LHStatus ends = 2;
}

message TaskRunStatusRange {
  TaskStatus starts = 1;
  TaskStatus ends = 2;
}

message UserTaskRunStatusRange {
  UserTaskRunStatus starts = 1;
  UserTaskRunStatus ends = 2;
}

message NodeReference {
  optional ThreadSpecReference thread_spec = 1;
  optional string node_type = 2;
  optional int32 node_position = 3;
  optional TaskDefId task_def_id = 4;
}

message ThreadSpecReference {
  WfSpecId wf_spec_id = 1;
  optional int32 thread_number = 2;
}
```

```proto
message MetricSpec {
  MetricSpecId id = 1;
  google.protobuf.Timestamp created_at = 2;
  repeated Aggregator aggregators = 3;
}
```


### Aggregation Types

```proto
enum AggregationType {
  COUNT = 0;
  LATENCY = 1;
}
```


### Aggregator Configuration

```proto
message Aggregator {
  google.protobuf.Duration window_length = 1;

  oneof type {
    Count count = 2;
    Latency latency = 3;
  }
}
```


### Metric Values

```proto
message Metric {
  MetricId id = 1;

  oneof value {
    int64 count = 2;
    int64 latency_avg = 3;
  }

  google.protobuf.Timestamp created_at = 4;
  map<int32, double> value_per_partition = 5;
}
```


### Metric Query

```proto
message ListMetricsRequest {
  MetricSpecId metric_spec_id = 1;
  google.protobuf.Duration window_length = 2;
  AggregationType aggregation_type = 3;
}
```

```proto
message MetricList {
  repeated Metric results = 1;
}

// The following runtime messages capture the canonical representation
// of the metric windows we persist and expose. These are read-only
// from the client's point of view; metrics are produced by the server.

// CountAndTiming is the canonical structure for count + latency aggregates.
message CountAndTiming {
  // The total count of entities that terminated in this window.
  int32 count = 1;

  // The min latency observed for entities that terminated in this window.
  int64 min_latency_ms = 2;

  // The max latency observed for entities that terminated in this window.
  int64 max_latency_ms = 3;

  // The total latency observed for entities that terminated in this window.
  int64 total_latency_ms = 4;
}

message WorkflowMetricId {
  // The WfSpecId that the metric tracks. If null then this is the metric for the
  // `Tenant` in aggregate.
  optional WfSpecId wf_spec = 1;

  // The time at which the window started.
  google.protobuf.Timestamp window_start = 2;
}

message TaskMetricId {
  // The TaskDefId that the metric tracks. If null then this is the metric for
  // the `Tenant` in aggregate.
  optional TaskDefId task_def = 1;

  // The time at which this window started.
  google.protobuf.Timestamp window_start = 2;
}

message WorkflowMetric {
  // The ID of this metric window
  WorkflowMetricId id = 1;

  // Number of `WfRun`s started for this WfSpec in this time window.
  int32 total_started = 2;

  // Metrics about `WfRun`s that moved to `COMPLETED` in this time window.
  CountAndTiming completed = 3;

  // Metrics about `WfRun`s that moved to `HALTED` in this time window.
  CountAndTiming halted = 4;

  // Metrics for the `WfRun`s transitioned to the `EXCEPTION` state in this
  // time window. Each key is the exception name
  map<string, CountAndTiming> exception = 5;

  // Metrics for `WfRun`s transitioned to the `ERROR` state in this
  // time window. Each key is the error type.
  map<LHErrorType, CountAndTiming> errors = 8;
}

message TaskMetric {
  // The ID of this metric window.
  TaskMetricId id = 1;

  // Number of `TaskRun`s started in this `TaskDef` for this window.
  int32 total_task_runs_started = 1;

  // Number of `TaskAttempt`s started in this `TaskDef` for this window.
  int32 total_task_attempts_started = 2;

  // Metrics about how long each `TaskAttempt` sat in the queue.
  CountAndTiming scheduled_to_running = 3;

  // Metrics about how long each `TaskAttempt` took to go from TASK_RUNNING
  // to TASK_SUCCESS
  CountAndTiming running_to_task_success = 4;

  // Metrics about how long each `TaskAttempt` took to go from TASK_RUNNING
  // to TASK_ERROR
  CountAndTiming running_to_task_error = 5;

  // Metrics about how long each `TaskAttempt` took to go from TASK_RUNNING
  // to TASK_EXCEPTION
  CountAndTiming running_to_task_exception = 6;

  // Number of `TaskAttempts` that timed out in this time window.
  int32 task_attempt_timeouts = 7;

  // TaskAttempts that moved from `PENDING` to `TASK_SCHEDULED` in this
  // time window due to exponential backoff retries.
  CountAndTiming pending_to_scheduled = 8;
}

message NodeMetricId {
  WfSpecId wf_spec = 1;
  string thread_spec_name = 2;
  string node_name = 3;
  google.protobuf.Timestamp window_start = 4;
}

message NodeMetric {
  NodeMetricId id = 1;

  // Number of NodeRun's started in this window.
  int32 started = 2;

  // NodeRun's moved to `COMPLETED` in this window.
  CountAndTiming completed = 3;

  CountAndTiming error = 4;

  CountAndTiming exception = 5;
}
```


## Global Metrics

LittleHorse requires tracking global metrics for all workflows and task definitions to provide cluster-wide observability. **These global metrics are configured by operators at startup** through the `DefaultMetricsRegistry` (i.e., server-side code or configuration), and will automatically apply to every workflow and every node in the system.

### Global Metrics Scope

Operators can define metrics at different scopes:

* **Cluster-wide workflow metrics**: Track behavior across all workflows (using `any_workflow: true`)
* **Cluster-wide task metrics**: Track behavior across all task nodes (using `NodeReference` with `task_def_id`)



### Server-side default config examples

#### Global Workflow Completion Count

Track the number of completed workflows across the entire cluster:

// Server-side default config example (e.g. entry in `DefaultMetricsRegistry`)
```proto
PutMetricSpecRequest {
  aggregation_type: COUNT
  reference:{
    any_workflow: true
  }
  status_range: {
    lh_status: {
      starts: RUNNING
      ends: COMPLETED
    }
  }
  window_length: { seconds: 300 }  // 5 minutes
}
```

#### Global Workflow Failure Count

Track the number of failed workflows across the entire cluster:

// Server-side default config example (e.g. entry in `DefaultMetricsRegistry`)
```proto
PutMetricSpecRequest {
  aggregation_type: COUNT
  reference:{
    any_workflow: true
  }
  status_range: {
    lh_status: {
      starts: RUNNING
      ends: ERROR
    }
  }
  window_length: { seconds: 300 }
}
```

#### Global Task Execution Latency

Track execution latency for all UserTaskNodeRun workflows:

// Server-side default config example (e.g. entry in `DefaultMetricsRegistry`)
```proto
PutMetricSpecRequest {
  aggregation_type: LATENCY
  reference:{
      node: {
          node_type="UserTaskNodeRun"
    }
  }
  status_range: {
    task_run: {
      starts: TASK_RUNNING
      ends: TASK_SUCCESS
    }
  }
  window_length: { seconds: 300 }
}
```

#### Specific TaskDef Metrics

Track metrics for a specific task definition:

```proto
PutMetricSpecRequest {
  aggregation_type: COUNT
  node: {
    task_def_id: {
      name: "send-email"
    }
  }
  status_range: {
    task_run: {
      starts: TASK_RUNNING
      ends: TASK_SUCCESS
    }
  }
  window_length: { seconds: 300 }
}
```


## Metric Exposure

Workflow metrics wil be exposed exclusively through the **gRPC API** , providing a centralized access point for all workflow observability data.

### Separation from Technical Metrics

LittleHorse currently exposes **technical metrics** (JVM, Kafka Streams, RocksDB) via Prometheus on port 1822. These are operational metrics for cluster health monitoring and are used by system administrators.

Workflow metrics are fundamentally different as they represent application-level data and require:

1. **Multi-tenancy isolation**: Metrics must be scoped by tenant with proper authentication and authorization
2. **Transactional guarantees**: Computed with Kafka Streams and stored in RocksDB

Prometheus cannot meet these requirements as it prioritizes availability over accuracy and does not support tenant-based access control.

### Alternative: Output Topics

Customers needing event-driven metric distribution can use **Kafka output topics**, which provide:

* Integration with external systems (data warehouses, alerting, dashboards)
* Per-tenant organization

This centralized gRPC approach ensures consistent access patterns for all workflow data (WfRuns, TaskRuns, metrics) while offering flexibility through Kafka output topics when needed.


## Implementation Overview

### Storage Model

* Metrics are stored as `Storeable` objects
* Aggregation happens per partition
* Global aggregation uses Kafka Streams repartitioning

### Ongoing Event Tracking

* A new internal `OngoingEvent` tracks start and end timestamps
* Metrics are emitted when end status is observed

### Locally-aggregated updates

Each partition maintains a local, mergeable window and forwards its partial aggregates to an aggregator every 5 minutes. The aggregator merges windows from different partitions into a global window which is then persisted. Windows store totals (e.g. `total_latency_ms` and `count`) to allow adding adjacent windows to form larger aggregates in a lossless way.

### Performance & caching considerations (TODO)

We will evaluate and document:

* Caching strategies to avoid repeated (de)serialization of partial updates
* How Kafka Streams State Store caching reduces read amplification on RocksDB and lowers changelog traffic
* Efficient calculation of partial updates per partition and memory/storage implications
* Cleanup strategies for old windows (default retention: 14 days, configurable) and efficient compaction/expiry

These items will be the focus of a follow-up performance characterization effort and engineering spike. 

