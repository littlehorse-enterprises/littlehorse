# Await Workflow Events

You can await a `WorkflowEvent` using the `AwaitWorkflowEvent` RPC call.

## `AwaitWorkflowEventRequest`

You need to create an `AwaitWorkflowEventRequest` to do so. The protobuf definition is:

```proto
message AwaitWorkflowEventRequest {
  WfRunId wf_run_id = 1;
  repeated WorkflowEventDefId event_def_ids = 2;
  repeated WorkflowEventId workflow_events_to_ignore = 3;
}
```

### Required Parameters

The three required values are:

1. `wfRunId` is the ID of the `WfRun` that the `WorkflowEvent` is thrown from.
2. `eventDefIds` is a repeated field including the IDs of the `WorkflowEventDef`s you want to await thrown `WorkflowEvent`s for. The request will return the first matching `WorkflowEvent` thrown. If this field is empty, the request will return the first `WorkflowEvent` thrown by the `WfRun`.
3. `workflowEventsToIgnore` is a repeated field of IDs of `WorkflowEvents` that you want to ignore. This gives the client the ability to ignore `WorkflowEvent`s that have already been awaited. See [Ignoring WorkflowEvents](#ignoring-workflowevents) for an example.

## Examples

### Awaiting a `WorkflowEvent`

```java
WorkflowEvent event = client.awaitWorkflowEvent(AwaitWorkflowEventRequest.newBuilder()
            .setWfRunId(WfRunId.newBuilder().setId("your-workflow-run-id"))
            .addEventDefIds(WorkflowEventDefId.newBuilder().setName("my-workflow-event-def"))
            .build());
```

### Awaiting from multiple `WorkflowEventDef`s

You can also await events from multiple `WorkflowEventDef`s. The request will return the first matching `WorkflowEvent` thrown.

```java
WorkflowEvent event = client.awaitWorkflowEvent(AwaitWorkflowEventRequest.newBuilder()
            .setWfRunId(WfRunId.newBuilder().setId("your-workflow-run-id"))
            .addEventDefIds(WorkflowEventDefId.newBuilder().setName("my-workflow-event-def"))
            .addEventDefIds(WorkflowEventDefId.newBuilder().setName("another-workflow-event-def"))
            .build());
```

### Using Deadlines

Upon the execution of a `THROW_EVENT` node, LittleHorse will always ensure that your `WorkflowEvent`s are thrown and returned to any clients awaiting them. However, you may still find it useful to set a [gRPC deadline](https://grpc.io/docs/guides/deadlines/) on your `AwaitWorkflowEvent` request in case a `WorkflowEvent` is not thrown within a specified period of time.

```java
Properties props = getConfigProps();
LHConfig config = new LHConfig(props);
LittleHorseBlockingStub client = config.getBlockingStub();

WorkflowEvent event = client.withDeadlineAfter(1000, TimeUnit.MILLISECONDS)
                .awaitWorkflowEvent(AwaitWorkflowEventRequest.newBuilder()
                        .setWfRunId(WfRunId.newBuilder().setId("your-workflow-run-id"))
                        .build());
```

:::info
You can configure a gRPC deadline for any LittleHorse Client request, not just `AwaitWorkflowEvent`! If the request does not complete within the specified time, it will be automatically canceled and return gRPC Status Code 1 `CANCELLED`. 
:::

### Ignoring `WorkflowEvent`s

Since a single `WfRun` may throw multiple `WorkflowEvent`s with the same `WorkflowEventDefId`, clients have the ability to "ignore" `WorkflowEvent`s that have already been awaited. Any `WorkflowEvent` specified within the `workflowEventsToIgnore` field will be ignored.

```java
// The first WorkflowEvent awaited by the client
WorkflowEvent event1 = client.awaitWorkflowEvent(AwaitWorkflowEventRequest.newBuilder()
            .setWfRunId(WfRunId.newBuilder().setId("your-workflow-run-id"))
            .addEventDefIds(WorkflowEventDefId.newBuilder().setName("my-workflow-event-def"))
            .build());

// The second WorkflowEvent awaited by the client
WorkflowEvent event2 = client.awaitWorkflowEvent(AwaitWorkflowEventRequest.newBuilder()
    .setWfRunId(WfRunId.newBuilder().setId("your-workflow-run-id"))
    .addEventDefIds(WorkflowEventDefId.newBuilder().setName("my-workflow-event-def"))
    // Ignore any WorkflowEvents matching the first one received
    // highlight-next-line
    .addWorkflowEventsToIgnore(event1.getId())
    .build());
```
