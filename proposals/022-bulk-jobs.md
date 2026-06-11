# Bulk Jobs

## Motivation

Today, LittleHorse only supports deleting (or operating on) a single `WfRun` at a time. In production environments, users often need to perform bulk operations such as deleting thousands of `WfRun`s that match certain criteria (e.g., all completed runs of a given `WfSpec` within a time range). Currently, this requires scripting pagination through `SearchWfRun` and issuing individual `DeleteWfRun` calls—an error-prone and slow process.

**BulkJob** introduces a first-class mechanism to run large-scale background operations (starting with bulk deletion of `WfRun`s) efficiently within the LH Server.

### Out of Scope

* Bulk operations beyond `WfRun` deletion (future extensions).

## Public Contract

### Protobuf

```proto
// Identifies a BulkJob.
message BulkJobId {
  // The unique identifier of the BulkJob.
  string id = 1;
}

// The status of a BulkJob.
enum BulkJobStatus {
  // The job is actively being processed. This is the initial state.
  BULK_JOB_RUNNING = 0;

  // The job completed successfully.
  BULK_JOB_COMPLETED = 1;

  // The job failed.
  BULK_JOB_FAILED = 2;
}

// A BulkJob represents a long-running background operation that affects many objects.
message BulkJob {
  // The ID of this BulkJob.
  BulkJobId id = 1;

  // When the BulkJob was created.
  google.protobuf.Timestamp created_at = 2;

  // Current status of the BulkJob.
  BulkJobStatus status = 3;

  // The specific operation to perform.
  oneof operation {
    BulkDeleteWfRun bulk_delete_wf_run = 10;
  }

  // Progress information: total number of items to process.
  int64 total_items = 20;

  // Progress information: number of items processed so far.
  int64 processed_items = 21;
}

// Represents a shard of a BulkJob running on a single partition.
// This is stored in the per-partition Core Store (not on the BulkJob metadata object)
// since each partition independently manages its own shard.
// Users can query shards via a dedicated RPC to get per-partition progress.
message BulkJobShard {
  // The BulkJob this shard belongs to.
  BulkJobId bulk_job_id = 1;

  // The partition this shard is operating on.
  int32 partition = 2;

  // Current status of this shard.
  BulkJobStatus status = 3;

  // Number of items processed by this shard so far.
  int64 processed_items = 4;
}

// Describes a bulk deletion of WfRun's matching certain criteria.
message BulkDeleteWfRun {
  // The name of the WfSpec whose WfRun's should be deleted. Required.
  string wf_spec_name = 1;

  // Only delete WfRun's that started at or after this time. Required.
  google.protobuf.Timestamp earliest_start = 2;

  // Only delete WfRun's that started at or before this time. Required.
  google.protobuf.Timestamp latest_start = 3;

  // If set, only delete WfRun's with this status.
  optional LHStatus status = 4;
}

// Request to create a BulkJob.
message CreateBulkJobRequest {
  // Optional client-provided ID for idempotency.
  optional string id = 1;

  // The operation to perform.
  oneof operation {
    BulkDeleteWfRun bulk_delete_wf_run = 10;
  }
}

// Request to get the status of a BulkJob.
message GetBulkJobRequest {
  // The ID of the BulkJob to retrieve.
  BulkJobId id = 1;
}

// Service RPCs (additions to LittleHorse service)
// rpc CreateBulkJob(CreateBulkJobRequest) returns (BulkJob) {}
// rpc GetBulkJob(GetBulkJobRequest) returns (BulkJob) {}
```

## `lhctl` Command

The CLI will expose bulk deletion of `WfRun`s via the existing `delete` subcommand:

```
lhctl delete wfRun <wfSpecName> --from <dateFrom> --to <dateTo> [--status <status>]
```

### Arguments

| Argument | Required | Description |
|----------|----------|-------------|
| `<wfSpecName>` | Yes | Name of the `WfSpec` whose `WfRun`s should be deleted. |
| `--from` | Yes | Start of the time range (inclusive). ISO 8601 format (e.g., `2025-01-01T00:00:00Z`). |
| `--to` | Yes | End of the time range (inclusive). ISO 8601 format (e.g., `2025-06-01T00:00:00Z`). |
| `--status` | No | If provided, only delete `WfRun`s with this status (e.g., `COMPLETED`, `ERROR`). |

