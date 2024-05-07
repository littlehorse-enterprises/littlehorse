# Child Threads

You can use `WorkflowThread#spawnThread()` and `WorkflowThread#waitForThreads()` to launch and wait for Child `ThreadRun`s, respectively. This is useful if you want to execute multiple pieces of business logic in parallel.

## Spawning Child Threads

The `WorkflowThread::spawnThread()` method takes in three arguments:

1. A `ThreadFunc` (normally a lambda or function pointer) defining the logic for the Child `ThreadSpec`/`ThreadRun`.
2. The name to assign to the Child `ThreadSpec`.
3. A `Map<String, ?>` for any input variables to the child thread (or equivalent, depending on the language of the SDK you use).

Let's spawn a child thread whose `ThreadSpec` we call `my-child-thread`, and set the variable `child-var` to `foo`.

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs>
  <TabItem value="java" label="Java" default>

```java
SpawnedThread spawnedThread = thread.spawnThread(
    child -> {
        WfRunVariable childVar = child.addVariable("child-var", VariableType.STR);
        child.execute("some-task", childVar);
    },
    "my-child-thread",
    Map.of("child-var", "foo")
);
```

  </TabItem>
  <TabItem value="go" label="Go">

```go
spawnedThread := thread.SpawnThread(
	func (child *wflib.WorkflowThread) {
		childVar := child.AddVariable("child-var", model.VariableType_STR);
		child.Execute("some-task", childVar);
	},
	"my-child-thread",
	map[string]interface{}{"child-var": "foo"},
)
```

  </TabItem>
  <TabItem value="python" label="Python" default>

```python
def my_thread_child(child: WorkflowThread) -> None:
    child_var = child.add_variable("child-var", VariableType.STR)
    child.execute("some-task", child_var)

wf.spawn_thread(my_thread_child, "my-child-thread", {"child-var": "foo"})
```

  </TabItem>
</Tabs>


## Waiting for Child Threads

The `WorkflowThread::waitForThreads()` method takes in a variable number of args. Each arg is the `SpawnedThread` object returned when you launch your Child `ThreadRun` (see above).

:::note
The return type is `NodeOutput`; however, it should be noted that the `NodeOutput` should only be used to set a timeout or handle an exception; there is no content/payload/value associated with `NodeOutput` for `WAIT_FOR_THREAD` Nodes.

Future releases of LittleHorse will allow a child `ThreadRun` to return an output.
:::

<Tabs>
  <TabItem value="java" label="Java" default>

```java
SpawnedThread spawnedThread = thread.spawnThread(...);
SpawnedThread anotherThread = thread.spawnThread(...);
// Omitted: Execute some business logic in parallel

NodeOutput waitedThreads = thread.waitForThreads(spawnedThread, anotherThread);

// You can handle exceptions here
thread.handleException(
    waitedThread,
    null, // catch any failure
    handler -> {
        handler.execute("some-error-reporting-task");
    }
);
```

  </TabItem>
  <TabItem value="go" label="Go">

```go
spawnedThread := ... // see above to spawn thread
anotherThread := ... // see above to spawn thread

// Omitted: Execute some business logic in parallel

waitedThreads := thread.WaitForThreads(spawnedThread, anotherThread)

thread.HandleException(
    waitedThreads,
    nil, // handle any failure
    func (handler *wflib.WorkflowThread) {
        handler.execute("some-error-reporting-task")
    },
)
```
  </TabItem>
  <TabItem value="python" label="Python">

```python
def my_handler(handle: WorkflowThread) -> None:
    handle.execute("some-error-reporting-task")

output = wf.wait_for_threads(SpawnedThreads.from_list(...))
wf.handle_exception(output, my_handler) # handle any failure
```

  </TabItem>
</Tabs>