# Await Workflow Events

You can await a `WorkflowEvent` using the `AwaitWorkflowEvent` RPC call.

## `AwaitWorkflowEventRequest`

You need to create an `AwaitWorkflowEventRequest` to do so. The protobuf definition is:

```protobuf
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

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem'; 

### Awaiting a `WorkflowEvent`

<Tabs>
  <TabItem value="java" label="Java" default>

```java
WorkflowEvent event = client.awaitWorkflowEvent(AwaitWorkflowEventRequest.newBuilder()
            .setWfRunId(WfRunId.newBuilder().setId("your-workflow-run-id"))
            .addEventDefIds(WorkflowEventDefId.newBuilder().setName("my-workflow-event-def"))
            .build());
```
  </TabItem>
  <TabItem value="python" label="Python" default>

```python
async def main() -> None:
    config = get_config()
    client = config.stub()

    await_workflow_event_request = AwaitWorkflowEventRequest(
        wf_run_id=WfRunId(id="your-wf-run-id"),
        event_def_ids=[WorkflowEventDefId(name="my-workflow-event-def")],
        workflow_events_to_ignore=None)

    client.AwaitWorkflowEvent(await_workflow_event_request)
```
  </TabItem>
  <TabItem value="go" label="Go" default>

```go
config := littlehorse.NewConfigFromEnv()
client, err := config.GetGrpcClient()

event, err := (*client).AwaitWorkflowEvent(context.Background(),
    &lhproto.AwaitWorkflowEventRequest{
      WfRunId: &lhproto.WfRunId{
        Id: "your-workflow-run-id",
      },
      EventDefIds: []*lhproto.WorkflowEventDefId {
        &lhproto.WorkflowEventDefId{
          Name: "my-workflow-event-def",
        },
      }
    },
)
```

  </TabItem>
</Tabs>

<hr/>

### Awaiting Events from Multiple `WorkflowEventDef`s

In the event your workflow throws multiple types of `WorkflowEvent`s, you can specify multiple `WorkflowEventDef`s in your request. The request will return the first matching `WorkflowEvent` thrown by your workflow.

<Tabs>
  <TabItem value="java" label="Java" default>

```java
WorkflowEvent event = client.awaitWorkflowEvent(AwaitWorkflowEventRequest.newBuilder()
            .setWfRunId(WfRunId.newBuilder().setId("your-workflow-run-id"))
            // highlight-start
            .addEventDefIds(WorkflowEventDefId.newBuilder().setName("my-workflow-event-def"))
            .addEventDefIds(WorkflowEventDefId.newBuilder().setName("another-workflow-event-def"))
            // highlight-end
            .build());
```

  </TabItem>
  <TabItem value="python" label="Python" default>

```python
async def main() -> None:
    config = get_config()
    client = config.stub()

    await_workflow_event_request = AwaitWorkflowEventRequest(
        wf_run_id=WfRunId(id="your-wf-run-id"),
        # highlight-start
        event_def_ids=[WorkflowEventDefId(name="my-workflow-event-def"),
                       WorkflowEventDefId(name="another-workflow-event-def")],
        # highlight-end
        workflow_events_to_ignore=None)

    event: WorkflowEvent = client.AwaitWorkflowEvent(await_workflow_event_request)
```

  </TabItem>
  <TabItem value="go" label="Go" default>

```go
config := littlehorse.NewConfigFromEnv()
client, err := config.GetGrpcClient()

event, err := (*client).AwaitWorkflowEvent(context.Background(),
    &lhproto.AwaitWorkflowEventRequest{
      WfRunId: &lhproto.WfRunId{
        Id: "your-workflow-run-id",
      },
      EventDefIds: []*lhproto.WorkflowEventDefId {
        // highlight-start
        &lhproto.WorkflowEventDefId{
          Name: "my-workflow-event-def",
        },
        &lhproto.WorkflowEventDefId{
          Name: "another-workflow-event-def",
        },
        // highlight-end
      }
    },
)
```

  </TabItem>
</Tabs>

<hr/>

### Using Deadlines

Upon the execution of a `THROW_EVENT` node, LittleHorse will always ensure that your `WorkflowEvent`s are thrown and returned to any clients awaiting them. However, you may still find it useful to set a [gRPC deadline](https://grpc.io/docs/guides/deadlines/) on your `AwaitWorkflowEvent` request in case a `WorkflowEvent` is not thrown within a specified period of time.

:::info
You can configure a gRPC deadline for any LittleHorse Client request, not just `AwaitWorkflowEvent`! If the request does not complete within the specified time, it will be automatically canceled and return gRPC Status Code 1 `CANCELLED`. 
:::

<Tabs>
  <TabItem value="java" label="Java" default>

To use deadlines with our Java SDK, you can call the `LittleHorseBlockingStub#withDeadlineAfter()` method before your gRPC request method call.

