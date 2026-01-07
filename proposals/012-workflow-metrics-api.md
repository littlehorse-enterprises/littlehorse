# Workflow Metrics API

Author: Eduwer Camacaro / Christian Caicedo

## Motivation

LittleHorse Server excels at managing workflows that are meaningful for the business while allowing technical people to write technical solutions that can endure in time. However, LittleHorse Server is missing an important key for these workflows: **observability**.

Workflow metrics will be a tool that enables both business and technical people to observe the current state of LittleHorse workflows and make informed decisions in their context.

### Use Cases

#### Developer/Technical Use Cases

* **Performance Monitoring**: Deploy a new `WfSpec` into production or staging environments and measure latencies related to task workers or the entire `WfRun`, enabling teams to focus on tasks that introduce unexpected latencies to the entire workflow.
* **Health Monitoring**: Gather information related to existing workflows and monitor the health of every new workflow by observing the numbers related to `WfRun`s.
* **Debug Mode Philosophy**: Enable familiar debugging patterns for workflow developers, similar to traditional application debugging.
* **Troubleshooting**: Aid in debugging and troubleshooting by providing detailed metrics about workflow execution.

#### Business Use Cases

* **Strategic Insights**: Provide strategic information that helps business people make better decisions regarding business processes.
* **Customer Experience**: For example, in a workflow representing a user's complete shopping experience, knowing how long the whole process took from start to finish helps identify slow parts and optimize them, enhancing the overall user experience.
* **Process Optimization**: Identify tasks or processes that slow down the entire workflow, enabling continuous improvement.

### Design Goals

* **No Performance Impact**: There should be no significant performance hit when computing metrics.
* **No External Dependencies**: No additional dependencies required for the LittleHorse Server.
* **Simple Query API**: Users can access an API that queries metrics related to their current workflows.
* **Flexible Granularity**: Support metrics at various levels (WfSpec, ThreadSpec, Node, Task).

## Metric Types

This proposal defines three types of metrics:

### 1. Rate Metrics

Measure how many observed events occur in a specific time window.

**Example**: How many `TaskRun`s executed in the last two minutes.

### 2. Latency Metrics

Measure how long a specific event takes to finish.

**Example**: `TaskRun` latency measured from `start_time` to `completed_time`.

### 3. Ratio Metrics

Measure the relation between `WfRun` count and specific action counts.

**Example**: Thread ratio = `(child_thread_1_count / wf_run_count) * 100`

Can compare `WfRun` counts by status.

## Latency Metrics Detail

Latency metrics measure the delay between two events inside a `WfRun`. A latency metric can be computed only if the object being measured has a status associated with it.

Currently, these are the objects that contain a status field in LittleHorse Server:

### TaskRun Statuses

* `TASK_SCHEDULED`
* `TASK_RUNNING`
* `TASK_SUCCESS`
* `TASK_FAILED`
* `TASK_TIMEOUT`
* `TASK_OUTPUT_SERIALIZING_ERROR`
* `TASK_INPUT_VAR_SUB_ERROR`
* `TASK_EXCEPTION`
* `TASK_PENDING`

### UserTaskRun Statuses

* `UNASSIGNED`
* `ASSIGNED`
* `DONE`
* `CANCELLED`

### WfRun, ThreadRun, NodeRun Statuses

* `STARTING`
* `RUNNING`
* `COMPLETED`
* `HALTING`
* `HALTED`
* `ERROR`
* `EXCEPTION`

### Example: TaskRun Latency Metrics

For a `TaskRun`, we can measure latency between the following status transitions:

| From | To | Description |
|------|-----|-------------|
| `TASK_SCHEDULED` | `TASK_RUNNING` | Time interval between when a `TaskRun` was added to the queue and when a worker polled it. |
| `TASK_SCHEDULED` | `TASK_SUCCESS` | Time interval between when a task was added to the queue and when a worker executed and sent a success response back to the server. |
| `TASK_SCHEDULED` | `TASK_FAILED` | Time interval between when a task was added to the queue and when a worker executed and sent a failure response back to the server. |
| `TASK_RUNNING` | `TASK_SUCCESS` | Time interval between when a task was polled by a worker and when the broker sent a success response back to the server. |

