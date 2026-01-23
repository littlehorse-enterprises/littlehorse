# Workflow Metrics

**Authors:** Eduwer Camacaro, Christian Caicedo


## Motivation

LittleHorse Server orchestrates workflows that represent real business processes and enables engineers to implement durable technical solutions. However, once workflows are running, there is currently no built-in way to observe how they behave over time.

Teams lack visibility into basic questions such as execution rates, latency, failure patterns, and workflow health. As a result, performance issues, regressions, and business-impacting delays are often detected late or inferred indirectly through logs and external systems.

This proposal introduces **workflow metrics** as a first-class feature in LittleHorse, enabling both technical and business users to understand workflow behavior using structured, queryable data.

### Out of scope

* **Counted Tags** — A separate proposal will handle count-at-a-single-instant queries (e.g., how many NodeRuns are currently in TASK_SCHEDULED). These require a different architectural treatment.
* **Exporting Windows** — The Metric windows will be retrievable via the gRPC API. This proposal does not include building additional exporters (e.g., Datadog).


## Current Problem

### Lack of Observability

Today, LittleHorse provides no native mechanism to answer questions like:

* How many completed workflows do we have in a period?
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

We propose introducing **workflow metrics** with a predefined list of metrics to collect. Clients can query collected metrics via read-only APIs (gRPC), but cannot create or mutate metric definitions at runtime.

Key points:

* Metrics are collected from a fixed, server-defined list.
* Retention period and metric recording level are configured at the tenant level, with sensible defaults if not set (`INFO` level, 2 weeks retention).
* An RPC is exposed to configure the metric recording level, which can be applied globally to all objects or specifically to individual objects (e.g., a workflow, task, or node).
* Metrics are aggregated over mergeable time windows (5 min window).
* All metrics are tenant-scoped for multi-tenancy isolation.


### Default Metrics Available

The following table lists the default metrics collected for each entity type. The "Key" column indicates the string key used in a `MetricWindow`. This key is how each metric is identified and queried in the metric window messages. The meaning and structure of a metric window will be described later in this proposal.

| Metric | Entity |  Key |
|--------|--------|------------------------|
| Number of workflows started | WfRun | "STARTED" |
| Number of workflows completed | WfRun | "COMPLETED" |
| Number of workflows halted | WfRun | "HALTED" |
| Number of workflows with exceptions | WfRun | exception name |
| Number of workflows with errors | WfRun | LHErrorType |
| Latency for completed workflows | WfRun | "STARTING_TO_COMPLETED" |
| Latency for halted workflows | WfRun | "HALTED" |
| Latency for exceptions | WfRun | exception name |
| Latency for errors | WfRun | LHErrorType |
| Number of TaskRuns started | TaskRun | "TASKRUN_STARTED" |
| Number of TaskAttempts started | TaskRun | "TASKATTEMPT_STARTED" |
| Latency from TASK_SCHEDULED to TASK_RUNNING | TaskRun | "SCHEDULED_TO_RUNNING" |
| Latency from TASK_RUNNING to TASK_SUCCESS | TaskRun | "RUNNING_TO_SUCCESS" |
| Latency from TASK_RUNNING to TASK_ERROR | TaskRun | "RUNNING_TO_ERROR" |
| Latency from TASK_RUNNING to TASK_EXCEPTION | TaskRun | "RUNNING_TO_EXCEPTION" |
| Number of TaskAttempts that timed out | TaskRun | "TIMEOUT" |
| Number of user tasks assigned | UserTaskRun | "ASSIGNED" |
| Number of user tasks completed | UserTaskRun | "COMPLETED" |
| Number of user tasks cancelled | UserTaskRun | "CANCELLED" |
| Latency from assignment to completion | UserTaskRun | "ASSIGN_TO_COMPLETE" |
| Per-user breakdowns and assignment latency (DEBUG) | UserTaskRun | user id |
| Number of NodeRuns started | NodeRun | "STARTED" |
| Number of NodeRuns completed | NodeRun | "COMPLETED" |
| Number of NodeRuns with errors | NodeRun | "ERROR" |
| Number of NodeRuns with exceptions | NodeRun | "EXCEPTION" |
| Latency for completed NodeRuns | NodeRun | "COMPLETED" |
| Latency for errors | NodeRun | "ERROR" |
| Latency for exceptions | NodeRun | "EXCEPTION" |


## Scope

### Supported Metric Types

This proposal introduces **two metric types**:

1. **Count metrics**
 Track the  number of times an event occurs in a period (window).
  
    *Example:  number of completed WorkflowRuns in 5m*

2. **Latency metrics**
   Measure time between two status transitions in a period (window).

   *Example: how long it took for a task to go from `TASK_SCHEDULED` → `TASK_SUCCESS` in 5m*



### Supported Entities


Metrics can be collected for:

* `WfRun`
* `NodeRun`
* `TaskRun`
* `UserTaskRun`

>For this first iteration, we focus on the most relevant runtime entities. Metrics for the others can be considered in the future if needed.



