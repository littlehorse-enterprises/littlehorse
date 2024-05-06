# Basics

To develop a `WfSpec` in LittleHorse, you can use the `Workflow` struct or object in our SDK's. Generally, the `Workflow` entity constructor requires two arguments:

1. The name of the `WfSpec` to create.
2. A `ThreadFunc`, which is function pointer, lambda function, or interface of some sort which contains the logic for the Entrypoint `ThreadSpec`.

The `Workflow` object translates your `ThreadFunc` into a `WfSpec`. As per the [Metadata Management Documentation](/docs/developer-guide/grpc/managing-metadata), you can easily deploy a `WfSpec` once you've gotten the `Workflow` object.

The `ThreadFunc` takes in one argument: a `WorkflowThread`. Everything you do goes through the `ThreadFunc`. The `ThreadFunc` defines a `ThreadSpec`, and the `ThreadFunc` passed into the `Workflow` object or struct is used to build the Entrypoint Thread.

## Defining a `WfRunVariable`

A `ThreadSpec` can have `VariableDef`s, which is similar to declaring a variable in programming. When declaring a `Variable` in LittleHorse, you need to:

* Provide the `name` of the `Variable`.
* Specify the `VariableType` or provide a default value from which the type is inferred.

:::note
A `Variable`'s name must be a valid hostname, meaning lowercase alphanumeric characters separated by a `-`.
:::

Recall the valid types of Variables:

- `STR`
- `INT` (64-bit integer, represented as a `long` in Java and `int64` in Go)
- `DOUBLE` (64-bit floating point, `double` in Java and `float64` in Go)
- `BOOL`
- `JSON_OBJ` (a dumped JSON String)
- `JSON_ARR` (a dumped JSON String)
- `BYTES`

### A Note on Indexes

:::warn
The API for indexes on `Variable`s is in the `EXPERIMENTAL` phase, meaning that it can experience breaking changes prior to the `1.0.0` release.
:::

LittleHorse allows you to search for various Execution Objects based on their attributes. For example, you can search `WfRun`'s by their status and creation time. Additionally, you can specify that a `Variable` should be indexed.

Since LittleHorse is a distributed system, indexes are not as simple as in Postgres where everything lives on one index. There are two types of indexes in LittleHorse:

* `REMOTE`, in which the attributes of the index are hashed and sent to a specific partition, offering super-fast lookup.
  * This is ideal for well-distributed indexes, such as the `guid` of a customer.
* `LOCAL`, in which the index is stored on the same partition as the actual `WfRun`.
  * This is ideal for when the index has a small set of possible values and when collisions are likely.

:::warning
If you use a `REMOTE` Index and have millions of objects with a specific value in that index, you will create a hot partition, which is an operational hazard that can cause latency spikes on `WfRun`'s sharing a Server Instance with that index. In extreme cases, it can cause the underlying Kafka cluster to experience operational difficulties.
:::

:::tip
As a general rule of thumb, start with a `LOCAL` index, as that is always safe.

`REMOTE` indexes most often make sense when a `WfRun` is tied to an individual person or identity, as we often want to be able to find workflow runs associated with a specific user, and it is rare for a human to have more than a thousand or so workflows associated with him/herself.
:::

### Persistent Variables

Variable Indexes allow you to search for a `Variable` (and by extension, its `WfRun`) by a certain value. For example, if your `WfSpec` defines a `WfRunVariable` called `email`, you can find the `WfRun` where `email == 'obiwan@jedi.temple'` with a `SearchVariable` rpc call.

However, that requires that you provide the `wfSpecVersion` in your search request. That means you can only search for a `Variable` if you know the version of the `WfSpec` it came from.

Release `0.4.0` introduced the ability to mark a `Variable` as `persistent`, which means that:
* Every future version of the `WfSpec` must have the same variable definition with the same index type.
* You can now search for variables with a certain value across _all versions_ of the `WfSpec`.

### Defining Variables

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs>
  <TabItem value="java" label="Java" default>

```java
public void threadFunction(WorkflowThread thread) {
    WfRunVariable myVar = thread.addVariable("my-variable", VariableTypePb.STR);
}
```

The first argument is the name of the variable; the second is the type. Alternatively, you can pass in a default value to the `Variable`. The following initializes `myVar` to `"Hello, there!"`.

```java
public void threadFunction(WorkflowThread thread) {
    WfRunVariable myVar = thread.addVariable("my-variable", "Hello, there!");
}
```

You can set an index on the variable as follows:

```java
public void threadFunction(WorkflowThread thread) {
    WfRunVariable myVar = thread.addVariable("my-variable", "Hello, there!").withIndex(IndexType.LOCAL_INDEX);
}
```

And you can mark the `Variable` as Persistent as follows:
```java
public void threadFunction(WorkflowThread thread) {
    WfRunVariable myVar = thread.addVariable("my-variable", "Hello, there!")
            .withIndex(IndexType.LOCAL_INDEX)
            .persistent();
}
```

  </TabItem>
  <TabItem value="go" label="Go">

```go
func myThreadFunc(thread *wflib.WorkflowThread) {
    myVar := thread.AddVariable("my-variable", model.VariableTypePb_STR)
}
```

You can add do the same and set a default value for the `Variable` as follows:

```go
func myThreadFunc(thread *wflib.WorkflowThread) {
    nameVar := thread.AddVariableWithDefault("my-variable", model.VariableType_STR, "Ahsoka Tano")
}
```