### Storage Requirements

At this time, the LittleHorse Server doesn't store the information needed to measure latencies for these entities. This ADR proposes storing a `Storeable` object on a per-partition basis that tracks ongoing events with both the beginning and ending timestamps. This approach increases the number of writes for each command that alters ongoing events.

Based on the `MetricSpec`, the server tracks every status that initializes an ongoing event using the `OngoingEvent` storable, and records metrics when the ending status arrives.

## Public API and Interfaces

### Metadata Management APIs

#### PutMetricSpec

Creates a new metric specification that defines how the server will collect and measure metrics.

```proto
rpc PutMetricSpec(PutMetricSpecRequest) returns (MetricSpec) {}
```

#### ListMetricSpecs

Lists existing metric specifications based on filter criteria.

```proto
rpc ListMetricSpecs(ListMetricSpecRequest) returns (MetricSpecList) {}
```

### Search and List APIs

#### ListMetrics

Lists the latest metrics for a given `MetricSpecId`.

```proto
rpc ListMetrics(ListMetricsRequest) returns (MetricList) {}
```

## Protobuf Definitions

### MetricSpec and Request Messages

```proto
// Creates a MetricSpec. This configuration specifies how the server will 
// collect and measure metrics.
message PutMetricSpecRequest {
  // Defines how the metric will be computed and collected
  AggregationType aggregation_type = 1;
  
  // Determines the entity or object for which the metric is being collected.
  oneof reference {
    // Refers to a specific node
    NodeReference node = 2;
    
    // Refers to a specific WfSpec
    WfSpecId wf_spec_id = 3;
    
    // Refers to a specific ThreadSpec within a WfSpec
    ThreadSpecReference thread_spec = 4;
  }
  
  // Defines the length for every window recorded for this MetricSpec
  google.protobuf.Duration window_length = 5;
}

// The MetricSpec defines how metrics should be collected and computed
message MetricSpec {
  // Unique identifier for this MetricSpec
  MetricSpecId id = 1;
  
  // When the MetricSpec was created
  google.protobuf.Timestamp created_at = 2;
  
  // List of aggregators that define how metrics are computed
  repeated Aggregator aggregators = 4;
}

// List MetricSpecs based on specified filter criteria.
// If no filters are applied, all MetricSpecs are returned
message ListMetricSpecRequest {
  // Filters the results based on the provided WfSpecId
  optional WfSpecId wf_spec_id = 1;
  
  // Filters the results based on the provided ThreadSpec
  optional ThreadSpecReference thread_spec_reference = 2;
}

// A list of MetricSpecs
message MetricSpecList {
  // Query result
  repeated MetricSpec results = 1;
}

// Types of metric aggregations that can be collected
enum AggregationType {
  // Counts occurrences within a time window
  COUNT = 0;
  
  // Computes average values
  AVG = 1;
  
  // Computes ratios between different counts
  RATIO = 2;
  
  // Measures latency (time between two events)
  LATENCY = 3;
}
```

### Aggregator Configuration

The `Aggregator` message defines how metrics are computed, including support for filtering by status ranges:

```proto
message Aggregator {
  // Status range filters for different object types
  message StatusRange {
    oneof type {
      LHStatusRange lh_status = 1;
      TaskRunStatusRange task_run = 2;
      UserTaskRunStatusRange user_task_run = 3;
    }
  }
  
  // Count aggregator - counts events within a window
  message Count {
    optional StatusRange status_range = 1;
  }

  // Ratio aggregator - computes ratios between counts
  message Ratio {
    optional StatusRange status_range = 2;
  }

  // Latency aggregator - measures time between status transitions
  message Latency {
    optional StatusRange status_range = 3;
  }

  // Window length for this aggregator
  google.protobuf.Duration window_length = 1;

  // Type of aggregation to perform
  oneof type {
    Count count = 2;
    Ratio ratio = 3;
    Latency latency = 4;
  }
}

// Status range for LittleHorse objects (WfRun, ThreadRun, NodeRun)
message LHStatusRange {
  LHStatus starts = 1;
  LHStatus ends = 2;
}

// Status range for TaskRun objects
message TaskRunStatusRange {
  TaskStatus starts = 1;
  TaskStatus ends = 2;
}

// Status range for UserTaskRun objects
message UserTaskRunStatusRange {
  UserTaskRunStatus starts = 1;
  UserTaskRunStatus ends = 2;
}
```

