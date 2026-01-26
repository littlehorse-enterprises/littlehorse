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
* An RPC is exposed to configure the metric recording level, which can be applied globally to all objects or specifically to individual workflows (e.g., a workflow).
* Metrics are aggregated over mergeable time windows (5 min window).
* All metrics are tenant-scoped for multi-tenancy isolation.


### Default Metrics Available

The following list describes the default metrics collected for each entity type. Each metric includes a count, and latency where applicable, identified by a key used in `MetricWindow`.

- **WfRun**:
  - "STARTED": Count of workflows started
  - "RUNNING_TO_COMPLETED": Count of workflows completed, Latency from RUNNING to COMPLETED
  - "RUNNING_TO_HALTED": Count of workflows halted, Latency from RUNNING to HALTED
  - exception name: Count of workflows with exceptions, Latency from RUNNING to EXCEPTION
  - LHErrorType: Count of workflows with errors, Latency from RUNNING to ERROR

- **TaskRun**:
  - "TASKRUN_STARTED": Count of TaskRuns started
  - "TASKATTEMPT_STARTED": Count of TaskAttempts started
  - "TASK_SCHEDULED_TO_TASK_RUNNING": Latency from TASK_SCHEDULED to TASK_RUNNING
  - "TASK_RUNNING_TO_TASK_SUCCESS": Latency from TASK_RUNNING to TASK_SUCCESS
  - "TASK_SCHEDULED_TO_TASK_SUCCESS": Latency from TASK_SCHEDULED to TASK_SUCCESS
  - "TASK_RUNNING_TO_TASK_FAILED": Latency from TASK_RUNNING to TASK_FAILED
  - "TASK_RUNNING_TO_TASK_EXCEPTION": Latency from TASK_RUNNING to TASK_EXCEPTION
  - "TIMEOUT": Count of TaskAttempts that timed out

- **UserTaskRun**:
  - "ASSIGNED": Count of user tasks assigned
  - "DONE": Count of user tasks completed
  - "CANCELLED": Count of user tasks cancelled
  - "ASSIGNED_TO_DONE": Latency from ASSIGNED to DONE

- **NodeRun** (when WF is in DEBUG, metrics collected per node):
  - "STARTED": Count of NodeRuns started for the node
  - "RUNNING_TO_COMPLETED": Count of NodeRuns completed for the node, Latency from RUNNING to COMPLETED
  - "RUNNING_TO_ERROR": Count of NodeRuns with errors for the node, Latency from RUNNING to ERROR
  - "RUNNING_TO_EXCEPTION": Count of NodeRuns with exceptions for the node, Latency from RUNNING to EXCEPTION


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

1. **Check for specific overrides**: Look up `MetricLevelOverride` objects for the specific `WfSpec` involved.
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
    int32 node_position = 3;  // Position within the ThreadSpec for ordering
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
  map<string, CountAndTiming> metrics = 2;
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

#### Example Query for WfRun Metrics

**Request:**

```json
{
  "id": {
    "workflow": {
      "wf_spec": {
        "name": "my-workflow",
        "version": 1
      }
    },
    "window_start": "2023-10-01T10:00:00Z"
  },
  "end_time": "2023-10-01T10:05:00Z"
}
```

**Response:**

```json
{
  "windows": [
    {
      "id": {
        "workflow": {
          "wf_spec": {
            "name": "my-workflow",
            "version": 1
          }
        },
        "window_start": "2023-10-01T10:00:00Z"
      },
      "metrics": {
        "STARTED": {
          "count": 150,
          "min_latency_ms": 0,
          "max_latency_ms": 0,
          "total_latency_ms": 0
        },
        "RUNNING_TO_COMPLETED": {
          "count": 140,
          "min_latency_ms": 5000,
          "max_latency_ms": 30000,
          "total_latency_ms": 2100000
        }
      }
    },
    {
      "id": {
        "workflow": {
          "wf_spec": {
            "name": "my-workflow",
            "version": 1
          }
        },
        "window_start": "2023-10-01T10:05:00Z"
      },
      "metrics": {
        "STARTED": {
          "count": 150,
          "min_latency_ms": 0,
          "max_latency_ms": 0,
          "total_latency_ms": 0
        },
        "RUNNING_TO_COMPLETED": {
          "count": 120,
          "min_latency_ms": 5000,
          "max_latency_ms": 30000,
          "total_latency_ms": 2100000
        }
      }
    },

  ]
}
```

### Override API Proto

```proto
message MetricLevelOverride {
  string id = 1;
  MetricRecordingLevel new_level = 2;
  WorkflowMetricId workflow = 3;
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
}

message MetricLevelOverridesList {
  repeated MetricLevelOverride overrides = 1;
}
```

### Metrics Data Flow

The following describes the system flow and technical implementation of workflow metrics:

#### Metrics Data Flow

1. **Server Startup and Metric Definition**:
   - The server initializes with a set of metric definitions listed above.
   - Each metric definition includes the entity type, key, aggregation type (count and latency), and applicable status transitions.
   - Metric recording levels are resolved hierarchically at runtime: specific overrides for `WfSpec` take precedence over tenant defaults, which fall back to server defaults. NodeRun metrics are only collected if the associated `WfSpec` is set to `DEBUG` level.

2. **Event Processing**:
   - As workflow events occur (e.g., a `WfRun` changes status from `RUNNING` to `COMPLETED`), the server checks if the event matches any predefined metric criteria and if the recording level allows it.
   - For NodeRun events, additionally check if the parent `WfSpec` has `DEBUG` level enabled.
   - If matched, the server updates the corresponding `MetricWindow` in the Kafka Streams state store. The `MetricWindowId` identifies the window by `EntityType`, `entity_id` (e.g., workflow name), and `window_start` timestamp.
   - Aggregations are performed using `CountAndTiming`: for COUNT metrics, increment the `count`; for LATENCY metrics, calculate the time difference and update `min_latency_ms`, `max_latency_ms`, and `total_latency_ms`.

3. **Window Management**:
   - Windows are aligned to the epoch and have a fixed length (5 minutes).
   - Old windows are cleaned up after the retention period (configurable, default 14 days).

4. **Querying Metrics**:
   - Clients (e.g., dashboard) send a `ListMetricsRequest` with a `MetricWindowId` specifying the entity (workflow, task, or node), start time, and optional end time.
   - The server retrieves relevant `MetricWindow` instances from the state store.
   - A `MetricList` is returned containing the matching `MetricWindow`s, each with aggregated data in the `metrics` map.

5. **Dashboard Visualization**:
   - The dashboard processes the `MetricList` to display time-series charts, aggregating windows as needed for different time ranges.
   - For example, multiple 5-minute windows can be merged into hourly views by summing `CountAndTiming` fields.
   - When `DEBUG` is enabled for a workflow, node-level heatmaps are available for detailed performance analysis.
