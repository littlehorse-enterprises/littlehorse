# Proposal: Archiving Inactive `ThreadRun`s

- [Proposal: Archiving Inactive `ThreadRun`s](#proposal-archiving-inactive-threadruns)
  - [Background](#background)
    - [`WfRun` Sizes Are Unbounded](#wfrun-sizes-are-unbounded)
  - [Inactive `ThreadRun`s As A Getable](#inactive-threadruns-as-a-getable)
    - [Proposed Protocol Buffer Changes](#proposed-protocol-buffer-changes)
    - [Control Flow](#control-flow)
    - [Client Changes](#client-changes)
  - [Overview](#overview)

Author: Jacob Snarr

This proposal will introduce a mechanism for archiving Inactive `ThreadRun`s, moving them out of the `WfRun` object and preventing some `WfRun`s from growing too large.

## Background

### `WfRun` Sizes Are Unbounded

In the LittleHorse Server, we store and retrieve messages representing Commands and Storeable data up to the size of ~1MB. When an item exceeds the size of 1MB, many parts of our system start to fail—including but not limited to the latency for retrieving the data and processing commands.

The sizes of some objects stored through LittleHorse are unbounded by nature. For instance, any `repeated` field represents an unbounded list of data — meaning it can be as big as the user wants it to be. That is until it reaches the server—upon which the server's message size limits are enforced and the message might be rejected for being too large.

`WfRun`s are unbounded in size due to the `repeated  ThreadRuns thread_runs` field. As more `ThreadRun`s are spawned within a `WfRun`, the size of the `WfRun` grows and grows, and in extreme scenarios can exceed our 1MB size limit. While there are many areas of the server with `repeated` fields unbounded in size, this case is particularly worrying because `WfRun`s can grow in size autonomously and in the background without a user noticing.

This proposal aims to resolve this issue by moving finished `ThreadRun`s to an `Archived` state.

## Inactive `ThreadRun`s As A Getable

We will "archive" inactive `ThreadRun`s out of their given `WfRun` object and into a separate read-only Getable object.

This will apply to `ThreadRun`s that reach a completed or un-recoverable status, such as `COMPLETED` or `ERROR`. 

The new getable will be named `InactiveThreadRun`, representing any `ThreadRun` that is not being executed within a `WfRun`.

### Proposed Protocol Buffer Changes

Add new message:
```proto
// A read-only object representing a ThreadRun that is not being executed within a WfRun
message InactiveThreadRun {
  ThreadRun thread_run = 0;

  InactiveThreadRunType type = 1;

  // The type of the InactiveThreadRun.
  // Can be expanded later to include 'Scheduled' or 'Pending' ThreadRuns
  enum InactiveThreadRunType {
    UNSPECIFIED = 0;
    ARCHIVED = 1;
  }
}
```

Add new object id message:
```proto
// ID for an InactiveThreadRun
message InactiveThreadRunId {
  WfRunId wf_run_id = 0;
  int32 thread_run_number = 1;
}
```

Add new service:
```proto
rpc GetInactiveThreadRun(InactiveThreadRunId) returns (InactiveThreadRun) {};
```

### Control Flow

At the end of the `advance()` stage of a `WfRun`, we will iterate over all of the "active" `ThreadRun`s and remove any `ThreadRun`s that have a completed or unrecoverable status. Then, we will put these once-active `ThreadRun`s inside of their own respective `InactiveThreadRun` objects and store them into the `GetableManager`.

The pseudocode would look something like so:

```java
ArrayList<ThreadRunModel> activeThreadRuns = new ArrayList<>(this.threadRuns);
for (int = this.threadRuns.size(); i > 0; i--) {
  ThreadRunModel threadRun = this.threadRuns.get(i);

  if (parent.getType() == ThreadType.ENTRYPOINT) continue;

  switch (threadRun.getStatus()) {
    case COMPLETED:
    case ERROR:
      activeThreadRuns.remove(i);
      InactiveThreadRunModel inactiveThreadRun = new InactiveThreadRunModel(threadRun);
      getableManager.put(inactiveThreadRun);
      break;
    default:
  }
}
this.threadRuns = activeThreadRuns;
```

We create a copy of our list of active `ThreadRun`s, remove each inactive `ThreadRun` from from the new list, put the inactive `ThreadRun`s into the `GetableManager`, and then replace the old list of active `ThreadRun`s with the updated list of active `ThreadRun`s.

### Client Changes

Client that work with `ThreadRun`s will need to adjust their code to support getting `InactiveThreadRun`s using the new `RPC GetInactiveThreadRun` service call.

The updated flow would involve performing a `GetInactiveThreadRun` call for any `ThreadRun` from index `0` to `greatest_threadrun_number` that isn't inside the `WfRun` object returned by the server.

## Overview

With the introduction of `InactiveThreadRun`s, we will resolve the issue of `WfRun`s growing too large whilst containing unused or inactive `ThreadRun` data. We will move all inactive `ThreadRun`s to a separate read-only getable that can be retrieved using only the `WfRunId` and the `thread_run_number`. 

This proposal will not solve every use case where `WfRun`s grow too large -- for example, if there are many active `ThreadRun`s then the `WfRun` will still grow in size as we cannot interfere with active workflow data.

Lastly, this proposal was designed with `Pending` or `Scheduled` `ThreadRun`s in mind. This supports the idea that one day we may add a `ThreadRun` pool mechanism that stores pending `ThreadRun`s in this `InactiveThreadRun` getable until there is space within a `WfRun` object to make the pending `ThreadRun` active. Think of this as the opposite of the `ThreadRun` archive mechanism.