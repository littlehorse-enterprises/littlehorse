## Running a Interrupt Handler

This example shows how to interrupt a workflow execution.
You need to register an external event at LH Server.

In this example the parent thread sleeps 30 sec, we can interrupt it while it's sleeping,
and it creates a child thread. When an interruption is received the workflow executes the task:
"some-task", if the task fails the whole workflow fails.

In order for the Interrupt to happen, the parent thread needs to be sleeping.

Let's run the example:

```
poetry shell
python -m example_interruption
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
# Note that the threadRuns number 1 is 'ERROR' with type 'INTERRUPT'
lhctl get wfRun <wf run id>

# Then you can inspect the output of the ExternalEvent:
lhctl get nodeRun <wf run id> 1 1
lhctl get taskRun <wf run id> <task runid>
```
