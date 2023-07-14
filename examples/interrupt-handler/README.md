## Running InterruptHandlerExample

This example shows how to interrupt a workflow execution.
You need to register and interrupt handler with thread.registerInterruptHandler.

Let's run the example in `InterruptHandlerExample.java`

```
gradle example-interrupt-handler:run
```

In another terminal, use `lhctl` to run the workflow:

```
# Run a new workflow
lhctl run example-interrupt-handler

# Take the resulting workflow ID, and do:
lhctl get wfRun <wf run id>

# Note that it is 'RUNNING'. Next, post an external event using the following:
lhctl postEvent <wf run id> interruption-event NULL

# Then inspect the wfRun:
# Note it is 'COMPLETED' whit type 'INTERRUPT'
lhctl get wfRun <wf run id>

# Then inspect the output of the ExternalEvent and how it completed the nodeRun:
lhctl get nodeRun <wf run id> 1 1
lhctl get taskRun <wf run id> <task runid>
```