### Example Usage

```bash
# Delete all WfRuns of "my-workflow" between Jan 1 and Jun 1, 2025
lhctl delete wfRun my-workflow --from 2025-01-01T00:00:00Z --to 2025-06-01T00:00:00Z

# Delete only COMPLETED WfRuns of "my-workflow" in that range
lhctl delete wfRun my-workflow --from 2025-01-01T00:00:00Z --to 2025-06-01T00:00:00Z --status COMPLETED
```

### Behavior

1. The CLI constructs a `CreateBulkJobRequest` with a `BulkDeleteWfRun` operation.
2. The server creates a `BulkJob` and returns its ID immediately.
3. The CLI prints the `BulkJobId` so users can check progress via `lhctl get bulkJob <id>`.
4. The deletion proceeds in the background within the server.

### Checking Status

```bash
lhctl get bulkJob <bulkJobId>
```

This returns the `BulkJob` object showing `status`, `total_items`, and `processed_items`.

## Architecture

### Overview

The `BulkJob` execution is split into **two distinct steps** to ensure Kafka Streams is always making progress and never blocked by a large number of deletions within a single transaction:

1. **Step 1 — Tag Range Scan (Punctuator):** A time-budgeted punctuator on the `CommandProcessor` performs a range scan over `Tag` indices to discover matching `WfRunId`s. For each discovered ID, it forwards an individual `DeleteWfRunRequest` command to the repartition topic.

2. **Step 2 — Deletion (DeleteWfRunRequest command):** Each `DeleteWfRunRequest` is processed as a standard command, deleting a single `WfRun` per Kafka Streams transaction. This reuses the existing deletion logic and guarantees that the processor is never stuck deleting many `WfRun`s in one go.

### Flow

1. **Job Creation:** The `rpc CreateBulkJob` stores a new `BulkJob` metadata object (tenant-scoped) in the Global Metadata Store. This object contains all the information needed to execute the operation (e.g., `wf_spec_name`, time range, optional status filter). The `BulkJob` is created with status `BULK_JOB_RUNNING`. Additionally, a cluster-scoped **`ActiveBulkJob`** registry entry is stored (containing only the `BulkJobId` and `tenantId`). This allows the punctuator to discover active jobs with a single prefix scan without needing to iterate over all tenants.

2. **Punctuator Detection & Shard Spawning:** Each `CommandProcessor` runs a punctuator that periodically scans the cluster-scoped `ActiveBulkJob` registry entries in the global store. For each entry, the punctuator resolves the tenant context and checks whether a **`BulkJobShard`** already exists for that `BulkJobId` in the local partition store. If one exists, the job is skipped (this partition has already spawned a shard for it).

3. **Spawning a Shard:** If no shard exists, the punctuator creates a **`BulkJobShard`** in the partition's local store, keyed by the `BulkJobId`, along with a **`BulkJobShardCursor`** to track scan progress internally. The `BulkJobShard` represents this partition's share of the work and prevents duplicate processing on subsequent punctuator ticks.

4. **Time-Budgeted Tag Range Scan (Punctuator):** The punctuator performs a range scan over the relevant `Tag` index to discover `WfRunId`s matching the job's criteria. The scan operates with a **time budget**: it processes as many tags as it can within the budget, forwarding a **`DeleteWfRunRequest`** command for each discovered `WfRunId` to the repartition topic (keyed by that `WfRunId`, so it routes back to the same partition). If the budget is exhausted before the scan completes:
   - It **saves a bookmark/cursor** on a **`BulkJobShardCursor`** (an internal storeable, not exposed via the public API) indicating where the range scan left off.
   - On the next punctuator tick, it **resumes from the cursor** and continues forwarding.

   The `BulkJobShardCursor` is a separate internal storeable keyed by `BulkJobId` + partition. It holds implementation details (e.g., the last iterated key) that users don't need to see. The `BulkJobShard` itself remains a clean, user-facing object.

