# Basics

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

To develop a `WfSpec` in LittleHorse, you can use the `Workflow` struct or object in our SDK's. Generally, the `Workflow` entity constructor requires two arguments:

1. The name of the `WfSpec` to create.
2. A `ThreadFunc`, which is function pointer, lambda function, or interface of some sort which contains the logic for the Entrypoint `ThreadSpec`.

The `Workflow` object translates your `ThreadFunc` into a `WfSpec`. As per the [Metadata Management Documentation](/docs/developer-guide/grpc/managing-metadata), you can easily deploy a `WfSpec` once you've gotten the `Workflow` object.

The `ThreadFunc` takes in one argument: a `WorkflowThread`. Everything you do goes through the `ThreadFunc`. The `ThreadFunc` defines a `ThreadSpec`, and the `ThreadFunc` passed into the `Workflow` object or struct is used to build the Entrypoint Thread.

## Quickstart

Below you can find executable files that define a `WfSpec` with a single step: execute the `greet` TaskDef with the supplied `first-name` variable which comes as input. As a prerequisite, you need to have the `greet` `TaskDef` already registered in your LittleHorse Cluster.

:::tip
If you haven't yet created a `TaskDef` named `greet`, you can do it by following our [Task Worker Development Quickstart](../05-task-worker-development.md#quickstart).
:::

<Tabs>
  <TabItem value="java" label="Java" default>

```java
package io.littlehorse.quickstart;

import java.io.IOException;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.WorkflowThread;
import io.littlehorse.sdk.common.proto.VariableType;


public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        LHConfig config = new LHConfig();

        // The `Workflow` object uses the DSL to compile the WfSpec
        Workflow workflowGenerator = Workflow.newWorkflow("my-wf-spec", Main::wfLogic);

        // Convenience method to register the `WfSpec` automatically.
        workflowGenerator.registerWfSpec(config.getBlockingStub());
    }

    // NOTE: this can be static or non-static.
    static void wfLogic(WorkflowThread wf) {
        // Required input variable.
        WfRunVariable firstName = wf.addVariable("first-name", VariableType.STR).required();

        // Execute the `greet` Task and pass in `first-name` as an argument.
        wf.execute("greet", firstName);
    }
}
```

  </TabItem>
  <TabItem value="go" label="Go">

```go
package main

import (
	"context"
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
)

func wfLogic(wf *littlehorse.WorkflowThread) {
	firstName := wf.AddVariable("first-name", lhproto.VariableType_STR).Required()
	wf.Execute("greet", firstName)
}

func main() {
	// Get a client
	config := littlehorse.NewConfigFromEnv()
	client, _ := config.GetGrpcClient()

	workflowGenerator := littlehorse.NewWorkflow(wfLogic, "my-wfspec")

	request, err := workflowGenerator.Compile()
	if err != nil {
		log.Fatal(err)
	}

	(*client).PutWfSpec(context.Background(), request)
}
```

  </TabItem>
  <TabItem value="python" label="Python" default>

```python
from littlehorse.workflow import WorkflowThread, WfRunVariable, Workflow
from littlehorse.config import LHConfig
from littlehorse.model import VariableType, PutWfSpecRequest


def workflow_logic(wf: WorkflowThread) -> None:
    first_name: WfRunVariable = wf.add_variable("first-name", VariableType.STR).required()
    wf.execute("greet", first_name)

if __name__ == '__main__':
    config = LHConfig()
    client = config.stub()

    workflow_generator = Workflow("my-wfspec", workflow_logic)
    request: PutWfSpecRequest = workflow_generator.compile()

    client.PutWfSpec(request)
```

  </TabItem>
</Tabs>

At this point, whether you used python, go, or Java for the WfSpec, you should be able to run the `WfSpec` via the following command:

```
lhctl run my-wfspec first-name Obi-Wan
```

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

### Searchable and Required Variables

It is often desirable to be able to search for a `WfRun` based on the value of the `Variable`s inside it. For example, how can I find the `WfRun` that has `email=foo@bar.com`? You can do that via the `rpc SearchVariable`

