# Child Threads

As discussed in the [`WfRun` documentation](./01-workflows.md), a `WfRun` can have multiple `ThreadRun`s. The main thread is called the Entrypoint Thread, and all other threads are children (or grandchildren) of the entrypoint.

## Entrypoint Thread

A `WfSpec` defines one or more `ThreadSpec`s, and the corresponding `WfRun` has one or more `ThreadRun`s. Each `ThreadRun` has a corresponding `ThreadSpec` in the `WfSpec`.

In every `WfSpec`, one `ThreadSpec` is special: it is the `ENTRYPOINT` thread. Similarly, each `WfRun` has a special `ENTRYPOINT` thread run.

When you run a `WfSpec` (thereby creating a `WfRun`), the resulting `WfRun` is created with one entrypoint `ThreadRun`, which (as you guessed) is specified by the entrypoint `ThreadSpec` of the `WfSpec`.

## Thread Types

A `WfRun` may have multiple `ThreadRun`s in it. `ThreadRun`s have four types:

- `ENTRYPOINT` threads, described above.
- `CHILD` thread, created explicitly via a `SPAWN_THREAD` node (in Java, for example, `WorkflowThread::spawnThread()`).
- `INTERRUPT` thread, triggered by an External Event.
- `FAILURE_HANDLER` threads, which are akin to exception handlers in programming.

All `ThreadRun`s other than the entrypoint thread will have a `parentThreadId`, referring to the thread that spawned it. In the case of an `INTERRUPT` thread, the parent thread is the thread that was interrupted; in the case of a `FAILURE_HANDLER` thread, the parent is the thread whose failure triggered the exception handler.

## Variable Scoping

A `ThreadSpec` can define `Variable`s (for example, through the `variableDefs` field in the JSON spec, or `WorkflowThread::adVariable()` in Java). When a `ThreadRun` is created, all defined `Variable`s are instantiated (either with input values or as `NULL`).

When a child `ThreadRun` of any type is started, it has _read and write_ access to its own `Variable`s, and all `Variable`s that its parent has access to (including the parent's parent, and so on).

Since a `ThreadRun` can have multiple children, the parent does _not_ have access to the variables of the children.
