## Await Workflow Event

You can throw a `WorkflowEvent` as part of a `WfRun` by using the `WorkflowThread#ThrowEvent()` function. The `WfSpec` in this example simply sleeps for a configurable number of seconds determined by the `sleep-time` input variable, and then throws a `sleep-done` `WorkflowEvent`.

This example does the following:
* Run a `WfRun` with a configured `sleep-time`
* Wait a configured time
* Use the `rpc AwaitWorkflowEvent` with a configured timeout.

To run this example in `AwaitWorkflowEventExample`, you can do the following:

```
dotnet run -- <delay-milliseconds> <timeout-milliseconds> <workflow-sleep>
```

The arguments are as follows:

* `<delay-milliseconds>` is the time to wait after starting the `WfRun` and before making the `rpc AwaitWorkflowEvent` call.
* `<timeout-milliseconds>` is the timeout on the `rpc AwaitWorkflowEvent` call (cannot be greater than 60,000).
* `<workflow-sleep>` is the time in SECONDS that the `WfRun` will sleep.

### Scenarios

#### `WorkflowEvent` Arrives First

In this case, we send the `rpc AwaitWorkflowEvent` _after_ the `WorkflowEvent` has been registered. You can test it as follows:

```
dotnet run -- 3000 1000 1
```

#### RPC Arrives First

In this case, the `rpc AwaitWorkflowEvent` arrives at the server _before_ the `WorkflowEvent` has been registered, and it blocks until the `WorkflowEvent` is thrown.

```
dotnet run -- 500 5000 1
```

#### Timeout

In this case, the `WorkflowEvent` does not arrive before the timeout, which causes a `DEADLINE_EXCEEDED` exception to be thrown.

```
dotnet run -- 0 500 2
```
