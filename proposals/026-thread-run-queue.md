# Proposal: Thread Run Queue

Author: Jake Rose

This proposal introduces a `ThreadRun` queue that allows a `WfRun` to create more `ThreadRun`s than `LHS_X_ACTIVE_THREAD_RUNS_PER_WF_RUN` without failing at runtime. It builds on the [`ThreadRun` archival proposal](012-archive-thread-run.md) by extending `InactiveThreadRun` to represent both archived and queued `ThreadRun`s.

## Background

For more detail about why a `WfRun` is an unbounded object, refer to the [background section of the `ThreadRun` archival proposal](012-archive-thread-run.md#background).

The LittleHorse Server can hold only a configured number of active `ThreadRun`s in a `WfRun`. Previously, the Server returned an internal error when a `WfRun` attempted to exceed this limit. The queue allows a `WfRun` to create additional `ThreadRun`s while maintaining a strict cap on the number held in memory.

## Extending `InactiveThreadRun`

Previously, `InactiveThreadRun` only stored completed or archived `ThreadRun`s:

```proto
message InactiveThreadRun {
  ThreadRun thread_run = 1;
}
```

An inactive `ThreadRun` can now be either archived or queued. The `oneof` keeps these states distinct and allows the message to be extended in the future. The ID identifies the inactive record directly, while queued information stores the input variables needed to start the `ThreadRun` later.

```proto
message InactiveThreadRun {
  ThreadRun thread_run = 1;

  oneof inactive_reason {
    ArchivedThreadRunInfo archived = 2;
    QueuedThreadRunInfo queued = 3;
  }

  InactiveThreadRunId id = 4;
}

message ArchivedThreadRunInfo {}

message QueuedThreadRunInfo {
  map<string, VariableValue> input_vars = 1;
}
```

## Extending `WfRun`

The `WfRun` stores an ordered queue of queued `ThreadRun` numbers. The corresponding `ThreadRun` data and input variables are stored in `InactiveThreadRun` records rather than in the active `thread_runs` collection.

```proto
message WfRun {
  // ...
  repeated int32 thread_run_queue = 14;
}
```

## Enqueueing a `ThreadRun`

When `startThread()` is invoked and the number of in-memory `ThreadRun`s is greater than or equal to `LHS_X_ACTIVE_THREAD_RUNS_PER_WF_RUN`, the new `ThreadRun` is stored as a queued `InactiveThreadRun` instead of being started immediately.

The queued `ThreadRun`:

- Receives the next `ThreadRun` number, so `greatest_thread_run_number` and iteration continue to include it.
- Has status `STARTING` and has no `NodeRun` until activation.
- Is added to its parent's `child_thread_ids`.
- Stores its input variables in `QueuedThreadRunInfo`.
- Has its number appended to `thread_run_queue`.

Because the parent records the queued child, an `EXIT` node implicitly waits for it. A queued child with status `STARTING` is neither terminated nor halted, so the parent cannot complete while that child is waiting to run.

## Halting Queued `ThreadRun`s

Queued `ThreadRun`s participate in halt propagation even though they have not started. When a queued `ThreadRun` is halted, the halt reason is stored on it and its status becomes `HALTED`. This path does not access a current `NodeRun`, because one does not exist yet.

When a parent is halted, its descendants receive a `PARENT_HALTED` reason. A queued child remains in the queue until all of its halt reasons are resolved. Queued `ThreadRun`s do not directly receive interrupt events and cannot fail before starting, so `INTERRUPTED` and `HANDLING_FAILURE` apply to active `ThreadRun`s rather than queued ones. The corresponding interrupt or failure-handler `ThreadRun` may itself wait in the queue while that reason remains on its active parent. During dequeue, a queued child's `PARENT_HALTED` reason is reevaluated. Explicitly managed or permanent reasons, including `MANUAL_HALT` and `HALTED_BY_PARENT`, continue to block activation until the operation responsible for them removes the reason, if applicable.

An `EXIT` node treats a halted child as stopped, so a parent may complete while a halted child remains represented in the queue. A terminal `WfRun` never activates another queued `ThreadRun`, even if that child's halt reasons are later resolved.

This differs from an active halted child. The current resume path does not reject a terminal `WfRun` or a completed parent, so an active child halted with `MANUAL_HALT` can resume after its parent has completed. That produces an orphaned active `ThreadRun`. The queue guard prevents the equivalent activation for queued children, but the broader `EXIT` and resume lifecycle should be addressed separately.

## Dequeueing a `ThreadRun`

The Server attempts to drain the queue whenever the `WfRun` advances. A queued `ThreadRun` is activated only when:

- The `WfRun` is not terminal.
- There is capacity below `LHS_X_ACTIVE_THREAD_RUNS_PER_WF_RUN`.
- The queued `ThreadRun` has no unresolved halt reasons.

Eligible `ThreadRun`s are processed in queue order. A blocked `ThreadRun` is deferred without preventing later eligible entries from being considered. On activation, the `ThreadRun` is moved into the active collection, its variables are initialized, execution begins at its first node, and its `InactiveThreadRun` record is deleted.

## Concerns

A `WfRun` can stall when every active `ThreadRun` is waiting for work that can only be completed by a queued `ThreadRun`. All active slots remain occupied, so no active `ThreadRun` can complete and be archived to create capacity for the queued `ThreadRun`. The `WfRun` remains `RUNNING`, but it cannot make progress unless an active `ThreadRun` is unblocked by another mechanism, such as an external event.

Increasing `LHS_X_ACTIVE_THREAD_RUNS_PER_WF_RUN` may delay or avoid this condition for a particular workflow, but it does not eliminate the underlying dependency cycle. Workflow authors must avoid designs in which progress by every active `ThreadRun` depends on a `ThreadRun` that may be queued.
