# Posting `ExternalEvent`s

You can post an `ExternalEvent` using the `PutExternalEvent` rpc call.

## `PutExternalEventRequest`

You need to create a `PutExternalEventRequest` to do so. The protobuf definition is:

```protobuf
message PutExternalEventRequest {
  string wf_run_id = 1;
  string external_event_def_name = 2;
  optional string guid = 3;
  VariableValue content = 5;
  optional int32 thread_run_number = 6;
  optional int32 node_run_position = 7;
}
```

### Required Parameters

The three required values are:

1. `wfRunId` is the ID of the `WfRun` that the `ExternalEvent` is sent to.
2. `externalEventDefName` is the name of the `ExternalEventDef` to send.
3. `content` is the actual payload of the `ExternalEvent`.

### Idempotence

The `guid` parameter is optional, but it is highly recommended to set it for idempotence. As per the [`ExternalEvent` Documentation](/docs/concepts/external-events), an `ExternalEvent` has a composite ID:

1. `wf_run_id`
2. `external_event_def_name`
3. `guid`

If no `guid` is provided, the server generates a random one for you. If you provide a `guid` and an `ExternalEvent` has already been posted with that `guid`, your call has no effect. This is a good strategy for enabling idempotent retries.

### Targeting Specific Threads and Nodes

The `thread_run_number` parameter can be used to send the `ExternalEvent` to a specific `ThreadRun`.

This feature is rarely used; only for advanced use-cases.

## Making the Request

The last thing we need to do to send the request is to set the `content` `VariableValue`.

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs>
  <TabItem value="java" label="Java" default>

In Java, we can use `LHLibUtil#objToVarVal()` to convert an aribitrary Java value or object into a `VariableValue`. You can post an event as follows:

```java
LittleHorseBlockingStub client = ...;

String idempotencyKey = "asdf13hoasdifoje"; // optional

client.PutExternalEvent(PutExternalEventRequest.newBuilder()
        .setExternalEventDefId(ExternalEventDefId.newBuilder().setName("my-external-event-def"))
        .setWfRunId(WfRunId.newBuilder().setId("asdf-1234"))
        .setContent(LHLibUtil.objToVarVal(someObject))
        .setGuid(idempotencyKey) // optional
        .build());
```

  </TabItem>
  <TabItem value="go" label="Go">

In Go, you can use the `common.InterfaceToVarVal()` method to create the `content` parameter.

```go
idempotencyKey := "asdfo2htoijsvlk" // optional

eventContent, _ := common.InterfaceToVarVal("some-interface")
result, _ := (*client).PutExternalEvent(context.Background(), &model.PutExternalEventRequest{
	WfRunId:            &model.WfRunId{Id: "some-wfrun-id"},
	ExternalEventDefId: &model.ExternalEventDefId{Name: "some-external-event-def"},
	Content:            eventContent,
	Guid:               &idempotencyKey, // optional
})
```

  </TabItem>
  <TabItem value="python" label="Python">
In Python, you can use the `littlehorse.to_variable_value()` method to create the `content` parameter.

```python
import littlehorse
from littlehorse.config import LHConfig
from littlehorse.model import *

config = LHConfig()
client = config.stub()

idempotency_key = "asfjok23jtoadfjl" # optional

client.PutExternalEvent(
    PutExternalEventRequest(
        wf_run_id=WfRunId(id="asdf-1234"),
        external_event_def_id=ExternalEventDefId(name="my-external-event-def"),
        content=littlehorse.to_variable_value("my value"),
        guid=idempotency_key, # optional
    )
)
```

  </TabItem>
</Tabs>
