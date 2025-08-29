## Running ExternalEventExample

This example demonstrates the asynchronous ExternalEvent functionality.
We will use "thread.waitForEvent" to wait for an external event, then when it arrives
it executes the task "greet".

Let's run the example in `ExternalEventExample.java`

```
./gradlew example-external-event:run
```

In another terminal, use `lhctl` to run the workflow:

```
# Start workflow
lhctl run example-external-event

# Take the resulting workflow ID, and do:
lhctl get wfRun <wf run id>

# Note that it is 'RUNNING'. Next, post an external event using the following:
lhctl postEvent <wf run id> name-event STR Obi-Wan

# Then inspect the wfRun:
# Note it is 'COMPLETED'
lhctl get wfRun <wf run id>

# Then inspect the output of the ExternalEvent and how it completed the nodeRun:
lhctl get nodeRun <wf run id> 0 1
lhctl get taskRun <wf run id> <task runid>
```
