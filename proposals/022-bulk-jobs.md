# Bulk Jobs

## Motivation

Today, LittleHorse only supports deleting (or operating on) a single `WfRun` at a time. In production environments, users often need to perform bulk operations such as deleting thousands of `WfRun`s that match certain criteria (e.g., all completed runs of a given `WfSpec` within a time range). Currently, this requires scripting pagination through `SearchWfRun` and issuing individual `DeleteWfRun` calls—an error-prone and slow process.

**BulkJob** introduces a first-class mechanism to run large-scale background operations (starting with bulk deletion of `WfRun`s) efficiently within the LH Server.

### Out of Scope

* Bulk operations beyond `WfRun` deletion (future extensions).

## Public Contract

### Protobuf

```proto
// The status of a BulkJob or BulkJobShard.
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
    BulkDeleteWfRun bulk_delete_wf_run = 4;
  }

  message Subprocess {
    // The ID of this subprocess, from 1 to total_subprocesses.
    int32 id = 1;

    // Current status of this subprocess.
    BulkJobStatus status = 2;

    // Timestamp of the last key seen by this subprocess. Updated periodically as the
    // subprocess makes progress, so users can observe how far a shard has advanced.
    google.protobuf.Timestamp last_seen_key = 3;
  }

  // Per-partition subprocesses tracking progress.
  repeated Subprocess subprocesses = 5;

  // The BulkJob is divided into this many subprocesses that run in parallel.
  int32 total_subprocesses = 6;
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
  optional LHStatus wf_run_status = 4;
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

### Internal Protobuf

```proto
// Internal storeable that tracks the range scan cursor for a BulkJobShard.
// Not exposed via the public API.
message BulkJobShardCursor {
  BulkJobId bulk_job_id = 1;
  string last_key = 2;
  bool scan_completed = 3;
  optional google.protobuf.Timestamp last_seen_timestamp = 5;
}

// Cluster-scoped internal registry entry that signals a BulkJob is currently active.
// Created when a BulkJob starts; deleted when it completes or fails.
// The punctuator iterates these to discover work without needing to scan per-tenant.
message ActiveBulkJob {
  ActiveBulkJobId id = 1;
  google.protobuf.Timestamp created_at = 2;
}

message ActiveBulkJobId {
  BulkJobId bulk_job_id = 2;
  TenantId tenant_id = 3;
}

// Report sent from the punctuator (core topology) back to the metadata topology
// to update the BulkJob's subprocess status.
message BulkJobShardReport {
  BulkJobId bulk_job_id = 1;
  int32 partition = 2;
  bool completed = 3;
  string last_seen_key = 4;
  optional google.protobuf.Timestamp last_seen_timestamp = 5;
}
```

## `lhctl` Command

The CLI exposes bulk deletion of `WfRun`s via the `delete` subcommand:

```
lhctl delete wfRunBulk <wfSpecName> --from <dateFrom> --to <dateTo> [--status <status>] [--id <id>]
```

### Arguments

| Argument | Required | Description |
|----------|----------|-------------|
| `<wfSpecName>` | Yes | Name of the `WfSpec` whose `WfRun`s should be deleted. |
| `--from` | Yes | Start of the time range (inclusive). ISO 8601 format (e.g., `2025-01-01T00:00:00Z`). |
| `--to` | Yes | End of the time range (inclusive). ISO 8601 format (e.g., `2025-06-01T00:00:00Z`). |
| `--status` | No | If provided, only delete `WfRun`s with this status (e.g., `COMPLETED`, `ERROR`). |
| `--id` | No | Client-provided ID for idempotency. |

### Example Usage

```bash
# Delete all WfRuns of "my-workflow" between Jan 1 and Jun 1, 2025
lhctl delete wfRunBulk my-workflow --from 2025-01-01T00:00:00Z --to 2025-06-01T00:00:00Z

# Delete only COMPLETED WfRuns of "my-workflow" in that range
lhctl delete wfRunBulk my-workflow --from 2025-01-01T00:00:00Z --to 2025-06-01T00:00:00Z --status COMPLETED

