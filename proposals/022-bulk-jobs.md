# Bulk Jobs

**Author:** Eduwer Camacaro

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

// Request to search for BulkJobs, optionally filtering by status.
message SearchBulkJobRequest {
  optional bytes bookmark = 1;
  optional int32 limit = 2;

  // If set, only return BulkJobs with this status.
  optional BulkJobStatus status = 3;
}

// A paginated list of BulkJobIds returned by SearchBulkJob.
message BulkJobIdList {
  repeated BulkJobId results = 1;
  optional bytes bookmark = 2;
}

// Request to delete a BulkJob. Only permitted for BulkJobs that are no longer
// RUNNING (i.e. BULK_JOB_COMPLETED or BULK_JOB_FAILED).
message DeleteBulkJobRequest {
  BulkJobId id = 1;
}

// Service RPCs (additions to LittleHorse service)
// rpc CreateBulkJob(CreateBulkJobRequest) returns (BulkJob) {}
// rpc GetBulkJob(GetBulkJobRequest) returns (BulkJob) {}
// rpc SearchBulkJob(SearchBulkJobRequest) returns (BulkJobIdList) {}
// rpc DeleteBulkJob(DeleteBulkJobRequest) returns (google.protobuf.Empty) {}
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

### Checking Status

```bash
lhctl get bulkJob <bulkJobId>
```

This returns the `BulkJob` object showing `status`, `subprocesses`, and `total_subprocesses`.

### Searching BulkJobs

BulkJobs can be listed (optionally filtered by status) via the `search` subcommand:

```
lhctl search bulkJob [--status <status>] [--limit <n>] [--bookmark <bookmark>]
```

| Argument | Required | Description |
|----------|----------|-------------|
| `--status` | No | If provided, only return `BulkJob`s with this status (`BULK_JOB_RUNNING`, `BULK_JOB_COMPLETED`, `BULK_JOB_FAILED`). |
| `--limit` | No | Maximum number of results per page. |
| `--bookmark` | No | Pagination bookmark returned by a previous search. |

```bash
# List all completed BulkJobs
lhctl search bulkJob --status BULK_JOB_COMPLETED
```

Under the hood this maps to `SearchBulkJob(SearchBulkJobRequest)` which returns a paginated `BulkJobIdList`. When a `status` filter is supplied the server performs an attribute-based `Tag` scan (`status=<value>`); otherwise it performs a prefix scan over all `BulkJob`s.

### Deleting a BulkJob

Completed or failed `BulkJob`s can be deleted via the `delete` subcommand:

```
lhctl delete bulkJob <bulkJobId>
```

This maps to `DeleteBulkJob(DeleteBulkJobRequest)`. Deletion is only permitted once the `BulkJob` is no longer `BULK_JOB_RUNNING`; attempting to delete a running job is rejected with `FAILED_PRECONDITION`. The guard is enforced both at the API handler (fast rejection) and again in the metadata command processor (to defend against a status transition between the check and the write).

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

### Time Budget

The punctuator runs on a 1-second wall-clock schedule and operates with a **time budget** (currently 50ms, `CommandProcessor.BULK_JOB_PUNCTUATION_BUDGET`) to keep each punctuation well within Kafka Streams transaction timeouts. In addition, it enforces a **command budget** (`CommandProcessor.BULK_JOB_MAX_COMMANDS_PER_PUNCTUATION`) that caps how many records a single punctuation appends within one punctuation, so a burst of matches can never overflow a transaction even if it stays under the time budget.