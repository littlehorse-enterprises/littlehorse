import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Exception Handling

In LittleHorse, a `Failure` is analogous to an `Exception` in Programming.

## Handling a Failure.

In LittleHorse, there are two different types of Failures:
* `EXCEPTION`, which denotes something that went wrong at the business-process level (eg. an executive rejected a transaction).
* `ERROR`, which denotes a technical failure, such as a third-party API being unavailable due to a network partition.

The `WorkflowThread` has three methods to allow you to handle various types of Failures:

1. `handleException()`, which handles `EXCEPTION` business failures.
2. `handleError()`, which handles `ERROR` technical failures.
3. `handleAnyFailure()`, which catches any failure of any type.

All three methods require a `NodeOutput` for the `Node` on which to add the `failureHandler` (see the [Concept Docs](/docs/04-concepts/10-exception-handling.md)). Additionally, all three methods require a `ThreadFunc` which defines the logic for the Failure Handler (either a lambda or a function pointer).

The syntax to handle a `Failure` is similar no matter which type of `Node` you are handling a failure for.

### Throwing an `EXCEPTION`

:::info
This section is concerned with throwing an `EXCEPTION` at the `ThreadSpec` level inside a `WfSpec`. If you want to throw an `EXCEPTION` at the Task Worker level, please refer to the [Task Worker Development Docs](/docs/developer-guide/task-worker-development#throwing-workflow-exceptions)
:::

In most programming languages such as Java and Python, you can `throw` or `raise` an `Exception`. For example:

```python
class MyError(Exception):
    def __init__(self, foo: str):
        self._foo = foo

if something_bad_happens():
   raise MyError("bar")

do_something_else()
```

Raising a `MyError` here interrupts the flow of the program and prevents `do_something_else()` from being called. Similarly, throwing an `EXCEPTION` in LittleHorse can stop the flow of the `ThreadRun`.

:::info
Even though GoLang itself doesn't allow you to interrupt program execution with exceptions, you can still use the Go SDK to define a `WfSpec` that throws a LittleHorse `EXCEPTION`.
:::

Let's throw an `EXCEPTION` with the name `payment-failed`. To do this, we will need the `WorkflowThread#fail()` method, which takes two arguments:

1. The name of the exception to throw.
2. A human readable error message which will show up on the `WfRun`.

Note that you can optionally specify a third argument, which is either a `WfRunVariable` or some literal value that represents the _content_ of the Exception we throw. In future versions of LittleHorse, you will be able to access this value as an input variable in the Exception Handler `ThreadRun`.

<Tabs>
  <TabItem value="java" label="Java" default>

```java
// Throw a normal exception
wf.fail("payment-failed", "This is a human readable error message for developers");

// Throw an exception with content
WfRunVariable exnContent = ...;
wf.fail("payment-failed", "This is a human readable error message for developers", exnContent);
```

  </TabItem>
  <TabItem value="go" label="Go">

```go
wf.Fail(nil, "payment-failed", "This is a human readable error message for developers")

// Fail with output.
var exnContent *model.WfRunVariable
// ...
wf.Fail(exnContent, "payment-failed", "This is a human readable error message for developers")
```

  </TabItem>
    <TabItem value="python" label="Python" default>

```python
wf.fail("payment-failed", "This is a human readable error message for developers")
```

  </TabItem>
</Tabs>

:::tip
Like many things in LittleHorse, a user-defined `EXCEPTION` must be in `sub-domain-case`. For those of you who love Kubernetes, this is the same regex used by K8s resource names.
:::

### Handling Exceptions

Let's handle a business failure with the `WorkflowThread#handleException` method. You need to provide:
1. The `NodeOutput` to handle the failure on.
2. A `ThreadFunc` (function pointer or lambda) to execute as the exception handler.

You can optionally provide the name of a specific `EXCEPTION` to handle. If that is not provided, it will handle any business `EXCEPTION` (but not a technical failure).

In this example, we will handle an `EXCEPTION` thrown by a Child `ThreadRun`. We catch the exception from the `waitForTheads()` call.

:::tip
You'll notice that we have two Failure Handlers defined in the example below. The way this behaves in practice is that the _first matching handler_ is executed. This is useful to allow you to handle different business exceptions with different exception handlers.
:::

<Tabs>
  <TabItem value="java" label="Java" default>

```java
// ...
NodeOutput threadsResult = wf.waitForThreads(...);

wf.handleException(
    threadsResult,
    "my-exn", // handle only the "out-of-stock" exception
    handler -> {
        handler.execute("some-failure-handler-for-my-exn");
    }
);

// The `handleException()` method with only two arguments catches all EXCEPTIONS
wf.handleException(
    threadsResult,
    handler -> {
        handler.execute("some-other-task-in-failure-handler");
    }
);

// We get here unless the Failure Handler fails.
wf.execute("another-task");
```

  </TabItem>
  <TabItem value="go" label="Go">

```go
threadsResult := wf.WaitForThreads(...)

exnName := "my-exn"
wf.HandleException(
    &threadsResult,
    &exnName, // handle specific exception
    func(handler *wflib.WorkflowThread) {
        handler.Execute("some-task-in-my-exn-handler")
    },
)

wf.HandleException(
    &taskOutput,
    &nil, // handle any exception
    func(handler *wflib.WorkflowThread) {
        handler.Execute("some-other-task-in-failure-handler")
    },
)

// We will always get here unless the Failure Handler fails.
wf.Execute("another-task");
```

  </TabItem>
  <TabItem value="python" label="Python">

```python
def entrypoint(wf: WorkflowThread) -> None:
    def my_exn_handler(handler: WorkflowThread) -> None:
        handler.execute("some-task-in-my-exn-handler")

    def any_exn_handler(handler: WorkflowThread) -> None:
        handler.execute("some-other-task-in-failure-handler")

    threads_result = wf.wait_for_threads(...)
    wf.handle_exception(output, my_exn_handler, exn_name="my-exn")
    wf.handle_exception(output, any_exn_handler, exn_name=None)

    # We will always get here unless the Failure Handler fails.
    wf.execute("another-task")
```

  </TabItem>
</Tabs>

### Handling Errors

Let's handle a technical failure with the `WorkflowThread#handleError` method. Just as with `handleException()`, you need to provide:
1. The `NodeOutput` to handle the failure on.
2. A `ThreadFunc` (function pointer or lambda) to execute as the exception handler.

You can optionally provide the name of a specific `ERROR` to handle. If that is not provided, it will handle any technical `ERROR` (but not a business `EXCEPTION`).

In this example, we will handle a `TIMEOUT` error from a `TaskRun`.

<Tabs>
  <TabItem value="java" label="Java" default>

```java
NodeOutput taskOutput = wf.execute("flaky-task");

wf.handleError(
    taskOutput,
    "TIMEOUT", // handle only TIMEOUT errors. Leave null to catch any ERROR.
    handler -> {
        handler.execute("some-task");
    }
);

```

  </TabItem>
  <TabItem value="go" label="Go">

```go
threadsResult := wf.WaitForThreads(...)

errorName := "TIMEOUT"
wf.HandleError(
    &threadsResult,
    &errorName, // handle only TIMEOUT error. Leave nil to catch all ERROR.
    func(handler *wflib.WorkflowThread) {
        handler.Execute("some-task-in-my-exn-handler")
    },
)
```

  </TabItem>
  <TabItem value="python" label="Python">

```python
def entrypoint(wf: WorkflowThread) -> None:
    def error_handler(handler: WorkflowThread) -> None:
        handler.execute("some-task")

    task_output = wf.wait_for_threads(...)
    wf.handle_error(task_output, error_handler, error_type=LHErrorType.TIMEOUT)
```

  </TabItem>
</Tabs>
