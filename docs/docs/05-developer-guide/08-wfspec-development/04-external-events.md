# External Events

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
func someThreadFunction(thread *wflib.WorkflowThread) {
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
func someThreadFunction(thread *wflib.WorkflowThread) {
    myVar := thread.AddVariable("my-var", model.VariableType_JSON_OBJ)
    eventOutput := thread.WaitForEvent("my-event-name")
    thread.Mutate(myVar, model.VariableMutationType_ASSIGN, eventOutput);
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
