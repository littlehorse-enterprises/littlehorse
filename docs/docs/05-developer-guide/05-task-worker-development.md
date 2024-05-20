# Developing Task Workers

Each LittleHorse SDK provides an `LHTaskWorker` object or struct which lets you turn an arbitrary function or method into a LittleHorse Task.

## Basics

The `LHTaskWorker` object allows you to create and start a Task Worker.

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs>
  <TabItem value="java" label="Java" default>

To create a Task Worker, you need to do the following:

1. Create an `LHConfig` (see [this configuration documentation](./02-client-configuration.md)).
2. Write a Task Worker class with an annotated `@LHTaskMethod` method.
3. Create an `LHTaskWorker` object with your config and Task Worker Object
4. Register the `TaskDef` with `worker.registerTaskDef()`
5. And finally call `.start()`.

Let's build a Task Worker for a `TaskDef` named `my-task` that takes in a String and returns a String. First, the Task Worker Object:

```java
class MyWorker {

    @LHTaskMethod("my-task")
    public String doTheTask(String input) {
        return "The input I got was: " + input;
    }
}
```

The `@LHTaskMethod` annotation tells the LH Library that `doTheTask` is the method that should be called for every `my-task` Task Run that is scheduled.

Finally, we can create an `LHTaskWorker`, create the `TaskDef`, and start the worker:

```java

MyWorker workerObject = new MyWorker();
LHWorkerConfig config = new LHWorkerConfig();

LHTaskWorker worker = new LHTaskWorker(workerObject, "my-task", config);
worker.registerTaskDef();
worker.start();
```

To gracefully shutdown, you can call `worker.close()`;

  </TabItem>
  <TabItem value="go" label="Go">

To create a Task Worker, you need to do three things:

1. Create a `common.LHConfig` (see [this configuration documentation](./02-client-configuration.md)).
2. Write a GoLang `func` which you will use as your Task Function.
3. Use the `taskworker.NewTaskWorker()` function to create an `LHTaskWorker` with your config and Task Function.

At this point, you can use your `LHTaskWorker` to register your `TaskDef` and to start executing tasks.

Let's build a Task Worker for a `TaskDef` named `my-task` that takes in a String and returns a String. First, the Task Function:

```go
func DoTheTask(input string) {
    return "the input I got was: " + input
}
```

Next, we can create an `LHTaskWorker` and start it:

```go
config := common.NewConfigFromEnv()
client, _ := common.NewLHClient(config)

worker, err := tasklib.NewTaskWorker(config, DoTheTask, "my-task")

// Create the TaskDef
err := worker.RegisterTaskDef()

// Start Working
worker.Start()
```

  </TabItem>
  <TabItem value="python" label="Python">

To create a Task Worker, you need to do the following:

1. Create an `LHConfig` (see [this configuration documentation](./02-client-configuration.md)).
2. Write an `async` python function which you will use as your Task Function.
3. Create and start an `LHTaskWorker` with that function.

Here is an example:

```python
import asyncio
import logging
from pathlib import Path
import random

import littlehorse
from littlehorse.config import LHConfig
from littlehorse.model.common_enums_pb2 import VariableType
from littlehorse.worker import LHTaskWorker, WorkerContext
from littlehorse.workflow import WorkflowThread, Workflow

logging.basicConfig(level=logging.INFO)


def get_config() -> LHConfig:
    config = LHConfig()
    config_path = Path.home().joinpath(".config", "littlehorse.config")
    if config_path.exists():
        config.load(config_path)
    return config


def get_workflow() -> Workflow:
    def my_entrypoint(thread: WorkflowThread) -> None:
        the_name = thread.add_variable("input-name", VariableType.STR)
        thread.execute("greet", the_name)

    return Workflow("example-basic", my_entrypoint)


async def greeting(name: str, ctx: WorkerContext) -> str:
    msg = f"Hello {name}!. WfRun {ctx.wf_run_id}"
    print(msg)
    await asyncio.sleep(random.uniform(0.5, 1.5))
    return msg


async def main() -> None:
    config = get_config()

    littlehorse.create_task_def(greeting, "greet", config)
    littlehorse.create_workflow_spec(get_workflow(), config)

    await littlehorse.start(LHTaskWorker(greeting, "greet", config))


if __name__ == "__main__":
    asyncio.run(main())
```

  </TabItem>
