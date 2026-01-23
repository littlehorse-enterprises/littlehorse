# Wait For One Of Many Threads

This proposal extends the functionality of the `WaitForThreadsNode` to allow users to wait for child `ThreadRun`s with different strategies.

Currently, the `WaitForThreadsNode` behaves such that:

* If any of the child `ThreadRun`s fail, the parent `NodeRun` fails (the `EXCEPTION` propagates upwards, or `ERROR` turns into `CHILD_FAILURE`). The other child `ThreadRuns`s are `HALTED`.
* The `NodeRun` blocks until all waited-for child `ThreadRun`s have `COMPLETED`.

We will extend it to allow a new behavior such that, if specified, the `NodeRun` is marked as completed as soon as one of the child `ThreadRun`s is completed.

## Motivation

Several users have wished to wait for one of several different child `ThreadRun`s to complete, and stop waiting for others to complete. Some use-cases include:

* Handling approvals where only one person must sign off out of several.
* Dealing with competing "race-style" processes.
* Implementing the BPMN-style "Any Of" pattern.

## SDK

The current behavior will be un-changed:

```java
SpawnedThread childThread1 = wf.spawnThread(child -> {
    child.waitForEvent("some-event");
}, "child-1", Map.of());

SpawnedThread childThread2 = wf.spawnThread(child -> {
    child.waitForEvent("some-event");
}, "child-1", Map.of());

wf.waitForThreads(SpawnedThreads.of(childThread1, childThread2));
```

We will add the following capability:

```java
SpawnedThread childThread1 = wf.spawnThread(child -> {
    child.waitForEvent("some-event");
}, "child-1", Map.of());

SpawnedThread childThread2 = wf.spawnThread(child -> {
    child.waitForEvent("different-event");
}, "child-2", Map.of());

wf.waitForAnyOf(SpawnedThreads.of(childThread1, childThread2));

wf.execute("something");
```

As soon as either `child-1` or `child-2` finish, the `WfRun` will move on and halt the other child `ThreadRun` which did not finish.

## Protobuf

We will introduce another `oneof` into the `WaitForThreadsNode`:

```proto
message WaitForThreadsNode {
  // ...

  // By default, the `WaitForThreadsNode` only completes once all of the specified child
  // `ThreadRun`s have completed. If none of the following in the `oneof` are specified, then
  // the default strategy is used.
  oneof advanced_waiting_strategy {
    // This strategy causes the `NodeRun` to complete as soon as any of the child `ThreadRun`s
    // have completed. The remaining children are `HALTED`.
    google.protobuf.Empty complete_after_any = 4;

    // TODO: add more entries to the `oneof` when more use-cases arise.
  }
}
```

## Compatibility

This proposal does not deprecate nor alter any existing functionality, syntax, nor protobuf structures.

## Future Work

We can also extend the `ExternalEventNode` to allow waiting for one of multiple different `ExternalEventDef`s. Doing so would provide a more elegant solution for a more limited problem.

The proposal to allow completing the `WaitForThreadsNode` upon receiving only a single completed child `ThreadRun` is a more flexible and extensible proposal which covers a wider range of use-cases but with a slightly less ergonomic SDK experience.