### Reference Types

```proto
// Reference to a NodeSpec which can be measured for metrics collection.
// Contains fields to specify the reference at various levels of granularity,
// such as by ThreadSpec, Node type, or node position.
message NodeReference {
  // Reference to the ThreadSpec where the node belongs
  optional ThreadSpecReference thread_spec = 1;
  
  // Specifies the type of node (e.g., UserTaskNode, TaskNode, etc.). 
  // If set to null, any node type is implied.
  optional string node_type = 2;
  
  // Indicates the position of the node within the specific thread. 
  // If set to null, any node within the thread is implied.
  optional int32 node_position = 3;
}

// Reference to a specific thread within a WfSpec
message ThreadSpecReference {
  // Reference to a specific WfSpec
  WfSpecId wf_spec_id = 1;
  
  // Represents a thread run number within a WfRun. 
  // If set to null, any thread run is implied.
  optional int32 thread_number = 2;
}
```

### Metric Identifiers

```proto
// Unique identifier for a MetricSpec
message MetricSpecId {
  oneof reference {
    NodeReference node = 1;
    WfSpecId wf_spec_id = 2;
    ThreadSpecReference thread_spec = 3;
  }
}

// Unique identifier for a Metric
message MetricId {
  MetricSpecId metric_spec_id = 1;
  google.protobuf.Duration window_length = 2;
  google.protobuf.Timestamp window_start = 3;
  AggregationType aggregation_type = 4;
}
```

### Metric Data

```proto
// Metric value for a given MetricId
message Metric {
  // Unique id of the metric value
  MetricId id = 1;
  
  // The value of the metric (type depends on AggregationType)
  oneof value {
    // Represents the value for a count-based metric
    int64 count = 2;
    
    // Represents the average latency for a latency-based metric
    int64 latency_avg = 3;
  }
  
  // Indicates when the metric was created
  google.protobuf.Timestamp created_at = 4;
  
  // Values per partition (for distributed aggregation)
  map<int32, double> value_per_partition = 5;
}

// Request to list metrics
message ListMetricsRequest {
  // Filter by metric spec id
  MetricSpecId metric_spec_id = 1;
  
  // Filter by window length
  google.protobuf.Duration window_length = 2;
  
  // Filter by aggregation type
  AggregationType aggregation_type = 3;
}

// A list of Metrics
message MetricList {
  // Query results
  repeated Metric results = 1;
}
```

### Partition-Level Metrics

For internal tracking and aggregation, partition-level metrics are stored:

```proto
// Partition-level metric tracking
message PartitionMetric {
  PartitionMetricId id = 1;
  google.protobuf.Timestamp created_at = 2;
  repeated PartitionWindowedMetric active_windows = 3;
  google.protobuf.Duration window_length = 4;
}

// Windowed metric data at partition level
message PartitionWindowedMetric {
  double value = 1;
  google.protobuf.Timestamp window_start = 2;
  int64 number_of_samples = 3;
}

// Identifier for partition-level metrics
message PartitionMetricId {
  MetricSpecId id = 1;
  TenantId tenant_id = 2;
  AggregationType aggregation_type = 3;
}
```

## Usage Examples

### Example 1: Count WfRun Completions

Create a metric spec to count how many `WfRun`s complete per hour.

**Using lhctl:**
```bash
lhctl put metricSpec \
  --aggregationType COUNT \
  --wfSpecName my-workflow \
  --windowLength 3600s
```

**Using gRPC (JSON):**
```json
{
  "aggregationType": "COUNT",
  "wfSpecId": {
    "name": "my-workflow"
  },
  "windowLength": "3600s"
}
```

