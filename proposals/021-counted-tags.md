# Counted Tags

**Authors:** Eduwer Camacaro

## Motivation

LittleHorse's existing `Search` RPCs return paginated lists of object IDs matching a set of attributes. This works well for browsing, but many use cases only need to know **how many** objects match—not which ones. For example:

* How many `NodeRun`s are currently associated with a given `WfSpec`?
* How many `TaskRun`s are in `TASK_SCHEDULED` state for a specific `TaskDef`?

Answering these questions today requires scanning all matching tags across all partitions, which requires multiple scan queries. We need a mechanism that maintains **pre-aggregated counts** so that count queries can be answered.

### Out of Scope

* **Time-windowed metrics** — covered by proposal 017 (Workflow Metrics).
* **Cross-tenant aggregation** — counts are always scoped to a single tenant.


## Proposed Solution: `COUNTED` Tag Storage Type

We introduce a third `TagStorageType`: **`COUNTED`**. Instead of storing one tag attribute per `Getable`, a `COUNTED` tag attribute maintains a single counter that is incremented when an object matches the attributes and decremented when an object is deleted or its indexed attributes change.

### User-Facing API

For this proposal, two new RPCs are exposed:

```proto
  // Counts the number of NodeRun's matching the given criteria. This is an eventually
  // consistent count maintained via pre-aggregated counters.
  rpc CountNodeRun(CountNodeRunRequest) returns (Count) {}

  // Counts the number of TaskRun's matching the given criteria for a specific TaskDef.
  // Useful for monitoring task queue depth and detecting backpressure on workers. This is
  // an eventually consistent count maintained via pre-aggregated counters.
  rpc CountTaskRun(CountTaskRunRequest) returns (Count) {}

// Request to count NodeRun's matching specified criteria. If no filter is set,
// the total count of all NodeRun's in the tenant is returned.
message CountNodeRunRequest {

  oneof filter {
    // Filter by WfSpec attributes.
    WfSpecFilter wf_spec_filter = 1;
  }

  // Filter NodeRun counts by WfSpec name, and optionally by major version and revision.
  message WfSpecFilter {
    // Filter by WfSpec name. Only NodeRun's belonging to this WfSpec are counted.
    string wf_spec_name = 1;

    // Filter by WfSpec major version. Requires wf_spec_name to be set.
    optional int32 wf_spec_major_version = 2;

    // Filter by WfSpec revision. Requires both wf_spec_name and wf_spec_major_version to be set.
    optional int32 wf_spec_revision = 3;
  }
}

// Request to count TaskRun's matching the given criteria for a specific TaskDef.
// The task_def_name is required. The status filter narrows the count to TaskRun's
// in a specific state. Initially, only TASK_SCHEDULED is supported as a counted status.
message CountTaskRunRequest {
  // The name of the TaskDef whose TaskRun's should be counted.
  string task_def_name = 1;

  // Filter by TaskRun status. Required. Initially only TASK_SCHEDULED is supported;
  // the server will reject requests with unsupported status values.
  TaskStatus status = 2;
}

// Response containing an eventually consistent count value.
message Count {
  // The count of objects matching the request criteria.
  int64 value = 1;
}
```

From the CLI:

```bash
# Count all NodeRuns in the tenant
lhctl count nodeRun --all

# Count all NodeRuns for a WfSpec
lhctl count nodeRun --wfSpecName my-workflow

# Count NodeRuns for a specific version
lhctl count nodeRun --wfSpecName my-workflow --wfSpecMajorVersion 2

# Count NodeRuns for a specific revision
lhctl count nodeRun --wfSpecName my-workflow --wfSpecMajorVersion 2 --wfSpecRevision 1

# Count TaskRuns in TASK_SCHEDULED state
lhctl count taskRun my-task --status TASK_SCHEDULED
```

## Current Architecture (Tags)

Today, each `Getable` declares its index configurations via `getIndexConfigurations()` and `getIndexValues()`. When a `Getable` is created or updated, the `TagStorageManager` creates `Tag` entries (secondary index records) in the store. These tags can be:

* **LOCAL** — stored in the same partition as the `Getable`.
* **REMOTE** — forwarded via the repartition topic to a deterministic partition for cross-partition search.

Note: the REMOTE type is not currently used in LittleHorse. It was disabled in the past and no longer supported. Future versions of LittleHorse may support REMOTE tags again.

Both types store one record _per object_, which makes search possible but counting O(n).

### How a Getable Declares a Counted Index

A `Getable` marks an index as counted by specifying `TagStorageType.COUNTED` in its `GetableIndex` configuration:

```java
// In NodeRunModel
new GetableIndex<>(
    List.of(Pair.of("wfSpecName", GetableIndex.ValueType.SINGLE)),
    Optional.of(TagStorageType.COUNTED));
```

The `IndexedField` returned by `getIndexValues()` must also carry `TagStorageType.COUNTED`:

```java
return List.of(new IndexedField(key, wfSpecId.getName(), TagStorageType.COUNTED));
```

### Consistency Model

Counted tags provide **eventual consistency**:

* Counts are accumulated locally and forwarded periodically (on each punctuation cycle).
* There is a small delay between a `Getable` being created/deleted and the global counter reflecting the change.
* The count is always converging toward the true value — no deltas are lost because they are persisted to the local store before being forwarded.

### Handling Rebalances

Counted tags forward their counts to the repartition topic using the same mechanism as Metrics. See proposal 017 (Workflow Metrics) for details on how the `CommandProcessor` handles metrics forwarding and rebalances. The same logic applies to counted tags:

When a partition is reassigned after a rebalance:

1. The `CommandProcessor` starts with `shouldUseMetricsHint = true`.
2. It reads any `PartitionCountedTagModel` entries still persisted in the cluster-scoped store (deltas not yet forwarded).
3. It drains those before switching to the in-memory path.

This ensures no count deltas are lost during partition reassignment.


## Adding a New Count RPC

To add a count query for a new entity type:

1. **Mark the index as `COUNTED`** in the `Getable`'s `getIndexConfigurations()`.
2. **Return `TagStorageType.COUNTED`** in the corresponding `getIndexValues()`.
3. **Define the proto request message** (e.g., `CountFooRequest`) and add an RPC to `service.proto`.
4. **Implement a `CountRequest<T>` subclass** that builds the correct `Attribute` list.
5. **Wire the RPC** in `LHServerListener` using `handleCount(reqModel)`.
6. **Add the CLI command** under `lhctl count`.


## Current Counted Indexes

| Getable | Attribute(s) | Use Case |
|---------|-------------|----------|
| `NodeRunModel` | _(none)_ | Count all NodeRuns in the tenant |
| `NodeRunModel` | `wfSpecName` | Count all NodeRuns for a WfSpec |
| `NodeRunModel` | `wfSpecName` + `wfSpecMajorVersion` | Count NodeRuns for a major version |
| `NodeRunModel` | `wfSpecName` + `wfSpecMajorVersion` + `wfSpecRevision` | Count NodeRuns for a specific revision |
| `TaskRunModel` | `taskDefName` + `status` (TASK_SCHEDULED) | Count queued tasks per TaskDef |


## Future Work

* **Additional count RPCs** — e.g., `CountWfRun`, `CountUserTaskRun`.
* **Expose counts in the dashboard** — display live counters for operational visibility.
* **Count with filters** — allow counting by additional attributes (e.g., `NodeRun` by status).