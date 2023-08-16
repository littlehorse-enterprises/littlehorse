## Running ParallelApprovalExample

This example uses the "Wait for Threads" feature of LittleHorse to simulate a business process in which:

* Three people must signal approval for some arbitrary transaction, and
* We want to periodically execute some task (eg. a reminder) until all three approvals have been gathered.

Let's run the example in `ParallelApprovalExample.java`.

```
# Register the WfSpec/TaskDefs/ExternalEventDefs and
# also start the Task Worker
gradle example-parallel-approval:run
```

In another terminal, use `lhctl` to run the workflow:

```
lhctl run parallel-approval

lhctl get wfRunModel <id>
```

Note that there are *four* `ThreadRun`s: the Entrypoint (i.e. main) thread, and one child thread for each of the three required approvals.

### Posting Events

Let's first take a look at the main thread. The `currentNodePosition` is `5`. So, we get the `5` `NodeRun` as follows:

```
-> lhctl get nodeRunModel <wfRunId> 0 5

{
  "code":  "OK",
  "result":  {
    "wfRunId":  "ad4d7eaedc5140f2af462d9c4a0f7cac",
    "threadRunNumber":  0,
    "position":  5,
    "status":  "RUNNING",
    "arrivalTime":  "2023-07-18T00:12:08.627Z",
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
}
```

We can see that it's waiting for threads `2`, `3`, and `4`.

Let's look at thread `3` and see what's up. Recall from `lhctl get wfRunModel` that thread `3` is on node `1`.

```
-> lhctl get nodeRunModel <wfRunId> 3 1
{
  "code":  "OK",
  "result":  {
    "wfRunId":  "ad4d7eaedc5140f2af462d9c4a0f7cac",
    "threadRunNumber":  3,
    "position":  1,
    "status":  "RUNNING",
    "arrivalTime":  "2023-07-18T00:12:08.627Z",
    "wfSpecId":  {
      "name":  "example-parallel-approval",
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
}
```

As expected, it's waiting on the `ExternalEvent` with the name `person-2-approves`. Let's send the event:

```
lhctl postEvent <wfRunId> person-2-approves NULL
```

*NOTE: most External Event's have a payload; however, in this example, we do not send a payload, so the value is `NULL`*

Now look at the main thread's `WAIT_FOR_THREAD` node:

```
-> lhctl get nodeRunModel <wfRunId> 0 5
{
  "code":  "OK",
  "result":  {
    "wfRunId":  "ad4d7eaedc5140f2af462d9c4a0f7cac",
    "threadRunNumber":  0,
    "position":  5,
    "status":  "RUNNING",
    "arrivalTime":  "2023-07-18T00:12:08.627Z",
    "wfSpecId":  {
      "name":  "example-parallel-approval",
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
          "threadEndTime":  "2023-07-18T05:24:10.714Z",
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
}
```

The second thread is completed! Let's finish off the next two threads (quickly).

```
-> lhctl postEvent <wfRunId> person-1-approves NULL
...
-> lhctl postEvent <wfRunId> person-3-approves NULL
...
```

Now the `WAIT_FOR_THREAD` node is completed. If you look at the `all-approved` Variable, you will see that all of the approvals have come in. The Reminder Thread will gracefully wake up and exit rather than send the next reminder. At that point, your `WfRun` will be `COMPLETED`.

```
-> lhctl get variable <wfRunId> 0 all-approved
{
  "code":  "OK",
  "result":  {
    "value":  {
      "type":  "BOOL",
      "bool":  true
    },
    "wfRunId":  "ad4d7eaedc5140f2af462d9c4a0f7cac",
    "threadRunNumber":  0,
    "name":  "all-approved",
    "date":  "2023-07-18T05:26:28.521Z"
  }
}
```

*NOTE: look at `ParallelApprovalWorkflow#sendReminders()` to see how `all-approved` is used.*
