# Workflow Metrics

**Authors:** Eduwer Camacaro, Christian Caicedo


## Motivation

LittleHorse Server orchestrates workflows that represent real business processes and enables engineers to implement durable technical solutions. However, once workflows are running, there is currently no built-in way to observe how they behave over time.

Teams lack visibility into basic questions such as execution rates, latency, failure patterns, and workflow health. As a result, performance issues, regressions, and business-impacting delays are often detected late or inferred indirectly through logs and external systems.

This proposal introduces **workflow metrics** as a first-class feature in LittleHorse, enabling both technical and business users to understand workflow behavior using structured, queryable data.


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

We propose introducing a **Workflow Metrics API** that allows users to explicitly define, collect, and query metrics related to workflow execution.

Metrics are:

* Defined via `MetricSpec`
* Aggregated over time windows
* Computed internally by the LittleHorse Server
* Stored and queried using native APIs

No external systems or dependencies are required.


## Scope

### Supported Metric Types

This proposal introduces **three metric types**:

1. **Rate metrics**
   Measure how often an event occurs in a time window
   *Example: number of `TaskRun`s in the last 5 minutes*

2. **Latency metrics**
   Measure time between two status transitions
   *Example: time from `TASK_SCHEDULED` → `TASK_SUCCESS`*

3. **Ratio metrics**
   Measure relationships between counts
   *Example: percentage of workflows that spawned a child thread*



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


### Ratio Metrics

Compute ratios between event counts.

**Example**

```
(child_thread_count / wf_run_count) * 100
```


## Public API

### Metric Metadata APIs

```proto
rpc PutMetricSpec(PutMetricSpecRequest) returns (MetricSpec);
rpc ListMetricSpecs(ListMetricSpecRequest) returns (MetricSpecList);
```

### Metric Query API

```proto
rpc ListMetrics(ListMetricsRequest) returns (MetricList);
```


## Protobuf Changes

### MetricSpec Definition

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
  AVG = 1;
  RATIO = 2;
  LATENCY = 3;
}
```


### Aggregator Configuration

```proto
message Aggregator {
  google.protobuf.Duration window_length = 1;

  oneof type {
    Count count = 2;
    Ratio ratio = 3;
    Latency latency = 4;
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
```


## Global Metrics

LittleHorse requires tracking global metrics for all workflows and all task definitions to provide cluster-wide observability. The operator could be the  responsible for defining these global metrics that will automatically apply to every workflow and every node in the system.

### Global Metrics Scope

Operators can define metrics at different scopes:

* **Cluster-wide workflow metrics**: Track behavior across all workflows (using `any_workflow: true`)
* **Cluster-wide task metrics**: Track behavior across all task nodes (using `NodeReference` with `task_def_id`)



### Example Requests

#### Global Workflow Completion Count

Track the number of completed workflows across the entire cluster:

```proto
PutMetricSpecRequest {
  aggregation_type: COUNT
  any_workflow: true
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

```proto
PutMetricSpecRequest {
  aggregation_type: COUNT
  any_workflow: true
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

Track execution latency for all task nodes across all workflows:

```proto
PutMetricSpecRequest {
  aggregation_type: LATENCY
  node: {
    // node_type, node_position, thread_spec not set = all task nodes
    task_def_id: null  // applies to all TaskDefs
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

#### Global Task Queue Latency

Track how long tasks wait in queues across all task definitions:

```proto
PutMetricSpecRequest {
  aggregation_type: LATENCY
  node: {
    task_def_id: null  // applies to all TaskDefs
  }
  status_range: {
    task_run: {
      starts: TASK_SCHEDULED
      ends: TASK_RUNNING
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



## Implementation Overview

### Storage Model

* Metrics are stored as `Storeable` objects
* Aggregation happens per partition
* Global aggregation uses Kafka Streams repartitioning

### Ongoing Event Tracking

* A new internal `OngoingEvent` tracks start and end timestamps
* Metrics are emitted when end status is observed

