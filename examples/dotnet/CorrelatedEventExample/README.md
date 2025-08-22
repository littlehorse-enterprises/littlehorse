## Running CorrelatedEventExample

This example demonstrates functionality of Correlated Events. Correlated Events are just external events outfitted with a unique Correlation ID. Users can post an external event by referencing the Correlation ID instead of the Workflow Run ID. To declare a Correlated Event we use "thread.WaitForEvent.WithCorrelationId("correlationId")" to wait for an external event that has been given a Correlation ID.

Let's run the example in `CorrelatedEventExample`

```
dotnet build
dotnet run
```

In another terminal, use `lhctl` to run the workflow:

```
# Start workflow
lhctl run example-correlated-event document-id my-document

# Take the resulting workflow ID, and do:
lhctl get wfRun <wf run id>

# Note that it is 'RUNNING'. Next, notice that to post an external event, we need the workflow run id:
lhctl postEvent <wf run id> document-signed BOOL true

#Instead, we will post a correlated event using the unique correlation key instead of the workflow run id:
lhctl put correlatedEvent my-document document-signed BOOL true

# Then inspect the wfRun:
# Note it is 'COMPLETED'
lhctl get wfRun <wf run id>

#Note that we can still use the external event functionality to post these events. Lets run another example, :
lhctl run example-correlated-event document-id some-other-document
lhctl postEvent <wf run id> document-signed BOOL true

# Then inspect the wfRun:
# Note it is 'COMPLETED'
lhctl get wfRun <wf run id>
```