# With a custom ID for idempotency
lhctl delete wfRunBulk my-workflow --from 2025-01-01T00:00:00Z --to 2025-06-01T00:00:00Z --id my-bulk-job-123
```

### Behavior

1. The CLI constructs a `CreateBulkJobRequest` with a `BulkDeleteWfRun` operation.
2. The server creates a `BulkJob` and returns it immediately.
3. The CLI prints the `BulkJobId` so users can check progress via `lhctl get bulkJob <id>`.
4. The deletion proceeds in the background within the server.

### Checking Status

```bash
lhctl get bulkJob <bulkJobId>
```

This returns the `BulkJob` object showing `status`, `subprocesses`, and `total_subprocesses`.

## Architecture

### Overview

The `BulkJob` execution is split into **two distinct steps** to ensure Kafka Streams is always making progress and never blocked by a large number of deletions within a single transaction:

1. **Step 1 — Tag Range Scan (Punctuator):** A time-budgeted punctuator on the `CommandProcessor` performs a range scan over `Tag` indices to discover matching `WfRunId`s. For each discovered ID, it forwards an individual `DeleteWfRunRequest` command to the repartition topic.

2. **Step 2 — Deletion (DeleteWfRunRequest command):** Each `DeleteWfRunRequest` is processed as a standard command, deleting a single `WfRun` per Kafka Streams transaction. This reuses the existing deletion logic and guarantees that the processor is never stuck deleting many `WfRun`s in one go.

### Key Objects

| Object | Scope | Store | Description |
|--------|-------|-------|-------------|
| `BulkJob` | Tenant-scoped | Global Metadata Store | User-facing metadata object. Tracks overall status and per-subprocess progress. |
| `ActiveBulkJob` | Cluster-scoped | Global Metadata Store | Internal registry entry. Exists only while the BulkJob is RUNNING. Allows the punctuator to discover active jobs with a single prefix scan without iterating tenants. |
| `BulkJobShardCursor` | Partition-local | Core Store | Internal storeable. Tracks the range scan cursor (last iterated key) for this partition. Not exposed to users. |

> Per-partition progress is surfaced to users through the `BulkJob.subprocesses` list (one `Subprocess` per partition, each with a `status` and `last_seen_key`), not a separate object. The only per-partition internal object is `BulkJobShardCursor`.

### Flow

1. **Job Creation (`CreateBulkJobRequest`):** The `MetadataProcessor` stores:
   - A **`BulkJob`** (tenant-scoped) in the Global Metadata Store with status `BULK_JOB_RUNNING`. Contains the operation details and a `subprocesses` list (one entry per partition, initially all `BULK_JOB_RUNNING`).
   - An **`ActiveBulkJob`** (cluster-scoped) registry entry containing only the `BulkJobId` and `tenantId`. This enables the punctuator to discover active jobs via a single prefix scan.

2. **Punctuator Discovery:** Each `CommandProcessor` schedules a `BulkJobPunctuator` on a **1-second wall-clock interval**. On each tick it:
   - Computes a **deadline** (`now + punctuationBudget`) to bound its own runtime and avoid exceeding Kafka Streams transaction timeouts. The clock is injectable (defaults to `Instant::now`) so the budget is deterministically testable.
   - Derives a shared `outOfBudget` predicate used by both the inter-job loop and the intra-job Tag scan.
   - Scans `ActiveBulkJob` registry entries via a cluster-scoped prefix range scan.
   - For each entry, resolves the `tenantId` and `BulkJobId`.
   - Checks `outOfBudget` before processing each job — if exceeded, breaks the loop (remaining jobs are processed on the next tick).

3. **Shard Cursor Check:** For each active job, the punctuator checks the partition's Core Store for a `BulkJobShardCursor`:
   - If no cursor exists, a new one is created (this is the first time this partition processes this job).
   - If a cursor exists and `scan_completed == true`, this partition's work is done — skip.
   - Otherwise, resume from the cursor's `last_key`.

4. **Time-Budgeted Tag Range Scan:** The punctuator delegates to `BulkJobModel.tryToComplete()`, which calls `BulkDeleteWfRunModel.process()`. This method:
   - Builds a `TagScan` from the job criteria (`wf_spec_name`, time range, optional status).
   - Determines the scan start: the cursor's `last_key` if resuming, otherwise the range's computed start key.
   - Performs a range scan over matching `Tag`s in the Core Store. Because the resume `last_key` is the **full Tag store key** of the last-deleted `WfRun` and the range is inclusive of it, the scan **skips that boundary key** on resume so each matching `WfRun` is deleted **exactly once** across punctuations.
   - For each remaining tag, forwards a **`DeleteWfRunRequest`** command (wrapped in an `LHTimer`) to the repartition topic, keyed by the target `WfRunId`, and records the tag's store key and `createdAt` as the cursor's `last_key` / `last_seen_timestamp`.
   - Checks `outOfBudget` at the top of each iteration. If exceeded, saves the current cursor position and returns without setting `scan_completed = true`. The next tick resumes from the saved position.

5. **Shard Report:** After each iteration (whether the scan completed or was interrupted by the deadline), the punctuator:
   - Saves the updated `BulkJobShardCursor` to the Core Store.
   - Forwards a **`BulkJobShardReport`** as a `MetadataCommand` to the metadata topic, reporting the partition's progress (completed flag, last seen key, timestamp).

6. **Deletion (per WfRun):** Each forwarded `DeleteWfRunRequest` is processed by the `CommandProcessor` as a normal command — deleting a single `WfRun` and its associated data in its own Kafka Streams transaction.

7. **Job Completion:** The `MetadataProcessor` receives `BulkJobShardReport`s and, in `BulkJobModel.updateShard`, updates the corresponding `Subprocess` entry — setting its `status` and its `last_seen_key` from the report's `last_seen_timestamp`. (The read-modify-write disables the metadata cache to avoid lost updates when multiple shard reports merge into the same `BulkJob`.) Once all subprocesses report `completed = true`, the `BulkJob` transitions to `BULK_JOB_COMPLETED` and the `ActiveBulkJob` registry entry is deleted. If an unrecoverable error occurs, the job transitions to `BULK_JOB_FAILED` and the registry entry is also deleted.

### Time Budget

The punctuator runs on a 1-second wall-clock schedule and operates with a **time budget** (currently 50ms, `CommandProcessor.BULK_JOB_PUNCTUATION_BUDGET`) to keep each punctuation well within Kafka Streams transaction timeouts. The budget is evaluated against an injectable clock and enforced at two levels via a shared `outOfBudget` predicate:

1. **Inter-job:** Before processing each `ActiveBulkJob`, the punctuator checks `outOfBudget`. If exhausted, it breaks the loop and defers remaining jobs to the next tick.
2. **Intra-job:** Inside the Tag range scan loop, each iteration checks `outOfBudget`. If exhausted, the scan saves its cursor position (full Tag store key + last-seen timestamp) and returns early; the next tick resumes from exactly that position, skipping the boundary key so no `WfRun` is deleted twice.
