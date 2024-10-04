# Workflow Events

While [External Events](./04-external-events.md) enable workflows to respond to events from the outside world, some applications may need to respond to events thrown from _within_ workflows.  _Workflow Events_ enable LittleHorse Clients to wait for a specific event to occur within a workflow. 

In LittleHorse, a `WorkflowEventDef` is a Metadata Object that defines an event or action that occurs within the context of a LittleHorse `WfRun`. A `WorkflowEvent` is an Execution Object that signifies the occurrence of such an internal event.

An External Event can be recorded through the execution of a [`THROW_EVENT` Node](../08-api.md#throweventnode) within a Workflow.  LittleHorse Clients can await External Events using [`RPC AwaitWorkflowEvent`](../08-api.md#awaitworkflowevent). 

:::tip
Think of Workflow Events as the opposite of External Events:
- External Events come from external sources. They allow LittleHorse to await events that happen outside a `WfRun` .
- Workflow Events come from within workflows. They allow LittleHorse Clients to await events that happen inside a `WfRun`.
:::