# Workflow Metrics

This proposal introduces application-level metrics for LH Server users to track the health of their `WfRun`s, `TaskRun`s, and `UserTaskRun`s. In this proposal, we:

* Introduce a new `Getable` called `MetricReading`.
* Pre-define a set of metric types which are automatically collected for each `TaskDef` and `WfSpec`.
* Collect aggregates for each metric type and `WfSpec` / `TaskDef` in 5-minute windows, which can be stored and retrieved historically.

## Motivation

Users want a simple, easy, and hassle-free way to understand questions such as:

* How many `WfRun`s were started or finished for a given `WfSpec` in a given time period?
* How long does this `WfSpec` take to run, start to finish?
* Is my `WfSpec` experiencing high levels of errors?

The data should be exposed in a `Tenant`-aware manner with proper scoping and ACL's in the GRPC API. It should also be displayed in a user-friendly manner in the dashboard.

### Out of Scope

This proposal does not include the following.

#### Exporting the `MetricReading`

The `MetricReading`s will be retrievable via the GRPC API. We do not propose to provide additional exporters—users can build their own using the GRPC clients.

However, the LH Dashboard will provide a user-friendly interface to explore the metrics.

#### Counted Tags

Counted Tags will be a separate proposal which allows answering questions such as:

* How many `TaskAttempt`s are in the `TASK_SCHEDULED` status (i.e. how many tasks are in the queue) for this `TaskDef` right now?
* How many `WfRun`s are in the `RUNNING` state for this `WfSpec` right now?
* How many `NodeRun`s am I storing because of this `WfSpec`?

These questions require a different architectural addition to be answered, and we will begin work on that in a subsequent proposal.

#### P95 Approximations

I would love to do approximate P95 for these metrics, but it is highly difficult to do that. Our implementation of metrics is _mergeable_; that is, each partition sends a "sketch" that is then forwarded to a downstream processor which merges the sketches together. The [DDSketch Algorithm](https://www.vldb.org/pvldb/vol12/p2195-masson.pdf) would allow us to approximate the P95 values for these metrics in a mergeable manner, but it's a bit too hard for now.

## Key Decisions

This section outlines and justifies certain key decisions in this proposal. The proposal aims for simplicity of the API and ease of use by the customer.

* All metrics are recorded in _mergeable_ windows of 5 minutes each. Windows are persisted for 14 days to allow for historical queries.
* Metric structures are pre-defined.
* Sensible defaults with simple overrides enable control of what metrics are scraped with minimal work.

### Mergeable Windows

* Every metric is aggregated over a **5-minute tumbling window**, with the start of the window aligning to the epoch.
* Every metric reading includes enough information so that you can add together adjacent windows and form larger windows.
  * For example, we never store `latency_avg`: we instead store `total_latency` and `number_observed`. This way, you can add adjacent windows together to get a picture of a 10-minute window (and so on, up to whatever window size you wish to query).
* GRPC Clients such as the LittleHorse Dashboard will be responsible for aggregating individual windows into larger time windows.
* The LittleHorse Dashboard will allow users to view the metrics in whichever time window size they wish.

### Metric Recording Levels

By default, 

```protobuf
enum MetricRecordingLevel {
    // By default, collect non-intrusive metrics for every metadata object
    // in the `Tenant`.
    INFO = 0;

    // Collect no metrics unless explictly configured with a `MetricSpec`.
    NONE = 1;

    // Collect more metrics by default, including computationally expensive
    // ones.
    DEBUG = 2;
}

message Tenant {
    // ...

    // The default level of metrics to record in a given `Tenant`.
    MetricRecordingLevel metrics_level = 4;
}
```

If you want to override the metric level for a specific `WfSpec` or `TaskDef`:

```
// This is a global getable
message MetricLevelOverride {
    MetricRecordingLevel new_level = 1;

    oneof target {
        WfSpecId wf_spec = 2;
        TaskDefId task_def = 3;
        NodeReference node = 4;
        // TODO: user task metrics, etc.
    }
}
```

### Metric Structures

This proposal defines an initial set of metrics. They

```protobuf
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

// CountAndTiming is the default store that LittleHorse keeps for a given metric
// value in a 
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

// This is a Getable that can be fetched and listed from the API, containing a group
// of metrics about a `WfSpec`'s execution in the given time window.
//
// Recorded by default in `INFO` level.
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

// This is a `Getable` that can be fetched and listed from the API, containing a group
// of metrics about a `TaskDef`'s execution in the given time window.
//
// Recorded by default in `INFO` level.
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

    // TODO: Should we add total overall `TaskRun` metrics, or is TaskAttempt
    // sufficient?
}
```

### Flamegraph Metrics

What about drilling in to the actual node executions? The following protobuf enables:

* Determining what percentage of WfRun's go left or right at a given point.
* Allowing heat maps of execution time in a `WfSpec`.
* Determining where the failures happen.

This is going to be expensive so it is only enabled in `DEBUG`.

```
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

## The Dashboard Experience

### Metrics Overview

On the front page of the dashboard, we should see time series line graphs for the Workflow Metrics and the Task Metrics.

### Workflow Metrics

The `WfSpec` overview page should have a chart (probably a time-series line graph) showing `WfRun`s in various configurations. Users should be able to see started, completed, error, exception, etc. Users should also be able to choose the time window they want to view. (Note: the aggregation is done transparently by the dashboard.)

There should be a "view heat map" button if `DEBUG` metrics are enabled _or_ there is a `MetricConfigOverride` for the `WfSpec`. There should be a few different screens available to the user:

1. How many `NodeRun`s for each `Node`?
2. What percentage of `NodeRun`s for a given `Node` succeeded?
3. How long did the `NodeRun`s for each `Node` take?

## Implementation Details

### Locally-Aggregated Updates

Every partition calculates a local mergeable time window and forwards it to an aggregator every 5 minutes.

### TODO: Performance Characterization

TODO explain the following:

* How to cache the partial updates to prevent deserialization work.
* Why Kafka Streams State Store Cache will limit the effects on RocksDB and changelog topic.
* How to calculate partial updates on each partition and forward them to an aggregator.
* How to clean up old metric windows after 30 days (or some other retention time)