### Example 2: Measure Task Latency

Create a metric spec to measure task execution latency:

**Using lhctl:**
```bash
lhctl put metricSpec \
  --aggregationType LATENCY \
  --wfSpecName my-workflow \
  --windowLength 300s
```

**Using gRPC (JSON):**
```json
{
  "aggregationType": "LATENCY",
  "wfSpecId": {
    "name": "my-workflow"
  },
  "windowLength": "300s"
}
```

### Example 3: Measure Specific Node Performance

Create a metric spec to measure latency for a specific node:

**Using lhctl:**
```bash
lhctl put metricSpec \
  --aggregationType LATENCY \
  --wfSpecName my-workflow \
  --threadNumber 0 \
  --nodePosition 3 \
  --windowLength 60s
```

**Using gRPC (JSON):**
```json
{
  "aggregationType": "LATENCY",
  "node": {
    "threadSpec": {
      "wfSpecId": {
        "name": "my-workflow"
      },
      "threadNumber": 0
    },
    "nodePosition": 3
  },
  "windowLength": "60s"
}
```

### Example 4: Query Metrics

List metrics for a given workflow:

**Using lhctl:**
```bash
# List all metrics for a workflow
lhctl list metrics \
  --wfSpecName my-workflow \
  --aggregationType LATENCY \
  --windowLength 300s

# Output:
# Metric ID: my-workflow | LATENCY | 300s | 2026-01-07T10:00:00Z
#   Latency Avg: 245ms
#   Count: 1523
#   Partition 0: 248.3ms
#   Partition 1: 241.7ms
```

**Using gRPC (JSON):**
```json
{
  "metricSpecId": {
    "wfSpecId": {
      "name": "my-workflow"
    }
  },
  "aggregationType": "LATENCY",
  "windowLength": "300s"
}
```

**Using SDK (Python):**
```python
metrics = client.list_metrics(
    metric_spec_id=MetricSpecId(
        wf_spec_id=WfSpecId(name="my-workflow")
    ),
    aggregation_type=AggregationType.LATENCY,
    window_length=Duration(seconds=300)
)

for metric in metrics.results:
    if metric.HasField("latency_avg"):
        print(f"Average latency: {metric.latency_avg}ms")
    elif metric.HasField("count"):
        print(f"Event count: {metric.count}")
```

### Example 5: Enable Metrics for a Specific Thread

To enable count metrics for a specific thread within a workflow:

**Using lhctl:**
```bash
lhctl put metricSpec \
  --aggregationType COUNT \
  --wfSpecName my-workflow \
  --threadNumber 1 \
  - Example 6: List All Metric Specs for a Workflow

Query all metric specs configured for a specific workflow:

**Using lhctl:**
```bash
lhctl list metricSpecs --wfSpecName my-workflow

# Output:
# MetricSpec: my-workflow | COUNT | 300s
# MetricSpec: my-workflow | LATENCY | 300s
# MetricSpec: my-workflow (thread 1) | COUNT | 300s
```

**Using gRPC (JSON):**
```json
{
  "wfSpecId": {
    "name": "my-workflow"
  }
}
```

**Using SDK (Go):**
```go
specs, err := client.ListMetricSpecs(ctx, &lhproto.ListMetricSpecRequest{
    WfSpecId: &lhproto.WfSpecId{Name: "my-workflow"},
})

for _, spec := range specs.Results {
    fmt.Printf("Found metric spec: %v\n", spec.Id)
    for _, agg := range spec.Aggregators {
        fmt.Printf("  Window: %vs\n", agg.WindowLength.Seconds)
    }
}
```

## Automatic Metric Creation
ontrol which metrics are auto-created (default: all enabled)
lh.metrics.auto.wfrun.enabled=true
lh.metrics.auto.task.enabled=true
lh.metrics.auto.node.enabled=true
lh.metrics.auto.user-task.enabled=true
```

### Default Metric Configurations

When `lh.metrics.auto.enabled=true`, the server automatically creates the following metrics for each registered `WfSpec`:

