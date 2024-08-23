# Mutating Variables

Recall from the [Concepts Documentation](../../04-concepts/01-workflows.md#variables) that every `Node` can have zero or more `VariableMutation`s on it. A `VariableMutation` changes the value of a `ThreadRun`'s `Variable`s.

You can add a `VariableMutation` at any point in your Thread Function by using tghe `WorkflowThread#mutate()` method or function.

## Basic Structure

The `WorkflowThread::Mutate()` functions/methods take three arguments:

1. A `WfRunVariable` which is the LHS to mutate.
2. A `VariableMutationType` which specifies which mutation to execute.
3. A `WfRunVariable`, `NodeOutput`, Object/interface/struct, or primitive type to serve as the RHS for the mutation.

The valid Mutation Types come from the `VariableMutationType` enum and are:

- `ASSIGN`
- `ADD`
- `SUBTRACT`
- `DIVIDE`
- `MULTIPLY`
- `EXTEND`
- `REMOVE_IF_PRESENT`
- `REMOVE_KEY`
- `REMOVE_INDEX`

A description of each `VariableType` can be found on the [protobuf documentation](../../08-api.md#variabletype).

## Examples

Here are some examples of mutating variables inside a `WfSpec`.

### Hard-Coded Literal Value

Let's assign our variable `foo` to the hard-coded value of `3`.

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs>
  <TabItem value="java" label="Java" default>

```java
public void threadFunction(WorkflowThread thread) {
    WfRunVariable foo = thread.addVariable("foo", VariableType.INT);
    // ... optionally execute some tasks
    thread.mutate(foo, VariableMutationType.ASSIGN, 3);
}
```
  </TabItem>
  <TabItem value="go" label="Go">

```go
func someThreadFunction(thread *littlehorse.WorkflowThread) {
    foo := thread.AddVariable("foo", lhproto.VariableType_INT)
    // ... optionally execute some tasks
    thread.Mutate(foo, lhproto.VariableMutationType_ASSIGN, 3)
}
```

  </TabItem>
    <TabItem value="python" label="Python" default>

```python
def thread_function(thread: WorkflowThread) -> None:
    foo = thread.add_variable("foo", VariableType.INT)
    # ... optionally execute some tasks
    thread.mutate(foo, VariableMutationType.ASSIGN, 3)
```
  </TabItem>
</Tabs>

### Using a `NodeOutput`

Let's say we have a `TaskDef` which returns an `INT` value, and we want to add that value to our `WfRunVariable`. To do that, we use the `NodeOutput` as the `RHS`.

This is analogous to the following pseudocode.

```
int myInt = 1;
myInt += doTask1();
```

<Tabs>
  <TabItem value="java" label="Java" default>

```java
public void threadFunction(WorkflowThread thread) {
    WfRunVariable foo = thread.addVariable("foo", 3);
    NodeOutput intOutput = thread.execute("some-task-that-returns-int");
    thread.mutate(foo, VariableMutationType.ADD, intOutput);
}
```
  </TabItem>
  <TabItem value="go" label="Go">

```go
func someThreadFunction(thread *littlehorse.WorkflowThread) {
    foo := thread.AddVariableWithDefault("foo", lhproto.VariableType_INT, 1)
    taskOutput := thread.Execute("some-task-that-returns-int")
    thread.Mutate(foo, lhproto.VariableMutationType_ADD, taskOutput)
}
```
  </TabItem>
    <TabItem value="python" label="Python" default>

```python
def thread_function(thread: WorkflowThread) -> None:
    foo = thread.add_variable("foo", VariableType.INT, 3)
    int_output = thread.execute("some-task-that-returns-int")
    thread.mutate(foo, VariableMutationType.ADD, int_output)
```
  </TabItem>
</Tabs>


### Using other `WfRunVariables`

We can also use another `WfRunVariable` as the `RHS`. For example, if our `LHS` is a `JSON_ARR`, we append a `STR` variable to it as follows:

<Tabs>
  <TabItem value="java" label="Java" default>

```java
public void threadFunction(WorkflowThread thread) {
    WfRunVariable strToAppend = thread.addVariable("string-to-append", VariableType.STR);
    WfRunVariable myList = thread.addVariable("my-list", VariableType.JSON_ARR);

    // ... execute some tasks

    thread.mutate(myList, VariableMutationType.EXTEND, strToAppend);
}
```

  </TabItem>
  <TabItem value="go" label="Go">

```go
func someThreadFunction(thread *littlehorse.WorkflowThread) {
    strToAppend := thread.AddVariable("string-to-append", lhproto.VariableType_STR)
    myList := thread.AddVariable("my-list", lhproto.VariableType_JSON_ARR)

    // ... execute a few tasks

    thread.Mutate(myList, lhproto.VariableMutationType_EXTEND, strToAppend)
}
```

  </TabItem>
    <TabItem value="python" label="Python" default>

```python
def thread_function(thread: WorkflowThread) -> None:
    str_to_append = thread.add_variable("string-to-append", VariableType.STR)
    my_list = thread.add_variable("my-list", VariableType.JSON_ARR)

    # ... execute a few tasks

    thread.mutate(my_list, VariableMutationType.EXTEND, str_to_append)
```

  </TabItem>
</Tabs>

## Using JsonPath

Both `NodeOutput` and `WfRunVariable` have a `#jsonPath()` method.

If your `LHS` variable is of type `JSON_ARR` or `JSON_OBJ`, you can use `WfRunVariable#jsonPath()` to allow you to mutate a specific sub-field of your object or list. For eample, if I have a `my-var` variable as follows:

```json
{
  "foo": "bar",
  "counter": 123
}
```

and I want to increment the `counter` field, I can do so as follows:

<Tabs>
  <TabItem value="java" label="Java" default>

```java
public void threadFunction(WorkflowThread thread) {
    WfRunVariable myVar = thread.addVariable("my-var", VariableType.JSON_OBJ);
    // ... execute some tasks
    thread.mutate(myVar.jsonPath("$.foo"), VariableMutationType.ADD, 1);
}
```

  </TabItem>
  <TabItem value="go" label="Go">

```go
func someThread(thread *littlehorse.WorkflowThread) {
  myVar := thread.AddVariable("my-var", lhproto.VariableType_JSON_OBJ)
  fooPath := myVar.JsonPath("$.foo")
  // ... execute some tasks
  thread.Mutate(&fooPath, lhproto.VariableMutationType_ADD, 1)
}
```

  </TabItem>
    <TabItem value="python" label="Python" default>

```python
def thread_function(thread: WorkflowThread) -> None:
    my_var = thread.add_variable("my-var", VariableType.JSON_OBJ)
    # ... execute some tasks
    thread.mutate(my_var.with_json_path("$.foo"), VariableMutationType.ADD, 1)
```

  </TabItem>
</Tabs>

You can also use `.jsonPath()` on the RHS to pick out a specific field of your `RHS` value.
