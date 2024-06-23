---
sidebar_label: External Events
---
# `ExternalEventDef` and `ExternalEvent`

In LittleHorse, an `ExternalEventDef` is a Metadata Object that defines some event or activity occuring outside of the LittleHorse `WfRun`. An `ExternalEvent` is an Execution Object that represents the occurrence of such an event. Common use-cases for an External Event would be encapsulating a webhook from github when a new branch is pushed, or representing an event from DocuSign that is fired when a document is completed.

An External Event can be recorded through the `PutExternalEvent` gRPC call. This can be accomplished using clients in any of our SDK's or through the use of `lhctl`. Future versions of LittleHorse will allow you to directly hook up a webhook or event streaming system (eg Kafka) to LittleHorse and send events in a hands-off manner.

## Motivation

Workflow engines aim to help automate business processes. Oftentimes, such business processes involve interacting with the outside world and _listening_ for things to happen outside of the workflow engine before making a decision about what to do next. That is precisely what an `ExternalEvent` is for: while a `TaskRun` allows a `WfRun` to change the outside world, an `ExternalEvent` allows a `WfRun` to react to the outside world. Some common use-cases are to:

- Integrate with asynchronous third-party API's.
- Trigger [Interrupts](./11-interrupts.md).
- Wait for a person to sign a document in DocuSign.
- Wait for a customer to respond to a text message using a callback from the Twilio API.

## `ExternalEvent` Structure

An `ExternalEvent` has a composite ID consisting of:

- The `wfRunId` of the associated `WfRun`
- The `name` of the `ExternalEventDef`
- A `guid` which is unique to the

Why the `wfRunId`? An `ExternalEven` is intended to affect the behavior of a `WfRun`; therefore, an `ExternalEvent` must be correlated to a specific `WfRun`.

`ExternalEvent`s have a payload which is simply a `VariableValue`.

## `ExternalEventDef` Structure

There is an `ExternalEventDef` API Resource. The relationship between an `ExternalEventDef` and an `ExternalEvent` is the same as the relationship between a `WfSpec` and a `WfRun`.

Currently, the only field in an `ExternalEventDef` is the name of the event type. The event name is used to isolate events of different types, for example `"document-signed"` and `"document-rejected"` events.

In future versions of LittleHorse, the `ExternalEventDef` will have more information:

- An optional schema for the data type to enable type checking in the `WfSpec`.
- Information on how to correlate an `ExternalEvent` to a `WfRun` without explicitly setting the `wfRunId`.

## `ExternalEvent` Nodes

One use for an `ExternalEvent` is the `EXTERNAL_EVENT` Node Type. When a `ThreadRun` reaches an `EXTERNAL_EVENT` node, it will halt until an `ExternalEvent` of the specified type (and with the correct `wfRunId`) arrives. The output of the `NodeRun` is simply the payload of the `ExternalEvent`.

An `EXTERNAL_EVENT` node can have a timeout configured; this means that if the `ExternalEvent` does arrive within X seconds after the `ThreadRun` arrives at the node, then the NodeRun will fail with a `TIMEOUT` exception.

If the `ExternalEvent` arrives before the `ThreadRun` reaches the `EXTERNAL_EVENT` Node, that's ok! The `ThreadRun` will immediately pick up the `ExternalEvent` and move on to the next `Node`.

An `ExternalEvent` gets correlated to one and only one `NodeRun`. When posting an event, you can optionally specify a `threadRunNumber` to ensure that the `ExternalEvent` can only be assigned to a `NodeRun` on that specific `ThreadRun`.

:::info
An `ExternalEvent` can also be used to trigger an Interrupt in LittleHorse. For more information, see the [Interrupt documentation](./11-interrupts.md).
:::

:::note
If you configure an `EXTERNAL_EVENT` node with the ExternalEventDef `foo`, you cannot use the `foo` external event def as an Interrupt Trigger elsewhere in the same `WfSpec`.
:::