#### WfRun Metrics (when `lh.metrics.auto.wfrun.enabled=true`)

| Metric | Aggregation Type | Description |
|--------|-----------------|-------------|
| `wfrun.count` | COUNT | Total number of WfRun executions |
| `wfrun.latency` | LATENCY | Time from STARTING to COMPLETED |
| `wfrun.error.count` | COUNT | Number of WfRuns that ended in ERROR or EXCEPTION |

#### Task Metrics (when `lh.metrics.auto.task.enabled=true`)

| Metric | Aggregation Type | Description |
|--------|-----------------|-------------|
| `task.count` | COUNT | Total number of TaskRun executions |
| `task.latency.total` | LATENCY | Time from TASK_SCHEDULED to completion (SUCCESS/FAILED) |
| `task.latency.queue` | LATENCY | Time from TASK_SCHEDULED to TASK_RUNNING |
| `task.latency.execution` | LATENCY | Time from TASK_RUNNING to completion |
| `task.failure.count` | COUNT | Number of failed tasks (TASK_FAILED, TASK_EXCEPTION, TASK_TIMEOUT) |

#### Node Metrics (when `lh.metrics.auto.node.enabled=true`)

| Metric | Aggregation Type | Description |
|--------|-----------------|-------------|
| `node.count` | COUNT | Total number of NodeRun executions per node type |
| `node.latency` | LATENCY | Time from STARTING to COMPLETED per node |

#### UserTask Metrics (when `lh.metrics.auto.user-task.enabled=true`)

| Metric | Aggregation Type | Description |
|--------|-----------------|-------------|
| `usertask.count` | COUNT | Total number of UserTaskRun executions |
| `usertask.latency.total` | LATENCY | Time from creation to DONE |
| `usertask.latency.assignment` | LATENCY | Time from UNASSIGNED to ASSIGNED |
| `usertask.latency.completion` | LATENCY | Time from ASSIGNED to DONE |
| `usertask.cancelled.count` | COUNT | Number of cancelled UserTaskRuns |

### Automatic Creation Behavior

#### On WfSpec Registration

When a new `WfSpec` is registered via `PutWfSpec`:

1. **Check Global Configuration**: If `lh.metrics.auto.enabled=true`, proceed to step 2.
2. **Create Default MetricSpecs**: For each enabled metric type (wfrun, task, node, user-task), create the corresponding `MetricSpec` objects.
3. **Use Default Window**: All auto-created metrics use the configured `lh.metrics.auto.window-length` (default: 300s).
4. **Atomic Registration**: The `MetricSpec` objects are created in the same transaction as the `WfSpec` registration.
5. **Idempotent**: If the `WfSpec` already exists and has auto-metrics, they are not recreated.

#### Example: Automatic Metrics for "checkout-flow" WfSpec

When registering a workflow named "checkout-flow" with auto-metrics enabled:

```java
// User registers the workflow normally
client.putWfSpec(wfSpec);

// Server automatically creates these MetricSpecs (if auto.enabled=true):
// - MetricSpec: wfrun.count (COUNT, 300s window)
// - MetricSpec: wfrun.latency (LATENCY, 300s window, STARTING→COMPLETED)
// - MetricSpec: wfrun.error.count (COUNT, 300s window, ERROR|EXCEPTION status)
// - MetricSpec: task.count (COUNT, 300s window)
// - MetricSpec: task.latency.total (LATENCY, 300s window, SCHEDULED→SUCCESS|FAILED)
// - MetricSpec: task.latency.queue (LATENCY, 300s window, SCHEDULED→RUNNING)
// - MetricSpec: task.latency.execution (LATENCY, 300s window, RUNNING→SUCCESS|FAILED)
// - MetricSpec: task.failure.count (COUNT, 300s window, FAILED|EXCEPTION|TIMEOUT)
// ... and so on for nodes and user tasks
```

### Querying Auto-Created Metrics

Auto-created metrics can be queried like any other metric:

```java
// List all auto-created metrics for a workflow
MetricSpecList specs = client.listMetricSpecs(
    ListMetricSpecRequest.newBuilder()
        .setWfSpecId(WfSpecId.newBuilder().setName("checkout-flow"))
        .build()
);

// Query specific metric values
MetricList wfrunCounts = client.listMetrics(
    ListMetricsRequest.newBuilder()
        .setMetricSpecId(MetricSpecId.newBuilder()
            .setWfSpecId(WfSpecId.newBuilder().setName("checkout-flow")))
        .setAggregationType(AggregationType.COUNT)
        .setWindowLength(Duration.newBuilder().setSeconds(300))
        .build()
);
```

### Opt-Out Mechanism

If auto-metrics are globally enabled but not desired for a specific workflow, users can opt-out:

```proto
message PutWfSpecRequest {
  // ... existing fields ...
  
  // Set to true to disable auto-metric creation for this specific WfSpec
  // even when lh.metrics.auto.enabled=true
  optional bool disable_auto_metrics = 11;
}
```

```java
// Register workflow without auto-metrics
client.putWfSpec(
    PutWfSpecRequest.newBuilder()
        .setName("low-priority-workflow")
        .setDisableAutoMetrics(true)  // Opt-out
        // ... other fields
        .build()
);
```

### Benefits

- **Zero Configuration**: Metrics are available immediately after workflow registration without manual setup.
- **Consistent Baseline**: All workflows get the same standard metrics, making cross-workflow analysis easier.
- **Kafka-Like Experience**: Similar to how Kafka automatically provides broker and topic metrics.
- **Discoverability**: New teams can observe their workflows without knowing about metrics configuration.
- **Production-Ready Defaults**: Window lengths and aggregation types are chosen for typical production use cases.

### Considerations

- **Storage Impact**: Auto-creating metrics for all workflows increases storage. Mitigate by:
  - Using reasonable default window lengths (5 minutes)
  - Providing clear retention policies for auto-metrics
  - Supporting per-workflow opt-out via `disable_auto_metrics`
  
- **Performance Impact**: More metrics mean more computation. Optimize by:
  - Efficient windowing and aggregation in Kafka Streams
  - Limiting auto-metrics to the most valuable ones (avoid creating metrics for every possible status transition)
  - Using per-partition aggregation before global aggregation

- **Metric Naming**: Auto-created metrics follow a predictable naming convention:
  - Pattern: `{metric-category}.{metric-name}[.{detail}]`
  - Examples: `wfrun.count`, `task.latency.queue`, `usertask.cancelled.count`

## Implementation Details

### Storage Architecture

* **No External Dependencies**: Uses Kafka Streams repartitioning topology instead of external systems.
* **Per-Partition Storage**: Metrics are stored as `Storeable` objects on a per-partition basis.
* **Ongoing Event Tracking**: A new `OngoingEvent` storable tracks events from their start status to their end status.

### Performance Considerations

* **Write Amplification**: Each status change that affects an ongoing event requires an additional write to track the event.
* **Windowing**: Metrics are aggregated into configurable time windows to reduce storage overhead.
* **Retention**: Old metric windows can be pruned based on configurable retention policies.

### Repartitioning Topology

To enable global aggregation of metrics across partitions, we will use Kafka Streams repartitioning:

1. Metrics from individual partitions are published to a repartitioned topic.
2. A separate aggregation stream consumes and aggregates metrics globally.
3. Aggregated metrics are stored in a queryable state store.

### Metric Computation

#### Rate Metrics

* Count events within a time window
* Aggregate counts across partitions
* Store counts per window

#### Latency Metrics

* Track start time when event begins (status transition)
* Calculate duration when event completes
* Compute percentiles (P50, P95, P99) using approximation algorithms (e.g., T-Digest)
* Store aggregated statistics per window

#### Ratio Metrics

* Track multiple event counts
* Compute ratios between counts
* Store ratio values per window

## Affected Projects and Stakeholders

