# Throwing Workflow Events

:::info
Before you can add a `THROW_EVENT` Node to a `WfSpec`, you need to create a `WorkflowEventDef` metadata object in LittleHorse representing the event you want to throw. You can do that following our [metadata management docs](../../09-grpc/05-managing-metadata.md#workfloweventdef).
:::

To throw a WorkflowEvent in LittleHorse, you must define a `THROW_EVENT` Node in a `WfSpec`.

## Defining a `THROW_EVENT` Node

The following code defines a workflow that waits 5 seconds before throwing a `WorkflowEvent` named "my-event", with the content "Hello There!".

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem'; 

<Tabs>
  <TabItem value="java" label="Java" default>
```java
public static Workflow getWorkflow() {
    return Workflow.newWorkflow(
        "throw-wf-event",
        wf -> {
            wf.sleepSeconds(5);
            wf.throwEvent("my-event", "Hello There!");
        });
}
```
  </TabItem>
  <TabItem value="python" label="Python">
```python
def get_workflow() -> Workflow:
    def my_entrypoint(wf: WorkflowThread) -> None:
        wf.sleep(5)
        wf.throwEvent("sleep-done", "Hello There!")

    return Workflow("throw-wf-event", my_entrypoint)
```
  </TabItem>
  <TabItem value="go" label="Go">

```go
func ThrowWfEvent(wf *littlehorse.WorkflowThread) {
  wf.Sleep(5)
  wf.ThrowEvent("my-event", "Hello there!")
}
```
  </TabItem>
</Tabs>

### Required Parameters

The two required values are:

1. `workflowEventDefName` is the Name of the `WorkflowEventDef` that you want to throw a `WorkflowEvent` for.
2. `content` is the content of the `WorkflowEvent` that will be stored in the `content` field of the `WorkflowEvent`. Content type will match the `VariableType` specified in your `WorkflowEventDef`.
