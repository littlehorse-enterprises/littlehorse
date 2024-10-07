# External Events and Interrupts

:::tip
This section is not a Developer Guide; if you want to learn how to [create an `ExternalEventDef`](../05-developer-guide/09-grpc/05-managing-metadata.md#externaleventdef), [post an `ExternalEvent`](../05-developer-guide/09-grpc/15-posting-external-events.md), [wait for an `ExternalEvent` in a Workflow](../05-developer-guide/08-wfspec-development/04-external-events.md), or [register an Interrupt Handler](../05-developer-guide/08-wfspec-development/05-interrupts.md), please check the appropriate docs in our developer guide.

This section focuses on concepts.
:::

Workflows must be able to respond to inputs from the outside world in order to be truly useful and dynamic. One of the tools that LittleHorse provides which allows you to do that is _External Events._

In LittleHorse, an `ExternalEventDef` is a Metadata Object that defines some event or activity occuring outside of the LittleHorse `WfRun`. An `ExternalEvent` is an Execution Object that represents the occurrence of such an event. Common use-cases for an External Event would be encapsulating a webhook from github when a new branch is pushed, or representing an event from DocuSign that is fired when a document is completed.

An External Event can be recorded through the `PutExternalEvent` gRPC call. This can be accomplished using clients in any of our SDK's or through the use of `lhctl`. Future versions of LittleHorse will allow you to directly hook up a webhook or event streaming system (eg Kafka) to LittleHorse and send events in a hands-off manner.

## Motivation

Workflow engines aim to help automate business processes. Oftentimes, such business processes involve interacting with the outside world and _listening_ for things to happen outside of the workflow engine before making a decision about what to do next. That is precisely what an `ExternalEvent` is for: while a `TaskRun` allows a `WfRun` to change the outside world, an `ExternalEvent` allows a `WfRun` to react to the outside world. Some common use-cases are to:

- Integrate with asynchronous third-party API's.
- Interrupt a `WfRun` and cause some special handling logic to be executed.
- Wait for a person to sign a document in DocuSign.
- Wait for a customer to respond to a text message using a callback from the Twilio API.

## Structure in the API

Just as a `TaskRun` is an instance of a `TaskDef`, an [`ExternalEvent`](../08-api.md#externalevent) is an instance of an [`ExternalEventDef`](../08-api.md#externaleventdef).

The `ExternalEventDef` metadata object must be [created](../05-developer-guide/09-grpc/05-managing-metadata.md#externaleventdef) before you can [post an `ExternalEvent`](../05-developer-guide/09-grpc/15-posting-external-events.md). The `ExternalEventDef` currently only contains a `name` which denotes the type of the `ExternalEvent`.

### `ExternalEvent`

An `ExternalEvent` is created through the [`rpc PutExternalEvent`](../08-api.md#putexternalevent). Every `ExternalEvent` has a `WfRunId` associated with it, so an `ExternalEvent` is sent to a specific `WfRun` at creation time.

An `ExternalEvent` has a composite ID (see [`message ExternalEventId`](../08-api.md#externaleventid)) consisting of:

- The `wf_run_id` of the associated `WfRun`
- The `name` of the `ExternalEventDef`
- A `guid` which is unique to the

Why the `WfRunId`? An `ExternalEvent` is intended to affect the behavior of a `WfRun`; therefore, an `ExternalEvent` must be correlated to a specific `WfRun`.

`ExternalEvent`s have a payload (the field is called `content`) which is simply a [`VariableValue](../08-api.md#variablevalue).

## Using External Events

An `ExternalEvent` can be used in a `WfRun` in two ways:

1. An [`ExternalEventNode`](../05-developer-guide/08-wfspec-development/04-external-events.md) blocks a `ThreadRun` and waits until an `ExternalEvent` arrives before allowing the `ThreadRun` to continue.
2. An [Interrupt](../05-developer-guide/08-wfspec-development/05-interrupts.md) stops a `ThreadRun` execution, runs an "interrupt handler" `ThreadRun`, and then resumes the `ThreadRun` once the interrupt handler completes.

### `ExternalEvent` Nodes

One use for an `ExternalEvent` is the [`ExternalEventNode`](../08-api.md#externaleventnode) Node Type. When a `ThreadRun` reaches an `ExternalEventNode`, it will wait until an `ExternalEvent` of the specified type (and with the correct `wf_run_id`) arrives. The output of the `NodeRun` is simply the payload of the `ExternalEvent`.

An `EXTERNAL_EVENT` node can have a timeout configured; this means that if the `ExternalEvent` does arrive within X seconds after the `ThreadRun` arrives at the node, then the NodeRun will fail with a `TIMEOUT` exception.

If the `ExternalEvent` arrives before the `ThreadRun` reaches the `EXTERNAL_EVENT` Node, that's ok! The `ThreadRun` will immediately pick up the `ExternalEvent` and move on to the next `Node`.

An `ExternalEvent` gets correlated to one and only one `NodeRun`. When posting an event, you can optionally specify a `threadRunNumber` to ensure that the `ExternalEvent` can only be assigned to a `NodeRun` on that specific `ThreadRun`.

:::note
If you configure an `EXTERNAL_EVENT` node with the ExternalEventDef `foo`, you cannot use the `foo` external event def as an Interrupt Trigger elsewhere in the same `WfSpec`.
:::

### Interrupts

In C and C++, you can register a handler function to handle an OS signal: when the signal is caught, the program gets interrupted and the interrupt handler is executed. In LittleHorse, you can handle an `ExternalEvent` and trigger an Interrupt `ThreadRun`.

When an `ExternalEvent` is posted to a `WfRun`, and a `ThreadRun` has registered an Interrupt for that specific `ExternalEventDef`, then the affected `ThreadRun` is halted. Once the `ThreadRun` is halted, then a new Child `ThreadRun` (specifically, an Interrupt Handler) is created.

Once the Interrupt Handler (Child) completes, the Interrupted Thread (Parent) is resumed. If the Interrupt Handler fails, then the Parent also fails with a `CHILD_FAILED` exception. This error is unrecoverable.

## Scoping

Interrupts are registered at the `ThreadSpec` level. Only one `ThreadSpec` may register an Interrupt for a specific `ExternalEventDef`.

When a `ThreadRun` is Interrupted, it must first halt. As per the [WfRun Documentation](./01-workflows.md#lifecycle), a `ThreadRun` is not considered `HALTED` until all of its Children are `HALTED` as well. Therefore, interrupting a `ThreadRun` causes all of the Children of the Interrupted `ThreadRun` to halt as well.

The Interrupt Handler is a Child of the Interrupted Thread. Therefore, it has read/write access to all of the Interrupted Thread's `Variable`s.

##### Use Cases

Interrupts may be used for various reasons, such as to:

- Kill a running `WfRun` and perform some cleanup action (such as notifying a customer) before making the Interrupted Thread fail.
- Update the value of some `Variable` in a running `WfRun`, such as to change contact info or add items to a shopping cart.
- Send heartbeats from an external system and allow the `WfRun` to keep track of the last seen activity (such as when determining when to invalidate an access token due to inactivity).