## Design Principles



### No External Dependencies

All metric collection and aggregation happens inside LittleHorse.

- Aggregation is performed per partition (process-level).
- Repartitioning is used only for global aggregation.

### Minimal Performance Impact

- Metrics are aggregated using time windows.
- Computation happens per partition first.
- Only finalized events produce metric updates.
- Window length: **5 minutes**.


### Mergeable windows & retention

* Default windows are **5 minute windows** aligned to the epoch.
* Each window is persisted for **14 days** (configurable at tenant level) to allow for historical queries.
* Metric windows are **mergeable**  we store totals, so windows can be added together to form larger window.

### Metrics Configuration and Recording Levels

By default, metrics are collected at a conservative level to avoid performance regressions. We recommend the following levels:

```proto
enum MetricRecordingLevel {
  INFO = 0;  // Collect non-intrusive defaults
  NONE = 1;  // Collect no metrics
  DEBUG = 2; // Collect detailed/expensive metrics
}

message MetricsConfig {
  MetricRecordingLevel level = 1;
  int32 retention_days = 2;
}

message Tenant {
  // ... existing fields ...

  // Configuration metrics associated with this Tenant. If not set, defaults are used.
  optional MetricsConfig metrics_config = 4;
}
```

### Metric Recording Level Resolution Logic

Metric recording levels are resolved hierarchically for each metric collection event:

1. **Check for specific overrides**: Look up `MetricLevelOverride` objects for the specific `WfSpec`, `TaskDef`, or `Node` involved.
2. **Fall back to tenant default**: If no specific override exists, use the `Tenant.metrics_level` field.
3. **Fall back to server default**: If no tenant default is set, use the server-wide default ( `INFO`).

This allows granular control: e.g., enable `DEBUG` metrics for a problematic workflow while keeping others at `INFO`.


## Metric Model

### Count Metrics

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
* `NodeRun`



#### Example: TaskRun Latency

| From Status      | To Status      | Meaning            |
| - | -------- | ------------------ |
| `TASK_SCHEDULED` | `TASK_RUNNING` | Queue latency      |
| `TASK_RUNNING`   | `TASK_SUCCESS` | Execution latency  |
| `TASK_SCHEDULED` | `TASK_SUCCESS` | Total task latency |


## Metrics configuration


### Query API (read-only)

The following RPCs remain public so that clients can read the collected metrics and historical windows.

```proto
rpc ListMetrics(ListMetricsRequest) returns (MetricList);
```

### Override API 

To allow clients to override metric recording levels without server restarts, we expose an admin API for managing `MetricLevelOverride` objects:

```proto
rpc PutMetricLevelOverride(PutMetricLevelOverrideRequest) returns (MetricLevelOverride);
rpc DeleteMetricLevelOverride(DeleteMetricLevelOverrideRequest) returns (google.protobuf.Empty);
rpc ListMetricLevelOverrides(ListMetricLevelOverridesRequest) returns (MetricLevelOverridesList);
```



## The Dashboard Experience

### Metrics Overview

On the dashboard front page, users should see time-series line charts for the Workflow and Task metrics. Users must be able to select arbitrary time ranges, the dashboard will be responsible for aggregating adjacent 5-minute windows into larger windows for display.

### Workflow Metrics

The `WfSpec` overview page should show started/completed/error/exception series with selectable windows (5m, 1h, 24h). When `DEBUG` metrics are enabled (see Recording Levels), provide a "view heat map" action to show node-level performance and failure hotspots.



## Protobuf Changes


### Runtime Metric Windows

```proto

message WorkflowMetricId {
    optional WfSpecId wf_spec = 1; // If null, tenant-level aggregate
}

message TaskMetricId {
    optional TaskDefId task_def = 1; // If null, tenant-level aggregate
}

message NodeMetricId {
    WfSpecId wf_spec = 1;
    string node_name = 2;
}

message MetricWindowId {
  oneof id {
    WorkflowMetricId workflow = 1;
    TaskMetricId task = 2;
    NodeMetricId node = 3;
  }
  google.protobuf.Timestamp window_start = 4;
}


message CountAndTiming {
  int32 count = 1;
  int64 min_latency_ms = 2;
  int64 max_latency_ms = 3;
  int64 total_latency_ms = 4;
}


message MetricWindow {
  MetricWindowId id = 1;
  map<string, CountAndTiming> task_status_metrics = 2;
}
```


### Query API

```proto

message ListMetricsRequest {
  / Metric window id contains object and start time
  MetricWindowId id = 1;
  // Optional: if not set, server uses current time
  optional google.protobuf.Timestamp end_time = 5;
}

message MetricList {
  repeated MetricWindow windows = 1;
}
```

### Override API Proto

```proto
message MetricLevelOverride {
  string id = 1;
  MetricRecordingLevel new_level = 2;
  oneof target {
    WorkflowMetricId workflow = 1;
    TaskMetricId task = 2;
    NodeMetricId node = 3;
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

   wf events es otra cosa, si hace match

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