In order to do that, however, you must first put an index on your `Variable` by using the `.searchable()` method.

Additionally, you can use the `.required()` method to make a `Variable` required as input to the `ThreadRun`. If you do this on your Entrypoint `ThreadRun`, then the `RunWfRequest` must specify a value for that `Variable`.

:::note
Putting an Index on a `Variable` or making the `Variable` "Required" means that the `Variable` becomes part of the public API of the `WfSpec`. That means you will increment a "major version" upon adding or removing an Index on a `Variable`. For more info, check out our docs on [WfSpec Versioning](../../04-concepts/30-advanced/00-wfspec-versioning.md).
:::

### Defining Variables

<Tabs>
  <TabItem value="java" label="Java" default>

```java
import io.littlehorse.sdk.wfsdk.WorkflowThread;
import io.littlehorse.sdk.wfsdk.WfRunVariable;

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
    WfRunVariable myVar = thread.addVariable("my-variable", "Hello, there!").searchable();
}
```

And you can mark the `Variable` as Required as follows:
```java
public void threadFunction(WorkflowThread thread) {
    WfRunVariable myVar = thread.addVariable("my-variable", "Hello, there!").required();
}
```

  </TabItem>
  <TabItem value="go" label="Go">

```go
func myThreadFunc(thread *littlehorse.WorkflowThread) {
    myVar := thread.AddVariable("my-variable", lhproto.VariableTypePb_STR)
}
```

You can add do the same and set a default value for the `Variable` as follows:

```go
func myThreadFunc(thread *littlehorse.WorkflowThread) {
    nameVar := thread.AddVariableWithDefault("my-variable", lhproto.VariableType_STR, "Ahsoka Tano")
}
```

You can add an index on a `WfRunVariable` to make the variable searchable.
```go
func myThreadFunc(thread *littlehorse.WorkflowThread) {
    nameVar := thread.AddVariableWithDefault("my-variable", lhproto.VariableType_STR, "Ahsoka Tano").Searchable()
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
    the_name = thread.add_variable("input-name", VariableType.STR).searchable()

    # optionally make the variable a Required variable
    the_name.required()
```

  </TabItem>
</Tabs>

### Masked Variables

In certain situations, you may need to mask the contents of a particular variable, ensuring that it remains hidden from users.
You can achieve this by using masked variables, which will only allow access to the variable's content within your `WfRun`.

<Tabs>
  <TabItem value="java" label="Java" default>

```java
public void threadFunction(WorkflowThread thread) {
    WfRunVariable myVar = thread.addVariable("my-masked-variable", VariableTypePb.STR).masked();
}
```

  </TabItem>
  <TabItem value="go" label="Go">

```go
func myThreadFunc(thread *littlehorse.WorkflowThread) {
    myVar := thread.AddVariable("my-masked-variable", lhproto.VariableTypePb_STR)
    myVar.Masked()
}
```

  </TabItem>
  <TabItem value="python" label="Python" default>

```python
def thread_function(thread: WorkflowThread) -> None:
    the_name = thread.add_variable("input-name", VariableType.STR).masked()
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
public void myWf(WorkflowThread thread) {
    NodeOutput output = thread.execute("foo");
}
```

  </TabItem>
  <TabItem value="go" label="Go">

```go
func myThreadFunc(thread *littlehorse.WorkflowThread) {
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
func threadFunction(thread *littlehorse.WorkflowThread) {
    myStr := thread.AddVariable("my-str", lhproto.VariableType_STR)
    myInt := thread.AddVariable("my-int", lhproto.VariableType_INT)

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

You can also define a input masked variables by adding some metadata to the task argument or return values. Using the 
same Python example:

```python
async def my_task(some_str: Annotated[str, LHType(name="some_str", masked=True)], some_int: int) -> str:
    return f"Inputs were {some_str} and {some_int}"
```

:::note
    Any type of variable can be masked, not limited to string types.
:::

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
wf := littlehorse.NewWorkflow(basic.MyWorkflow, "my-workflow")
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