5. **Deletion (per WfRun):** Each forwarded `DeleteWfRunRequest` is processed by the `CommandProcessor` as a normal command — deleting a single `WfRun` and its associated data (e.g., `NodeRun`s, `Variable`s, `TaskRun`s) in its own Kafka Streams transaction. This ensures the processor is never blocked by a large batch of deletions.

6. **Shard Completion:** Once a partition's scan finds no more matching `WfRun`s (range scan exhausted), its `BulkJobShard` is marked `COMPLETED` and removed from the store.

7. **Job Completion:** The overall `BulkJob` transitions to `BULK_JOB_COMPLETED` once all shards across all partitions have completed. The cluster-scoped `ActiveBulkJob` registry entry is deleted at this point. If an unrecoverable error occurs on any shard, it transitions to `BULK_JOB_FAILED` and the registry entry is also deleted.

### Why Key Each DeleteWfRunRequest by Its Own WfRunId?

Since `WfRun`s are partitioned by their ID, keying each `DeleteWfRunRequest` by the target `WfRunId` guarantees the command lands on the partition that owns that `WfRun`. In the context of a `BulkJob`, the punctuator only scans its own local store, so all discovered IDs already belong to the current partition — the forwarded commands route back to the same partition.

### Key Design Decisions

| Decision | Rationale |
|----------|-----------|
| `BulkJob` stored as a metadata object in the Core Store | Leverages existing persistence and replication; survives rebalances. |
| Two-step split: scan vs. deletion | The punctuator handles discovery (read-only scan), and each deletion is its own command/transaction. This prevents Kafka Streams from being blocked by a large batch of writes in a single transaction. |
| Sub-process per partition | Each partition independently spawns a `BulkJobShard` that tracks its own progress. This exposes per-partition status to users and prevents duplicate work. |
| Punctuator-driven discovery (only `RUNNING` jobs) | Each partition independently detects and processes jobs. Ignoring terminal states avoids unnecessary work. |
| Time-budgeted punctuator with bookmark/resume | Keeps punctuator execution bounded; avoids blocking regular command processing or causing Kafka Streams timeouts. |
| Reuse of `DeleteWfRunRequest` | Each `WfRun` deletion is a single, small Kafka Streams transaction — no risk of oversized transactions or blocking. |

### Diagram

```
┌──────────────────────────────────────────────────────────────────────────┐
│                   Core Topology (per partition)                            │
│                                                                           │
│  ┌─────────────────────────────────────────────┐                          │
│  │         CommandProcessor                    │                          │
│  │                                             │                          │
│  │  ┌───────────────────────────────────────┐  │                          │
│  │  │  Punctuator (time-budgeted)           │  │                          │
│  │  │  1. Scans for BulkJob metadata        │  │                          │
│  │  │  2. Spawns BulkJobShard              │  │                          │
│  │  │  3. Range-scans Tags for WfRunIds     │  │                          │
│  │  │  4. Forwards DeleteWfRunRequest per ID │  │                          │
│  │  │  5. Saves bookmark if budget exhausted │  │                          │
│  │  └────────────────┬──────────────────────┘  │                          │
│  │                   │                         │                          │
│  └───────────────────┼─────────────────────────┘                          │
│                      │ forward DeleteWfRunRequest (key = wfRunId)          │
│                      ▼                                                    │
│            ┌──────────────────┐                                           │
│            │ Repartition Topic│                                           │
│            └────────┬─────────┘                                           │
│                     │ routes back to same partition                        │
│                     ▼                                                      │
│  ┌─────────────────────────────────────────────┐                          │
│  │         CommandProcessor                    │                          │
│  │  (processes DeleteWfRunRequest)             │                          │
│  │  - deletes single WfRun + associated data   │                          │
│  │  - one Kafka Streams transaction per WfRun  │                          │
│  └─────────────────────────────────────────────┘                          │
│                                                                           │
│                    ┌──────────┐                                            │
│                    │CoreStore │                                            │
│                    │(RocksDB) │                                            │
│                    └──────────┘                                            │
└──────────────────────────────────────────────────────────────────────────┘
```