You can add an index on a `WfRunVariable` to make the variable searchable.
```go
func myThreadFunc(thread *wflib.WorkflowThread) {
    nameVar := thread.AddVariableWithDefault("my-variable", model.VariableType_STR, "Ahsoka Tano")
    nameVar.WithIndex(model.IndexType_REMOTE_INDEX)

    // optionally make it a Persistent variable
    nameVar.Persistent()
}
```

  </TabItem>
  <TabItem value="python" label="Python" default>

```python
def thread_function(thread: WorkflowThread) -> None:
    the_name = thread.add_variable("input-name", VariableType.STR)
```

The first argument is the name of the variable; the second is the type. Alternatively, you can pass in a default value to the `Variable`.

```python
def thread_function(thread: WorkflowThread) -> None:
    the_name = thread.add_variable("input-name", VariableType.STR, "The Mandalorian")
```

You can set an index on the variable as follows:

```python
def thread_function(thread: WorkflowThread) -> None:
    the_name = thread.add_variable("input-name", VariableType.STR)
    the_name.with_index(IndexType.LOCAL_INDEX)

    # optionally make the variable a Persistent variable
    the_name.persistent()
```

  </TabItem>
</Tabs>


## Executing a `TASK` Node

The `WorkflowThread::execute()` method can be used to execute a Task. It is required that the `TaskDef` is already registered with the LH Server, and that you have a Task Worker that is polling for those tasks.

:::info
It is perfectly acceptable for a `WfSpec` written in one language to execute tasks that are defined and run in other languages.
:::
To execute the `foo` task, you simply do the following:

<Tabs>
  <TabItem value="java" label="Java" default>

```java
public void myWf(ThreadFunction thread) {
    NodeOutput output = thread.execute("foo");
}
```

  </TabItem>
  <TabItem value="go" label="Go">

```go
func myThreadFunc(thread *wflib.WorkflowThread) {
    taskOutput := thread.Execute("foo")
}
```

  </TabItem>
    <TabItem value="python" label="Python" default>

```python
def thread_function(thread: WorkflowThread) -> None:
    thread.execute("foo")
```

  </TabItem>
</Tabs>


### Task Input Variables

You can pass input variables to a Task. Let's say, for example, I have a Python Task Function as follows:

```python
async def my_task(some_str: str, some_int: int) -> str:
    return f"Inputs were {some_str} and {some_int}"
```

The resulting `TaskDef` has two input variables, one of type `STR` and another of type `INT`.

You can hard-code the input variables in a call to that `TaskDef` as follows:

<Tabs>
  <TabItem value="java" label="Java" default>

```java
String inputStrVal = "input string value!";
int inputIntVal = 54321;
thread.execute("foo", inputStrVal, inputIntVal);
```

  </TabItem>
  <TabItem value="go" label="Go">

```go
inputStrVal := "input string value!"
inputIntVal := 54321
thread.Execute("foo", inputStrVal, inputIntVal)
```

  </TabItem>
    <TabItem value="python" label="Python" default>

```python
str_val = "input string value!"
int_val = 54321
thread.execute("foo", str_val, int_val)
```

  </TabItem>
</Tabs>


Alternatively, if you have a `WfRunVariable`, you can use it as input:

<Tabs>
  <TabItem value="java" label="Java" default>

```java
public void threadFunction(WorkflowThread thread) {
    WfRunVariable myStr = thread.addVariable("my-str", VariableType.STR);
    WfRunVariable myInt = thread.addVariable("my-int", VariableType.INT);

    thread.execute("foo", myStr, myInt);
}
```

  </TabItem>
  <TabItem value="go" label="Go">

```go
func threadFunction(thread *wflib.WorkflowThread) {
    myStr := thread.AddVariable("my-str", model.VariableType_STR)
    myInt := thread.AddVariable("my-int", model.VariableType_INT)

    thread.Execute("foo", myStr, myInt)
}
```

  </TabItem>
    <TabItem value="python" label="Python">

```python
def thread_function(thread: WorkflowThread) -> None:
    my_str = thread.add_variable("my-str", VariableType.STR)
    my_int = thread.add_variable("my-int", VariableType.INT)
    thread.execute("foo", my_str, my_int)
```

  </TabItem>
</Tabs>

### Setting Retention Hours

You can use the `Workflow::withRetentionHours()` method to set how long a `WfRun` should stay on the system. Remember that our default system hosts `WfRun`s for 168 hours (7 days). For example, if the `WfSpec` has a retention period of 2 hours, a `WfRun` will be deleted 2 hours after it is `COMPLETED` or `ERROR`:

<Tabs>
  <TabItem value="java" label="Java" default>

```java
Workflow wf = new WorkflowImpl(...)
wf.withRetentionHours(23);
wf.register(...);
```

  </TabItem>
  <TabItem value="go" label="Go">

```go
client := ...;
wf := wflib.NewWorkflow(basic.MyWorkflow, "my-workflow")
putWf, _ := wf.Compile()

hours := int32(23) 
putWf.WithRetentionHours(&hours)
resp, err := client.PutWfSpec(putWf)
```

  </TabItem>
    <TabItem value="python" label="Python" default>

```python
wf = Workflow("my-wf", thread_function)
wf.retention_hours = 23
littlehorse.create_workflow_spec(wf, config)
```

  </TabItem>
</Tabs>