* **Dashboard Team** (@Bryson Glembin): Will need to display and configure workflow metrics on the LittleHorse Dashboard.
* **User Tasks Metrics** (@Jhosep Marin): User task-specific metrics integration.
* **Technical Advisor** (@Saúl Piña): Technical guidance and review.
* **CEO** (@Colt McNealy): Strategic alignment and business impact.

## Naming Conventions

### MetricSpec Names

`MetricSpec` names should follow a consistent pattern to make them easily identifiable and queryable:

**Pattern**: `{wfspec-name}.{object-type}.{metric-type}[.{detail}]`

**Examples**:
* `checkout-flow.workflow.latency`
* `checkout-flow.task.rate`
* `checkout-flow.node-3.latency`
* `global.task.latency` (for all workflows)

### RPC Names

All RPC methods follow the existing LittleHorse conventions:
* `PutMetricSpec` - Creates or updates a metric specification
* `ListMetricSpecs` - Lists metric specifications with filtering
* `ListMetrics` - Lists metric values
* `GetLatestMetric` - Retrieves the most recent metric value

## Dashboard Integration

The Dashboard team will need to implement:

1. **Metrics Configuration UI**: Interface to create and manage `MetricSpec`s.
2. **Metrics Visualization**: Charts and graphs to display metric values over time.
3. **Real-time Updates**: Live updates of metric values as new data arrives.
4. **Filtering and Search**: Filter metrics by workflow, time range, and metric type.
5. **Alerting** (future): Configure alerts based on metric thresholds.

## Testing

### Unit Tests

* Test metric computation logic for all metric types
* Test percentile calculation algorithms
* Test windowing and aggregation logic

### Integration Tests

* Test end-to-end metric collection for workflows
* Test metric queries with various filters
* Test pagination and bookmarking

### Performance Tests

* Measure impact of metric collection on workflow execution
* Measure storage overhead for different window sizes
* Measure query performance for large metric datasets

## Future Enhancements

### Phase 2: Advanced Features

* **Alerting**: Configure alerts when metrics exceed thresholds
* **Custom Aggregations**: Support for custom aggregation functions
* **Metric Exports**: Export metrics to external monitoring systems (Prometheus, Datadog, etc.)
* **Derived Metrics**: Metrics computed from other metrics
* **Anomaly Detection**: Automatic detection of unusual metric patterns

### Phase 3: Business Intelligence

* **Metric Dashboards**: Pre-built dashboards for common use cases
* **Metric Reports**: Scheduled reports delivered via email
* **Cross-Workflow Analytics**: Compare metrics across different workflows
* **Cost Analysis**: Metrics related to resource consumption and costs

## Backwards Compatibility

This is a new feature with no impact on existing workflows. All changes are additive:

* New RPC methods
* New protobuf messages
* New internal storage objects

Existing workflows will continue to function without any changes. Metrics collection is opt-in via `MetricSpec` creation.

## Rejected Alternatives

### External Metrics System (e.g., Prometheus)

**Why Rejected**: 
* Adds external dependency, violating design goal
* Requires additional infrastructure setup
* More complex deployment and operations
* Potential security concerns with exposing metrics externally

**Decision**: Use Kafka Streams repartitioning topology instead.

### Pre-compute All Possible Metrics

**Why Rejected**:
* Excessive storage overhead
* Performance impact on workflow execution
* Most metrics would never be queried
* Difficult to manage retention policies

**Decision**: Metrics are opt-in via `MetricSpec` creation.

### Store Full Event History

**Why Rejected**:
* Unbounded storage growth
* Query performance degradation over time
* Expensive to maintain

**Decision**: Use windowed aggregations with configurable retention.

## Open Questions

1. **Metric Retention**: What should be the default retention policy for metrics?
2. **Aggregation Granularity**: Should we support sub-window queries (e.g., 1-minute breakdown within a 1-hour window)?
3. **Real-time vs Batch**: Should metrics be computed in real-time or in micro-batches?
4. **Percentile Algorithm**: Which percentile approximation algorithm should we use (T-Digest, HdrHistogram, etc.)?

## Revisions

| Date | Author | Description |
|------|--------|-------------|
| 2026-01-07 | Eduardo Weber | Initial proposal |