</Tabs>

## Advanced Usage

The Task Worker library has some features that make advanced use cases easier.

### Throwing Workflow `EXCEPTION`s

As described in our [Failure Handling Concept Docs](/docs/concepts/exception-handling#business-exceptions), LittleHorse distinguishes between technical `ERROR`s and business `EXCEPTION`s:

* A technical `ERROR` denotes a technological failure, such as a Timeout caused by a network outage, or an unexpected error returned by your Task Worker.
* A Business `EXCEPTION` represents an unhappy-path case in your business logic, such as when an item is out of stock or a credit card got declined.

If your Task Worker throws an uncaught error (depending on your language), then it is treated as a LittleHorse `ERROR` with the error code `LHErrorType.TASK_FAILURE`. However, sometimes your Task Worker notices that a business process-level failure (what LittleHorse calls an `EXCEPTION`) has occurred. For example, the Task Worker could notice that a credit card got declined. In this case, you can make the `TaskRun` throw a LittleHorse `EXCEPTION` by using the `LHTaskException` object.

In the following example, we will throw the `out-of-stock` user-defined business `EXCEPTION` if the item is out of stock.

<Tabs>
  <TabItem value="java" label="Java" default>

```java
@LHTaskMethod("ship-item")
public String shipItem() throws Exception {
    if (isOutOfStock()) {
        throw new LHTaskException("out-of-stock", "some descriptive message");
    }
}
```

  </TabItem>
  <TabItem value="go" label="Go">

The Go SDK currently (as of `0.7.2`) does not yet support throwing `LHTaskException`s.

  </TabItem>
  <TabItem value="python" label="Python">

```python
async def ship_ite() -> None:
    if is_out_of_stock():
        raise LHTaskException("out-of-stock", "some descriptive message")
```

  </TabItem>
</Tabs>


### Json Deserialization

In some SDK's, LittleHorse will automatically deserialize JSON variables into objects or structs for you.


<Tabs>
  <TabItem value="java" label="Java" default>
Let's say we have a class `MyCar` as follows:

```java
class MyCar {
    String make;
    String model;

    public MyCar(String make, String model) {
        this.make = make;
        this.model = model;
    }

    // getters, setters omitted
}
```

And one of the `Variable`s (for example, `my-obj`) in our `WfSpec` is of type `JSON_OBJ`.

Let's say there's a `TaskDef` called `json-example` with one input variable of type `JSON_OBJ`. We can have a Task Worker defined as follows:

```java
class MyWorker {

    @LHTaskMethod("json-example")
    public void executeTask(MyCar input) {
        System.out.println(input.getMake());
        System.out.println(input.getModel());
    }
}
```

The Library will deserialize the JSON from something like: `{"make": "Ford", "model": "Explorer"}` to an actual `MyCar` object.

  </TabItem>
  <TabItem value="go" label="Go">
Let's say we have a struct `MyCar` as follows:

```go
car := &MyCar{
    Make:  "Ford",
    Model: "Explorer",
}
```

And one of the `Variable`s (for example, `my-obj`) in our `WfSpec` is of type `JSON_OBJ`.

Let's say there's a `TaskDef` called `json-example` with one input variable of type `JSON_OBJ`. We can have a Task Function that looks like:

```go
func MyTaskFunc(car *MyCar) string {
    return "the make of your car is " + car.Make + "!"
}
```

The Library will deserialize the JSON from something like: `{"make": "Ford", "model": "Explorer"}` to an actual `MyCar` struct.

  </TabItem>
  <TabItem value="python" label="Python">

Let's say we have a python Task Function as follows:

```python
async def describe_car(car: dict[str, Any]) -> str:
    msg = f"You drive a {car['brand']} model {car['model']}"
    return msg
```

The Library will deserialize the JSON from something like: `{"brand": "Ford", "model": "Explorer"}` to a python `dict`.

  </TabItem>
</Tabs>

### Accessing Metadata

Sometimes, your Task Worker needs to know something about where the `TaskRun` came from. Each LittleHorse SDK offers a `WorkerContext` object or struct that exposes this metadata to the Task Worker.

<Tabs>
  <TabItem value="java" label="Java" default>

If you need to access metadata about the Task Run that is being executed, you can add a `WorkerContext` parameter to the end of your method signature for the Task Method.

Let's say you have a `TaskDef` with one input parameter of type `INT`. You can access the `WorkerContext` by doing the following:

```java
class SomeWorker {

    @LHTaskMethod("my-task")
    public void doTask(long inputLong, WorkerContext context) {
        String wfRunId = context.getWfRunId();
        TaskRunId taskRunId = context.getTaskRunId();
        NodeRunId nodeRunId = context.getNodeRunId();

        Date timeWhenTaskWasScheduled = context.getScheduledTime();

        context.log(
            "This is a message that gets sent to the log output on the scheduler"\
        );

        int attemptNumber = context.getAttemptNumber();
        if (attemptNumber == 0) {
            // then this is the first time this Task Run has been attempted.
        } else {
            // then this is a retry.
        }

        // This is a constant value between all attempts for this TaskRun.
        // Useful to allow retries to third-party API's that accept idempotency
        // keys, such as Stripe.
        String idempotencyKey = context.getIdempotencyKey();
    }
}
```


  </TabItem>
  <TabItem value="go" label="Go">

If you need to access metadata about the Task Run that is being executed, you can add a `WorkerContext` parameter to the end of your method signature for the Task Method.

Let's say you have a `TaskDef` with one input parameter of type `INT`. You can access the `WorkerContext` by doing the following:

```go
func DoTask(long inputLong, context *common.WorkerContext) {
	wfRunId := context.GetWfRunId();
	taskRunId := context.GetTaskRunId();
    nodeRunId := context.GetNodeRunId();

	timeWhenTaskWasScheduled := context.GetScheduledTime();

	context.Log(
		"This is a message that gets sent to the log output on the scheduler",
	);

	attemptNumber := context.GetAttemptNumber();
	if (attemptNumber == 0) {
		// then this is the first time this Task Run has been attempted.
	} else {
		// then this is a retry.
	}

	idempotencyKey := context.GetIdempotencyKey();
}
```


  </TabItem>
  <TabItem value="python" label="Python">

If you need to access metadata about the Task Run that is being executed, you can add an `LHWorkerContext` parameter to the end of your method signature for the Task Method.

Let's say you have a `TaskDef` with one input parameter of type `INT`. You can access the `LHWorkerContext` by doing the following:

```python

async def greeting(name: str, ctx: LHWorkerContext) -> str:
    task_run_id = ctx.task_run_id
    node_run_id = ctx.node_run_id
    wf_run_id = ctx.node_run_id

    time_task_was_scheduled = ctx.scheduled_time

    attempt_number = ctx.attempt_number
    if attempt_number > 0:
        # this is a retry
        pass
    else:
        # this is not a retry
        pass
    
    idempotency_key = ctx.idempotency_key
    return "asdf"

```

  </TabItem>
</Tabs>

## Best Practices

### Client ID

Every Task Worker instance should have a unique `LHC_CLIENT_ID` set in its configuration. This is important so that you can audit which client executed which Task, and also so that the LH Server can efficiently assign partitions of work to your Task Workers.

### Idempotence

With all workflow engines, it is best when your tasks are idempotent. You can use the `NodeRunIdPb` from `WorkerContext::getNodeRunId()` as an idempotency key.
