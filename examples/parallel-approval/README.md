## Running ParallelApprovalExample

This example uses the "Wait for Threads" feature of LittleHorse to simulate a business process in which:

* Three people must signal approval for some arbitrary transaction, and
* We want to periodically execute some task (eg. a reminder) until all three approvals have been gathered.

Let's run the example in `ParallelApprovalExample.java`.

```
# Register the WfSpec/TaskDefs/ExternalEventDefs and
# also start the Task Worker
./gradlew example-parallel-approval:run
```

In another terminal, use `lhctl` to run the workflow:

```
lhctl run parallel-approval

lhctl get wfRun <id>
```

Note that there are *four* `ThreadRun`s: the Entrypoint (i.e. main) thread, and one child thread for each of the three required approvals.

### Posting Events

Let's first take a look at the main thread. The `currentNodePosition` is `5`. So, we get the `5` `NodeRun` as follows:

```
lhctl get nodeRun <wfRunId> 0 5

{
  "wfRunId":  "a5a9a33b1e1043ecaf2a3db7a16da627",
  "threadRunNumber":  0,
  "position":  5,
  "status":  "RUNNING",
  "arrivalTime":  "2023-08-30T17:58:45.220Z",
  "wfSpecId":  {
    "name":  "parallel-approval",
    "version":  0
  },
  "threadSpecName":  "entrypoint",
  "nodeName":  "5-threads-WAIT_FOR_THREADS",
  "failures":  [],
  "waitThreads":  {
    "threads":  [
      {
        "threadStatus":  "RUNNING",
        "threadRunNumber":  2
      },
      {
        "threadStatus":  "RUNNING",
        "threadRunNumber":  3
      },
      {
        "threadStatus":  "RUNNING",
        "threadRunNumber":  4
      }
    ]
  },
  "failureHandlerIds":  []
}
```

We can see that it's waiting for threads `2`, `3`, and `4`.

Let's look at thread `3` and see what's up. Recall from `lhctl get wfRun` that thread `3` is on node `1`.

```
lhctl get nodeRun <wfRunId> 3 1

{
  "wfRunId":  "a5a9a33b1e1043ecaf2a3db7a16da627",
  "threadRunNumber":  3,
  "position":  1,
  "status":  "RUNNING",
  "arrivalTime":  "2023-08-30T17:58:45.218Z",
  "wfSpecId":  {
    "name":  "parallel-approval",
    "version":  0
  },
  "threadSpecName":  "person-2",
  "nodeName":  "1-person-2-approves-EXTERNAL_EVENT",
  "failures":  [],
  "externalEvent":  {
    "externalEventDefName":  "person-2-approves"
  },
  "failureHandlerIds":  []
}
```

As expected, it's waiting on the `ExternalEvent` with the name `person-2-approves`. Let's send the event:

```
lhctl postEvent <wfRunId> person-2-approves JSON_OBJ '{"approval": true}'
```

Now look at the main thread's `WAIT_FOR_THREAD` node:

```
lhctl get nodeRun <wfRunId> 0 5

{
  "wfRunId":  "a5a9a33b1e1043ecaf2a3db7a16da627",
  "threadRunNumber":  0,
  "position":  5,
  "status":  "RUNNING",
  "arrivalTime":  "2023-08-30T17:58:45.220Z",
  "wfSpecId":  {
    "name":  "parallel-approval",
    "version":  0
  },
  "threadSpecName":  "entrypoint",
  "nodeName":  "5-threads-WAIT_FOR_THREADS",
  "failures":  [],
  "waitThreads":  {
    "threads":  [
      {
        "threadStatus":  "RUNNING",
        "threadRunNumber":  2
      },
      {
        "threadEndTime":  "2023-08-30T18:00:17.820Z",
        "threadStatus":  "COMPLETED",
        "threadRunNumber":  3
      },
      {
        "threadStatus":  "RUNNING",
        "threadRunNumber":  4
      }
    ]
  },
  "failureHandlerIds":  []
}
```

The second thread is completed! Let's finish off the next two threads (quickly).

```
lhctl postEvent <wfRunId> person-1-approves JSON_OBJ '{"approval": true}'
...
lhctl postEvent <wfRunId> person-3-approves JSON_OBJ '{"approval": true}'
...
```

Now the `WAIT_FOR_THREAD` node is completed. If you look at the `all-approved` Variable, you will see that all of the approvals have come in. The Reminder Thread will gracefully wake up and exit rather than send the next reminder. At that point, your `WfRun` will be `COMPLETED`.

```
lhctl get variable <wfRunId> 0 all-approved

{
  "value":  {
    "type":  "BOOL",
    "bool":  true
  },
  "wfRunId":  "a5a9a33b1e1043ecaf2a3db7a16da627",
  "threadRunNumber":  0,
  "name":  "all-approved",
  "date":  "2023-08-30T17:58:45.247Z",
  "wfSpecId":  {
    "name":  "parallel-approval",
    "version":  0
  }
}
```

*NOTE: look at `ParallelApprovalWorkflow#sendReminders()` to see how `all-approved` is used.*

### Handling Failed Approvals

What happens if someone doesn't approve the workflow? We run the exception handler. Search for `HANDLE FAILED APPROVALS HERE` in [the Parallel Approval workflow](./src/main/java/io/littlehorse/examples/ParallelApprovalExample.java) to see how it works.

First, run the workflow:

```
lhctl run parallel-approval
```

Then as before, verify that we're waiting on 3 people:

```
lhctl get wfRun <wf_run_id> 0 5
```

Now, we _make one of the approvals fail_. To do so:

```
lhctl postEvent <wf_run_id> person-1-approves JSON_OBJ '{"approval": false}'
```

Look at the node run. Notice that the approval for thread `1` failed.

```
lhctl get nodeRun <wf_run_id> 0 5
```

Look at the `WfRun`:

```
lhctl get wfRun <wf_run_id>
```

There are two important things to notice:

1. There is now a 6th `ThreadRun`. Look at thread `5` (the last one, since it's zero-indexed). You'll see that it is type `FALIURE_HANDLER`, which means that it's our exception handler thread.
2. The `WfRun` is now in the `EXCEPTION` state, because our Exception Handler thread propagated the failure up the stack.
