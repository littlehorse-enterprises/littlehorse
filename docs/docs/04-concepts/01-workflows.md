# Workflows

In LittleHorse, the [`WfSpec`](../08-api.md#wfspec) object is a _Metadata Object_ defining the blueprint for a [`WfRun`](../08-api.md#wfrun), which is a running instance of a workflow.

A simple way of thinking about it is that a `WfSpec` is a directed graph consisting of `Node`s and `Edge`s, where a `Node` defines a "step" of the workflow process, and an `Edge` tells the workflow what `Node` to go to next.


## Workflow Structure

A `WfSpec` (Workflow Specification) is a blueprint that defines the control flow of your `WfRun`s (Workflow Run). Before you can run a `WfRun`, you must first register a `WfSpec` in LittleHorse (for an example of how to do that, see [here](../05-developer-guide/08-wfspec-development/01-basics.md#quickstart)).

A `WfSpec` contains a set of `ThreadSpec`s, with one _special_ `ThreadSpec` that is known as the _entrypoint_. When you run a `WfSpec` to create a `WfRun`, the first `ThreadSpec` that is run is the _entrypoint_.

:::info
You can see the exact structure of a `WfSpec` as a protobuf message [in our api docs](../08-api.md#wfspec).
:::

### `ThreadSpec`s

A `ThreadSpec` is a blueprint for a single thread of execution in a `WfSpec`. When a `ThreadSpec` is run, you get a `ThreadRun`. Logically, a `ThreadSpec` is a directed graph of `Node`s and `Edge`s, where a `Node` represents a "step" to execute in the `ThreadRun`, and the `Edge`s tell LittleHorse what `Node` the `ThreadRun` should move to next.

In the LittleHorse Dashboard, when you click on a `WfSpec` you are shown the _entrypoint_ `ThreadSpec`. In the picture you see, the circles and boxes are `Node`s, and the arrows are `Edge`s.

A `ThreadRun` can only execute one `Node` at a time. If you want a `WfRun` to execute multiple things at a time (either to parallelize `TaskRun`'s for performance reasons, or to wait for two business events to happen in parallel, or any other reason), then you need your `WfRun` to start multiple `ThreadRun`s at a time. See the section on Child Threads below for how this works.

For the highly curious reader, you can inspect the structure of a `ThreadRun` in our [api docs here](../08-api.md#threadrun). At a high level it contains a status and a pointer to the current `NodeRun` that's being executed. The real data is stored in the `NodeRun`, which you can retrieve from the API as a separate object.

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

### `NodeRun`s

When a `ThreadRun` arrives at a `Node`, LittleHorse creates a [`NodeRun`](../08-api.md#noderun), which is an instance of a `Node`. In the case of a `TASK` Node, a `TaskNodeRun` is created (which also causes the creation of a [Task](./03-tasks.md) which is dispatched to a Task Worker).

A `NodeRun` is a real [object in the LH API](../08-api.md#noderun), which stores data about:

* When the `ThreadRun` arrived at the `Node`
* When the `NodeRun` was completed (if at all).
* The status of the `NodeRun`.
* A pointer to any related objects (for example, a Task `NodeRun` has a pointer to a `TaskRun`).

When you click on a `NodeRun` in the dashboard, that information is fetched and displayed on a screen. You can also retrieve information about a `NodeRun` via some `lhctl` commands:

* `lhctl list nodeRun <wfRunId>`: shows all `NodeRun`'s from a `WfRun`.
* `lhctl get nodeRun <wfRunId> <x> <y>`: retrieves the `y`th `NodeRun` from the `x`th `ThreadRun` in the specified `WfRun`.

### Variables

Just as a program or function can store state in variables, a `WfRun` can store state in `Variable`s as well. Variables in LittleHorse are defined in the `ThreadSpec`, and as such are scoped to a `ThreadRun`. Note that a child `ThreadRun` may access the variables of its parents.

Like `NodeRun`'s, a `Variable` is an object that you can fetch from the LittleHorse API. A `Variable` is uniquely identified by a [`VariableId`](../08-api.md#variableid), which has three fields:
1. The `wf_run_id`, which is the ID of the associated `WfRun`.
2. The `thread_run_number`, which is the ID of the associated `ThreadRun` (since a `Variable` lives within a specific `ThreadRun`).
3. The `name`, which is the name of the `Variable`.

You can fetch `Variable`s using [`rpc GetVariable`](../08-api.md#getvariable), [`rpc SearchVariable`](../08-api.md#searchvariable), and [`rpc ListVariables`](../08-api.md#listvariables).

Variables can be of certain types, which you can find in the [`VariableType`](../08-api.md#variabletype) enum.

## `WfRun` Structure


## `WfRun`

A `WfRun`, short for Workflow Run, is an instantiation of a `WfSpec`. Each `WfRun` consists of one or more `ThreadRun`s, which in turn contain multiple `NodeRun`s.

In the programming analogy, you could think of a `WfRun` as a process that is running your `WfSpec` program. A `ThreadRun` is a thread in that program.

Many simple `WfRun`s will have only one `ThreadRun`: the "entrypoint" thread. However, just as regular programs can spawn child threads to execute work in parallel, a LittleHorse `WfRun` might spawn child `ThreadRun`s.

A `WfRun`'s ID is simply a string. 

### Threading Model

In LittleHorse, a `ThreadSpec` is a sub-structure of a `WfSpec` that defines one sequential thread of execution. A `ThreadRun` is a run of a `ThreadSpec`, and it is a sub-structure of a `WfRun`.

`WfSpec`s and `WfRun`s are API Objects, meaning that they have ID's and can be directly retrieved from the LittleHorse API. In contrast, `ThreadSpec`s and `ThreadRun`s are NOT API objects; rather, they are sub-structures that can be retrieved from the API by querying their parents.

`ThreadSpec`s and `ThreadRun`s exhibit the same Metadata/Execution duality seen with `WfSpec`s and `WfRun`s.

Every `WfSpec` has one special `ThreadSpec` called the Entrypoint `ThreadSpec`. When a `WfSpec` is run (thus creating a `WfRun`), the first thing that starts is a `ThreadRun` specified by the Entrypoint `ThreadSpec`. You can think of the Entrypoint `ThreadSpec` as the `main()` function in a generic programming language.

And just like a thread in a normal program, a `ThreadRun` in LittleHorse can spawn child `ThreadRun`s.

### Lifecycle

The status of a `WfRun` is determined by looking at the status of the Entrypoint `ThreadRun`. A `ThreadRun`, and by extension a `WfRun`, can have one of the following statuses;

- `RUNNING`
- `HALTING`
- `HALTED`
- `ERROR`
- `COMPLETED`

A `ThreadRun` can be halted for any of the following reasons:

* If a `StopWfRun` request is received (manual halt by system administrator).
* When interrupted by an `ExternalEvent` which triggers an Interrupt Handler.
* If the `ThreadRun` is a child thread, and the parent `ThreadRun` is `HALTED`.

Note that halting a parent `ThreadRun` causes all of the children of that `ThreadRun` to be halted as well.

### Interruptibility

When a `ThreadRun` is halted (for example, via the `StopWfRun` grpc or when Interrupted), it moves to either the `HALTING` or `HALTED` state. If the `ThreadRun` is interruptible, it moves immediately to the `HALTED` state. Otherwise, it moves to the `HALTED` state as soon as it is interruptible.

The criteria for interruptibility are as follows:

- If the `ThreadRun` has any child threads, all children must be in the `COMPLETED`, `ERROR`, or `HALTED` state.
  - If this condition is not satisfied, then the runtime will halt all Children.
- There can be no `TaskRun`s that have been dispatched to a Task Worker but not completed, failed, or timed out. In other words, no in-flight tasks.

If a `WfRun` is waiting at an `EXTERNAL_EVENT`, `USER_TASK`, or `SLEEP` Node, the second condition is automatically satisfied.
