# External Events

:::info
Before you can use an `ExternalEventDef` in a `WfSpec`, you need to create the `ExternalEventDef` metadata object in LittleHorse. You can do that following our [metadata management docs](../09-grpc/05-managing-metadata.md#externaleventdef).
:::

You can use `WorkflowThread#WaitForEvent()` to add an `EXTERNAL_EVENT` Node that causes the `WfRun` to block until an `ExternalEvent` arrives. It's simple:

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs>
  <TabItem value="java" label="Java" default>

```java
public void threadFunction(WorkflowThread thread) {
    NodeOutput eventOutput = thread.waitForEvent("my-event-name");
}
```

  </TabItem>
  <TabItem value="go" label="Go">

```go
func someThreadFunction(thread *littlehorse.WorkflowThread) {
    eventOutput := thread.WaitForEvent("my-event-name")
}
```

  </TabItem>
    <TabItem value="python" label="Python" default>

```python
def thread_function(thread: WorkflowThread) -> None:
    ext_event_output = thread.wait_for_event("my-event-name")
```

  </TabItem>
</Tabs>

### Accessing Event Content

Lastly, just as with `TASK` nodes you can use the `NodeOutput` from `WorkflowThread::waitForEvent()` to mutate a `WfRunVariable`:

<Tabs>
  <TabItem value="java" label="Java" default>

```java
public void threadFunction(WorkflowThread thread) {
    WfRunVariable myVar = thread.addVariable("my-var", VariableType.JSON_OBJ);
    NodeOutput eventOutput = thread.waitForEvent("my-event-name");
    thread.mutate(myVar, VariableMutationType.ASSIGN, eventOutput);
}
```

  </TabItem>
  <TabItem value="go" label="Go">

```go
func someThreadFunction(thread *littlehorse.WorkflowThread) {
    myVar := thread.AddVariable("my-var", lhproto.VariableType_JSON_OBJ)
    eventOutput := thread.WaitForEvent("my-event-name")
    thread.Mutate(myVar, lhproto.VariableMutationType_ASSIGN, eventOutput);
}
```

  </TabItem>
    <TabItem value="python" label="Python" default>

```python
def thread_function(thread: WorkflowThread) -> None:
    myVar = thread.add_variable("my-var", VariableType.JSON_OBJ)
    event_output = thread.wait_for_event("my-event-name")
    thread.mutate(myVar, VariableMutationType.ASSIGN, event_output)
```

  </TabItem>
</Tabs>

:::note
If you use an `ExternalEventDef` for any `EXTERNAL_EVENT` node as shown in this tutorial, you cannot re-use that `ExternalEventDef` as a trigger for [Interrupts](./05-interrupts.md).
:::
