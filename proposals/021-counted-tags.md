# Counted Tags

**Authors:** Eduwer Camacaro

## Motivation

LittleHorse's existing `Search` RPCs return paginated lists of object IDs matching a set of attributes. This works well for browsing, but many use cases only need to know **how many** objects match—not which ones. For example:

* How many `NodeRun`s are currently associated with a given `WfSpec`?
* How many `TaskRun`s are in `TASK_SCHEDULED` state for a specific `TaskDef`?

Answering these questions today requires scanning all matching tags across all partitions, which is expensive and slow. We need a mechanism that maintains **pre-aggregated counts** so that count queries can be answered in constant time.

### Out of Scope

* **Time-windowed metrics** — covered by proposal 017 (Workflow Metrics).
* **Cross-tenant aggregation** — counts are always scoped to a single tenant.


## Current Architecture (Tags)

Today, each `Getable` declares its index configurations via `getIndexConfigurations()` and `getIndexValues()`. When a `Getable` is created or updated, the `TagStorageManager` creates `Tag` entries (secondary index records) in the store. These tags can be:

* **LOCAL** — stored in the same partition as the `Getable`.
* **REMOTE** — forwarded via the repartition topic to a deterministic partition for cross-partition search.

Both types store one record _per object_, which makes search possible but counting O(n).


## Proposed Solution: `COUNTED` Tag Storage Type

We introduce a third `TagStorageType`: **`COUNTED`**. Instead of storing one record per object, a `COUNTED` tag maintains a single counter that is incremented when an object is created and decremented when an object is deleted or its indexed attributes change.

### User-Facing API

Two new RPCs are exposed:

```proto
rpc CountNodeRun(CountNodeRunRequest) returns (Count) {}
rpc CountScheduledTaskRun(CountScheduledTaskRunRequest) returns (Count) {}

message CountNodeRunRequest {
  optional string wf_spec_name = 1;
  optional int32 wf_spec_major_version = 2;
  optional int32 wf_spec_revision = 3;
}

message CountScheduledTaskRunRequest {
  string task_def_name = 1;
}

message Count {
  int64 value = 1;
}
```

From the CLI:

```bash
# Count all NodeRuns for a WfSpec
lhctl count nodeRun --wfSpecName my-workflow

# Count NodeRuns for a specific version
lhctl count nodeRun --wfSpecName my-workflow --wfSpecMajorVersion 2

# Count NodeRuns for a specific revision
lhctl count nodeRun --wfSpecName my-workflow --wfSpecMajorVersion 2 --wfSpecRevision 1

# Count TaskRuns in TASK_SCHEDULED state
lhctl count scheduledTaskRun --taskDefName my-task
```

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

### Internal Data Flow

The counting mechanism is designed around Kafka Streams' partitioned processing model:

```
┌──────────────────────────────────────────────────────────────────────┐
│  Command Processor (partition N)                                      │
│                                                                      │
│  1. Getable created/updated/deleted                                  │
│  2. TagStorageManager detects COUNTED tag                            │
│  3. Increment/decrement PartitionCountedTag in memory + local store  │
│                                                                      │
│  ┌─────────────────────────────────────┐                             │
│  │  PartitionMetricsMemoryStore        │                             │
│  │  (in-memory accumulator per tag)    │                             │
│  └─────────────────────────────────────┘                             │
│                                                                      │
│  4. Punctuator fires periodically                                    │
│  5. Sends UpdateCountedTag command to core topic (keyed by           │
│     attribute string → deterministic partition)                       │
└──────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌──────────────────────────────────────────────────────────────────────┐
│  Command Processor (deterministic partition for attribute string)     │
│                                                                      │
│  6. Receives UpdateCountedTag                                        │
│  7. Reads/creates CountedTagModel from store                         │
│  8. Applies delta to count                                           │
│  9. Persists updated CountedTagModel                                 │
└──────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌──────────────────────────────────────────────────────────────────────┐
│  Count RPC                                                           │
│                                                                      │
│  10. Client calls CountNodeRun / CountScheduledTaskRun               │
│  11. Server computes attribute string from request                   │
│  12. Routes to the partition owning that attribute string             │
│  13. Reads CountedTagModel from store → returns count                │
└──────────────────────────────────────────────────────────────────────┘
```

### Key Components

| Component | Responsibility |
|-----------|---------------|
| `TagStorageManager` | Detects `COUNTED` tags and increments/decrements the `PartitionCountedTagModel` in memory and local store |
| `PartitionCountedTagModel` | Per-partition accumulator stored in the cluster-scoped store. Holds `tenantId`, `attributeString`, and a running `count` delta |
| `PartitionMetricsMemoryStore` | In-memory map that batches counted tag deltas between punctuations |
| `CommandProcessor` punctuator | Periodically drains the memory store and sends `UpdateCountedTag` commands to the core topic |
| `UpdateCountedTagModel` | A `CoreSubCommand` that applies the delta to the global `CountedTagModel` on the target partition |
| `CountedTagModel` | The authoritative counter stored on the deterministic partition, read at query time |
| `CountRequest<T>` | Abstract base class for count request models; computes the attribute string and routes to the correct partition |

### Consistency Model

Counted tags provide **eventual consistency**:

* Counts are accumulated locally and forwarded periodically (on each punctuation cycle).
* There is a small delay between a `Getable` being created/deleted and the global counter reflecting the change.
* The count is always converging toward the true value — no deltas are lost because they are persisted to the local store before being forwarded.

### Handling Rebalances

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
| `NodeRunModel` | `wfSpecName` | Count all NodeRuns for a WfSpec |
| `NodeRunModel` | `wfSpecName` + `wfSpecMajorVersion` | Count NodeRuns for a major version |
| `NodeRunModel` | `wfSpecName` + `wfSpecMajorVersion` + `wfSpecRevision` | Count NodeRuns for a specific revision |
| `TaskRunModel` | `taskDefName` (status=TASK_SCHEDULED) | Count queued tasks per TaskDef |


## Future Work

* **Additional count RPCs** — e.g., `CountWfRun`, `CountUserTaskRun`.
* **Expose counts in the dashboard** — display live counters for operational visibility.
* **Count with filters** — allow counting by additional attributes (e.g., `NodeRun` by status).

