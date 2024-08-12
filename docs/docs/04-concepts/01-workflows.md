# Workflows

In LittleHorse, the [`WfSpec`](../08-api.md#wfspec) object is a _Metadata Object_ defining the blueprint for a [`WfRun`](../08-api.md#wfrun), which is a running instance of a workflow.

A simple way of thinking about it is that a `WfSpec` is a directed graph consisting of `Node`s and `Edge`s, where a `Node` defines a "step" of the workflow process, and an `Edge` tells the workflow what `Node` to go to next.

## Workflow Structure

A `WfSpec` (Workflow Specification) is a blueprint that defines the control flow of your `WfRun`s (Workflow Run). Before you can run a `WfRun`, you must first register a `WfSpec` in LittleHorse (for an example of how to do that, see [here](../05-developer-guide/08-wfspec-development/01-basics.md#quickstart)).

A `WfSpec` contains a set of `ThreadSpec`s, with one _special_ `ThreadSpec` that is known as the _entrypoint_. When you run a `WfSpec` to create a `WfRun`, the first `ThreadSpec` that is run is the _entrypoint_.

:::info
You can see the exact structure of a `WfSpec` as a protobuf message [in our api docs](../08-api.md#wfspec).
:::

A [`WfRun`](../08-api.md#wfrun), short for Workflow Run, is an instantiation of a `WfSpec`.

In the programming analogy, you could think of a `WfRun` as a process that is running your `WfSpec` program. A `ThreadRun` is a thread in that program.

A `WfRun` is created by the LittleHorse Server when a user requests the server to run a `WfSpec`, for example using the [`rpc RunWf`](../08-api.md#runwf).

### Threads

A workflow consists of one or more threads. A thread in LittleHorse is analogous to a thread in programming: it has its own thread execution context (set of LH `Variables`) and it can execute one instruction (in LH, a `Node`) at a time.

A `ThreadSpec` is a blueprint for a single thread in a `WfSpec`. When a `ThreadSpec` is run, you get a `ThreadRun`. Logically, a `ThreadSpec` is a directed graph of `Node`s and `Edge`s, where a `Node` represents a "step" to execute in the `ThreadRun`, and the `Edge`s tell LittleHorse what `Node` the `ThreadRun` should move to next.

In the LittleHorse Dashboard, when you click on a `WfSpec` you are shown the _entrypoint_ `ThreadSpec`. In the picture you see, the circles and boxes are `Node`s, and the arrows are `Edge`s.

A `ThreadRun` can only execute one `Node` at a time. If you want a `WfRun` to execute multiple things at a time (either to parallelize `TaskRun`'s for performance reasons, or to wait for two business events to happen in parallel, or any other reason), then you need your `WfRun` to start multiple `ThreadRun`s at a time. See the section on Child Threads below for how this works.

For the highly curious reader, you can inspect the structure of a `ThreadRun` in our [api docs here](../08-api.md#threadrun). At a high level it contains a status and a pointer to the current `NodeRun` that's being executed. The real data is stored in the `NodeRun`, which you can retrieve from the API as a separate object.


### Variables

Just as a program or function can store state in variables, a `WfRun` can store state in `Variable`s as well. Variables in LittleHorse are defined in the `ThreadSpec`, and as such are scoped to a `ThreadRun`. Note that a child `ThreadRun` may access the variables of its parents.

A `Variable` is an object that you can fetch from the LittleHorse API. A `Variable` is uniquely identified by a [`VariableId`](../08-api.md#variableid), which has three fields:

1. The `wf_run_id`, which is the ID of the associated `WfRun`.
2. The `thread_run_number`, which is the ID of the associated `ThreadRun` (since a `Variable` lives within a specific `ThreadRun`).
3. The `name`, which is the name of the `Variable`.

A `Variable` is created when a `ThreadRun` is created. Since it's possible to have multiple `ThreadRun`s created with the same `ThreadSpec` (for example, iterating over a list and launching a child thread to process each item), simply identifying a `Variable` by its `name` and `wf_run_id` is insufficient. That is why the `VariableId` also includes the `thread_run_number`: a `Variable` is uniquely identified by its name, workflow run id, and thread run number.

You can fetch `Variable`s using [`rpc GetVariable`](../08-api.md#getvariable), [`rpc SearchVariable`](../08-api.md#searchvariable), and [`rpc ListVariables`](../08-api.md#listvariables).

Variables can be of certain types, which you can find in the [`VariableType`](../08-api.md#variabletype) enum.

Lastly, a `Variable`'s [value](../08-api.md#variablevalue) can be set when the thread is created, and the value can be mutated using a [`VariableMutation`](../08-api.md#variablemutation) after the completion of a `Node`.

### Nodes

A `Node` is a "step" in a `ThreadRun`. LittleHorse allows for many different types of `Node`s in a `WfSpec`, including:

* [`TASK`](../08-api.md#tasknode) nodes, which allow for executing a `TaskRun`.
* [`USER_TASK`](../08-api.md#usertasknode) nodes, which allow for executing [User Tasks](./05-user-tasks.md).
* [`EXTERNAL_EVENT`](../08-api.md#externaleventnode) nodes, which allow for waiting for an [External Event](./04-external-events.md) to arrive.
* [`START_THREAD`](../08-api.md#startthreadnode) nodes, which allow for starting child `ThreadRun`s.
* [`WAIT_FOR_THREADS`](../08-api.md#waitforthreadsnode) nodes, which allow for waiting for child `ThreadRun`s to complete.

:::info
For a complete list of all of the available node types, check out the [`Node`](../08-api.md#node) protobuf message.
:::

A `Node` is not a fully-fledged object in the LittleHorse API. Rather, it is a sub-structure of the `WfSpec` object, which _is_ an object in the LH API.

When a `ThreadRun` arrives at a `Node`, LittleHorse creates a [`NodeRun`](../08-api.md#noderun), which is an instance of a `Node`. In the case of a `TASK` Node, a `TaskNodeRun` is created (which also causes the creation of a [Task](./03-tasks.md) which is dispatched to a Task Worker).

In contrast to a `Node`, a `NodeRun` is an [object in the LH API](../08-api.md#noderun), which stores data about:

* When the `ThreadRun` arrived at the `Node`
* When the `NodeRun` was completed (if at all).
* The status of the `NodeRun`.
* A pointer to any related objects (for example, a Task `NodeRun` has a pointer to a `TaskRun`).

When you click on a `NodeRun` in the dashboard, that information is fetched and displayed on a screen. You can also retrieve information about a `NodeRun` via some `lhctl` commands:

* `lhctl list nodeRun <wfRunId>`: shows all `NodeRun`'s from a `WfRun`.
* `lhctl get nodeRun <wfRunId> <x> <y>`: retrieves the `y`th `NodeRun` from the `x`th `ThreadRun` in the specified `WfRun`.

## Threading Model

Just as a `WfSpec` is a blueprint for a `WfRun` (workflow), a `ThreadSpec` is a blueprint for a `ThreadRun` (thread). A `ThreadSpec` is a sub-structure of a `WfSpec`; a `ThreadRun` is a sub-structure of a `WfRun`, and therefore neither are top-level objects in the LittleHorse API.

Every workflow has one special thread called the Entrypoint Thread. If you consider a `WfSpec` as a program, then you could say that the Entrypoint `ThreadSpec` is like the `main()` method of the `WfSpec`/program.

When a `WfSpec` is run and a `WfRun` is created, the `WfRun` creates an Entrypoint `ThreadRun` which is an instance of the specified Entrypoint `ThreadSpec`.

For many workflows with only one thread (for example, our [quickstarts](../05-developer-guide/00-install.md#get-started)), the Entrypoint Thread is the only thread in the workflow, and thus it's often simple to think of it as just the entire workflow.

### Child Threads

In computer science, the main thread of a program can launch child threads within the same process. Child threads in programming run in the same memory address space and can share certain variables with the parent process.

Similarly, LittleHorse allows you to [launch child threads](../05-developer-guide/08-wfspec-development/07-child-threads.md). A child thread results in a new `ThreadRun` being created in the same `WfRun`.

Child Threads have many use-cases. A subset of those are:

* **Parallel Execution:** A single `ThreadRun` can only execute one `Node` at a time. Child `ThreadRun`s allow you to execute multiple business process threads at once within a single workflow.
* **Error Handling Boundaries:** You can attach [Failure Handlers](#failure-handling) to a single `Node` (for example, a `TaskRun`), or to a whole `ThreadRun` by attaching it to the `WaitForThreadsNode`.
* **Workflow Decomposition:** Using a Thread allows you to decompose your workflow into smaller logical chunks which makes for more understandable code and workflow diagrams.
* **Repeatable Functionality:** Certain workflows may require executing the same business process with multiple inputs. For example, a workflow might require asking three different departments to approve a change. You could use the same `ThreadSpec` with different input variables, each running as a child `ThreadRun` sequentially or in parallel.

### Variable Scoping

As described above, a `Variable` is scoped to the `ThreadRun` level. A `Variable` object is created in the LittleHorse API when a `ThreadRun` starts.

When a child `ThreadRun` of any type is started, it has _read and write_ access to its own `Variable`s, and all `Variable`s that its parent has access to (including the parent's parent, and so on).

:::info
Since a `ThreadRun` can have multiple children, the parent does _not_ have access to the variables of the children.
:::

### Failure Handling

A [`Failure`](../08-api.md#failure) in LittleHorse is like an Exception in programming. It means that A Bad Thing® has happened. In a workflow engine like LittleHorse, there are two potential sources of Failure:

1. A technical process, such as an external API call, fails.
2. Something goes wrong at the business process level; for example, a credit card has insufficient funds.

:::caution
Exception Handling in LittleHorse is a fully separate concept from [`TaskRun` retries](https://littlehorse.dev/docs/concepts/tasks#retries-and-taskattempt).
:::

#### `ERROR`s and `EXCEPTION`s

A `Failure` that is a result of a technical problem, such as a variable casting error or a `TaskRun` timeout, is an `ERROR` in LittleHorse. All `ERROR`s are pre-defined by the LittleHorse System. You can find them at the [`LHErrorType` documentation](../08-api.md#lherrortype).

In contrast, a business process-level failure is an `EXCEPTION`. All `EXCEPTION`s are defined by users of LittleHorse. You must explicitly throw an `EXCEPTION` with a specific `name`.

By rule LittleHorse uses the following naming conventions for `ERROR`s and `EXCEPTION`s:

* `ERROR`'s are pre-defined in the `LHErrorType` enum and follow `UPPER_UNDERSCORE_CASE`.
* `EXCEPTION` names are defined by users and follow `kebab-case`.

As per the [Exception Handling Developer Guide](../05-developer-guide/08-wfspec-development/06-exception-handling.md), you may have different error handling logic for different `Failure`s. For example, you can catch failures for a specific `ERROR`, any `ERROR`, a specific `EXCEPTION`, any `EXCEPTION`, or any `Failure`.

### Interrupts

There are four types of `ThreadRun`s in LittleHors:
* `ENTRYPOINT` threads
* `CHILD` threads, which are created explicitly via a [`StartThreadNode`](../08-api.md#startthreadnode) or a [`StartMultipleThreadsNode`](../08-api.md#startmultiplethreadsnode).
* `FAILURE_HANDLER` threads, described above.
* `INTERRUPT` threads.

For a description of `INTERRUPT` threads, please check out the [External Event docs](./04-external-events.md#interrupts).

## Lifecycle

The status of a `WfRun` is determined by looking at the status of the Entrypoint `ThreadRun`. A `ThreadRun`, and by extension a `WfRun`, can have one of the following statuses, determined by the [`LHStatus` enum](../08-api.md#lhstatus):

- `RUNNING`
- `HALTING`
- `HALTED`
- `ERROR`
- `EXCEPTION`
- `COMPLETED`

### Halting a Workflow

A `ThreadRun` can be halted for any of the following reasons:

* If a `StopWfRun` request is received (manual halt by system administrator).
* When interrupted by an `ExternalEvent` which triggers an Interrupt Handler.
* If the `ThreadRun` is a child thread, and the parent `ThreadRun` is `HALTED` o.

Note that halting a parent `ThreadRun` causes all of the children of that `ThreadRun` to be halted as well.

When a `ThreadRun` is halted, it first moves to the `HALTING` status until the current `NodeRun` can be halted as well (for example, it's always possible to halt an `ExternalEventNode` but a `TaskNode` can't be halted while there is an in-flight `TaskAttempt`).

The criteria for halting a `ThreadRun` are as follows:

- If the `ThreadRun` has any child threads, all children must be in the `COMPLETED`, `ERROR`, or `HALTED` state.
  - If this condition is not satisfied, then the runtime will halt all Children.
- There can be no `TaskRun`s that have been dispatched to a Task Worker but not completed, failed, or timed out. In other words, no in-flight tasks.

If a `WfRun` is waiting at an `EXTERNAL_EVENT`, `USER_TASK`, `WAIT_FOR_THREADS`, or `SLEEP` Node, the second condition is automatically satisfied.

