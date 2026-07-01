# Proposal: Thread Run Queue

Author: Jake Rose


This proposal introduces a `threadRun` queue mechanism that allows a `wfRun` to exceed the `maxThreadRunNumber` and avoid run time failures. This implementation is aligned closely with [archived thread run proposal ](012-archive-thread-run.md) extending upon `inactiveThreadRun`.

## Background 

For an in depth background on how and why a `wfRun` is an unbounded object refer to [archived thread run proposal ](012-archive-thread-run.md#background). 

Since a `wfRun` is an unbounded object within the littlehorse server, there can only be a limited number of active `threadRun`s held within a `wfRun` object. Currently the littlehorse server throws an internal error when a `wfRun` attempts to exceed the set number for allowed `threadRun`s. Adding a `threadRun` queue to the little horse server will allow a `wfRun` to spawn more `threadRun`s then (`LHS_X_MAX_THREAD_RUNS_PER_WF_RUN`), while still maintaining a strict capped size for `threadRun`s held on the `wfRun`.

## Extending `InactiveThreadRun`

The `InactiveThreadRun` object currently looks like:

```proto
message InactiveThreadRun {
  ThreadRun thread_run = 1;
}
```

It stores completed/archived `threadRun`s to free space on the `wfRun` object, allowing more `threadRun`s to be spawned. `InactiveThreadRun` should encompass all `threadRun`s not held on the `wfRun`; in order to do this we need to extend the current proto schema while keeping in mind backwards compatibility. 

The new `InactiveThreadRun` schema:

```proto
message InactiveThreadRun {
  ThreadRun thread_run = 1;

  oneof inactive_thread_run_type {
    ArchivedThreadRunInfo archived = 2;
    QueuedThreadRunInfo queued = 3;
  }

}
```

To elaborate on the new schema, every inactive `threadRun` has a `threadRun` object so that remains the same in the new schema. Creating a oneof type allows `inactiveThreadRun` to be extensible, while still maintaining simplicity for the current `threadRun` queue implementation.

Currently `archived` oneof will be an empty object to represent type, but queue will hold input variables needed to start a `threadRun` when being dequeued.

```proto
message ArchivedThreadRunInfo {}

message QueuedThreadRunInfo {
  map<string, VariableValue> input_vars = 1;
}
```

## Extending `wfRun`

Instead of holding every single `threadRun` on the `wfRun` object, we can have a `Queue<Integer>` that holds queued `threadRun` numbers.

```proto
message WfRun {
    // ....
    repeated int32 thread_run_queue = 12;
}
```

## Storing `childThreadId`s
Even though the queued `threadRun` will be considered inactive the parent `threadRun` will still store the `childThreadId`. This will ensure that the parent `threadRun` will not complete while a `threadRun` is in the queue.

A `threadRun` from the queue will only be able to start when the parent `threadRun` has `RUNNING` status. Any other parent status (e.g. `HALTED`/`HALTING`, or `ERROR`/`EXCEPTION` while it could still be rescued) keeps the child parked in the queue.

## Enqueue and Dequeue
A `threadRun` will be queued into the `threadRun` queue when `startThread()` is invoked while the number of in-memory `threadRun`s is greater than or equal to the max `threadRun`s set (`LHS_X_MAX_THREAD_RUNS_PER_WF_RUN`). An enqueue will still increment `greatestThreadRunNumber` allowing `threadRunIterator` to still have access to all `threadRun`s. 

When a `threadRun` is dequeued the parent `threadRun` status will be checked, if the `wfRun` is running the `threadRun` will be started with the proper input vars and the `inactiveThreadRun` will be removed from the core store.

## Concerns 
In the rare case in which every active `threadRun` is blocked waiting on a queued descendant `threadRun`, the `wfRun` will stall. This is specific to parent-child spawn-and-wait (`waitForThreads`): the active threads holding every slot can only be released by a queued descendant running, but that descendant can never be admitted because no slot is free. It should be documented to avoid authoring `wfSpec`s where the spawn-and-wait (`waitForThreads`) nesting depth could approach `LHS_X_MAX_THREAD_RUNS_PER_WF_RUN`. The stall is graceful: the `wfRun` simply makes no further progress.