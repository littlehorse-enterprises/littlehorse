---
sidebar_label: Workflows
---

# `WfSpec` and `WfRun`

In the LittleHorse API, there are two types of Objects:

* Metadata objects, which are like blueprints.
* Execution objects, which are instantiations of Metadata objects.

You will find several instances of duality between a Metadata Object and an Execution Object. The `WfSpec`/`WfRun` duality is the first such instance of this duality.

## `WfSpec`

A `WfSpec`, short for Workflow Specification, is a metadata object that LittleHorse uses to define a blueprint for how a business process or technical process works. Essentially, the `WfSpec` contains business logic for a process. You can use the `RunWf` grpc call to instruct the LittleHorse server to run an instance of that `WfSpec`, thus creating a `WfRun`.

A `WfSpec` consists of one or more `ThreadSpec`s, which in turn contain a set of `Node`s and a set of edges between those `Node`s. A `Node` defines a unit of work (for example, "execute this computer task" or "wait for this callback to come in from an external system"), and the edges define the control flow in the `ThreadRun`.

In the programming analogy, you could think of a `WfSpec` as the actual source code for your program.

A `WfSpec` has a composite ID of a `name` and an integer `version`.

### `WfSpec` Versioning

A `WfSpec` is a versioned resource. Each `WfSpec` is uniquely identified by its `name` (a String), its `majorVersion` (an auto-incremented number, managed by LittleHorse), and its `revision` (another auto-incremented number).

When you create a `WfSpec` with the same `name` as another previous `WfSpec`, LittleHorse will either increment the `revision` (if there are no "breaking changes") or increment the `majorVersion` and set `revision` to zero (if there _are_ "breaking changes"). A "breaking change" in this regard is defined as changing either:

* The set of required input variables to the `WfSpec`, or
* The set of indexed searchable variables in the `WfSpec`.

When you run a `WfSpec` (thus creating a `WfRun`), you may optionally specify the version of the `WfSpec` that you wish to run. If you specify a specific version, then LittleHorse will run the `WfSpec` specified by the `RunWf` request. If no version number is provided, then `LittleHorse` will automatically run the latest version of the `WfSpec` with the provided name. For instructions on how this works in practice, please check out our [Metadata Management docs](/docs/developer-guide/grpc/managing-metadata).

This versioning scheme allows you to improve the business logic of your `WfSpec` without changing the client code that invokes the `WfSpec`: all your clients need to do is specify the `name` of their `WfSpec`, and the latest logic will be run transparently. Alternatively, you can "pin" your clients to run a specific version of your `WfSpec`.

Once a `WfRun` is launched with the `WfSpec` specified by (`name` "foo", `version` 123), the `WfRun` will always be associated with that specific version. In other words, deploying a new version of a `WfSpec` does not affect already-running `WfRun`s.

Future versions of LittleHorse will add an optional "on-the-fly" upgrade mechanism.


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
