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

### Current Behavior

The current behavior will be un-changed:

```java
SpawnedThread childThread1 = wf.spawnThread(child -> {
    child.waitForEvent("some-event");
}, "child-1", Map.of());

SpawnedThread childThread2 = wf.spawnThread(child -> {
    child.waitForEvent("some-event");
}, "child-2", Map.of());

wf.waitForThreads(SpawnedThreads.of(childThread1, childThread2));
```

### Wait For First

We will add the following capability:

```java
SpawnedThread childThread1 = wf.spawnThread(child -> {
    child.waitForEvent("some-event");
}, "child-1", Map.of());

SpawnedThread childThread2 = wf.spawnThread(child -> {
    child.waitForEvent("different-event");
}, "child-2", Map.of());

wf.waitForFirstOf(SpawnedThreads.of(childThread1, childThread2));

wf.execute("something");
```

As soon as either `child-1` or `child-2` finish, the `WfRun` will move on and halt the other child `ThreadRun` which did not finish. If `child-1` fails while `child-2` is still `RUNNING`, the `NodeRun` will still fail and `child-2` is `HALTED`.

### Wait for Any

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

This time, if one thread fails before the other finishes, the `NodeRun` will patiently wait until the other `ThreadRun` finishes. If both fail, then the failure thrown upwards is always the `CHILD_FAILURE` technical `ERROR`.

## Protobuf

We will introduce another `oneof` into the `WaitForThreadsNode`:

```proto
enum WaitForThreadsStrategy {
  // By default, the `WaitForThreadsNode` only completes once all of the specified child
  // `ThreadRun`s have completed.
  WAIT_FOR_ALL = 0;

  // This strategy causes the `WfRun` to wait for the first of the child `ThreadRun`s to
  // fail or complete. If the first finisher is `COMPLETED`, then the `NodeRun` completes
  // and the other threads are `HALTED`.
  //
  // If the first child to terminate fails, then the failure propagates to the `NodeRun`
  // and the other threads are `HALTED`.
  WAIT_FOR_FIRST = 1;

  // This strategy waits for the any of the child `ThreadRun`s to complete, ignoring the
  // failures of prior children. If all children fail, then the `CHILD_FAILURE` error is
  // thrown upwards.
  //
  // Once any of the children reaches `COMPLETED`, the others are moved to `HALTED`.
  WAIT_FOR_ANY = 2;
}

message WaitForThreadsNode {
  // ...

  // Determines the strategy to wait for different threads in parallel.
  WaitForThreadStrategy strategy = 4;
}
```

We also have to introduce a new halt reason to say that a `NodeRun` in one of the parent threads decided to halt a child:

```proto
// A Halt Reason denoting that a specific NodeRun in a parent ThreadRun caused this ThreadRun
// to halt. Could be the parent, the parent's parent, or so on.
message HaltedByParentNodeHaltReason {
  // The ThreadRun number of the NodeRun which caused the halt.
  int32 parent_thread_run_number = 1;

  // The specific node run position which caused the halt.
  int32 waiting_node_run_position = 2;
}
```

## Compatibility

This proposal does not deprecate nor alter any existing functionality, syntax, nor protobuf structures.

## Future Work

We can also extend the `ExternalEventNode` to allow waiting for one of multiple different `ExternalEventDef`s. Doing so would provide a more elegant solution for a more limited problem.

The proposal to allow completing the `WaitForThreadsNode` upon receiving only a single completed child `ThreadRun` is a more flexible and extensible proposal which covers a wider range of use-cases but with a slightly less ergonomic SDK experience.