```java
Properties props = getConfigProps();
LHConfig config = new LHConfig(props);
LittleHorseBlockingStub client = config.getBlockingStub();

// highlight-next-line
WorkflowEvent event = client.withDeadlineAfter(1000, TimeUnit.MILLISECONDS)
                .awaitWorkflowEvent(AwaitWorkflowEventRequest.newBuilder()
                        .setWfRunId(WfRunId.newBuilder().setId("your-workflow-run-id"))
                        .build());
```

**Parameters**

The `LittleHorseBlockingStub#withDeadlineAfter()` method takes two parameters:
1. `long duration`: the duration to await the request
2. `TimeUnit unit`: the unit of time for the duration value

  </TabItem>
  <TabItem value="python" label="Python" default>

To use deadlines with our Python SDK, you can use the `timeout` parameter in any gRPC request method call.

```python
async def main() -> None:
    config = get_config()
    client = config.stub()

    await_workflow_event_request = AwaitWorkflowEventRequest(
        wf_run_id=WfRunId(id="your-wf-run-id"),
        event_def_ids=[WorkflowEventDefId(name="my-workflow-event-def")],
        workflow_events_to_ignore=None)

    # highlight-next-line
    event: WorkflowEvent = client.AwaitWorkflowEvent(await_workflow_event_request, timeout=1)
```

**Parameters**

Every `LittleHorseStub` request method features an `int timeout` parameter which represents the amount of time in `seconds` that the client will wait before cancelling your request.

  </TabItem>
  <TabItem value="go" label="Go" default>

To use deadlines in our Go SDK, you can use the `context` library's `withTimeout()` method to wrap your context with a fixed deadline.

```go
config := littlehorse.NewConfigFromEnv()
client, err := config.GetGrpcClient()

// highlight-start
contextWithTimeout, cancel := context.WithTimeout(context.Background(), time.Millisecond*1000)
defer cancel() // Ensure that cancel is called to release resources
// highlight-end

event, err := (*client).AwaitWorkflowEvent(contextWithTimeout,
  &lhproto.AwaitWorkflowEventRequest{
    WfRunId: &lhproto.WfRunId{
      Id: "your-workflow-run-id",
    },
    EventDefIds: []*lhproto.WorkflowEventDefId {
      &lhproto.WorkflowEventDefId{
        Name: "my-workflow-event-def",
      },
    }
  },
)
```

**Parameters**

The `Context.WithTimeout()` method takes two parameters:
- `parent Context`: the context of your request, usually `context.Background()`
- `timeout time.Duration`: the unit and duration of time for your deadline

[Context.WithTimeout() Documentation](https://pkg.go.dev/context#WithTimeout)

  </TabItem>
</Tabs>

<hr/>

### Ignoring `WorkflowEvent`s

Since a single `WfRun` may throw multiple `WorkflowEvent`s with the same `WorkflowEventDefId`, clients have the ability to "ignore" `WorkflowEvent`s that have already been awaited. Any `WorkflowEvent` specified within the `workflowEventsToIgnore` field will be ignored.

<Tabs>
  <TabItem value="java" label="Java" default>

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
  </TabItem>
  <TabItem value="python" label="Python" default>

```python
async def main() -> None:
    config = get_config()
    client = config.stub()

    await_workflow_event_request = AwaitWorkflowEventRequest(
        wf_run_id=WfRunId(id="your-wf-run-id"),
        event_def_ids=[WorkflowEventDefId(name="my-workflow-event-def")],
        workflow_events_to_ignore=None)
    event1: WorkflowEvent = client.AwaitWorkflowEvent(await_workflow_event_request)

    await_workflow_event_request_2 = AwaitWorkflowEventRequest(
        wf_run_id=WfRunId(id="your-wf-run-id"),
        event_def_ids=[WorkflowEventDefId(name="my-workflow-event-def")],
        # Ignore any WorkflowEvents matching the first one received
        # highlight-next-line
        workflow_events_to_ignore=[event1.id])
    
    event2: WorkflowEvent = client.AwaitWorkflowEvent(await_workflow_event_request_2)
```
  </TabItem>
  <TabItem value="go" label="Go" default>

```go
config := littlehorse.NewConfigFromEnv()
client, err := config.GetGrpcClient()

event1, err := (*client).AwaitWorkflowEvent(context.Background(),
  &lhproto.AwaitWorkflowEventRequest{
    WfRunId: &lhproto.WfRunId{
      Id: "your-workflow-run-id",
    },
    EventDefIds: []*lhproto.WorkflowEventDefId{
      &lhproto.WorkflowEventDefId{
        Name: "my-workflow-event-def",
      },
    },
  },
)

event2, err := (*client).AwaitWorkflowEvent(context.Background(),
  &lhproto.AwaitWorkflowEventRequest{
    WfRunId: &lhproto.WfRunId{
      Id: "your-workflow-run-id",
    },
    EventDefIds: []*lhproto.WorkflowEventDefId{
      &lhproto.WorkflowEventDefId{
        Name: "my-workflow-event-def",
      },
    },
    // highlight-start
    WorkflowEventsToIgnore: []*lhproto.WorkflowEventId{
      event1.Id,
    },
    // highlight-end
  },
)
```
  </TabItem>
</Tabs>

