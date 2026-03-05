# LittleHorse .NET Examples

.NET SDK examples for LittleHorse workflow features.

## Prerequisites

- Running LittleHorse server (see [`../README.md`](../README.md)).
- `lhctl` installed and configured.
- .NET SDK installed.

Verify server connectivity:

```bash
lhctl whoami
```

## Running .NET examples

Example (`BasicExample`):

```bash
cd examples/dotnet/BasicExample
dotnet build
dotnet run
```

Use another terminal for `lhctl run ...` commands.

## .NET Example Index

- [`AwaitWorkflowEventExample/`](./AwaitWorkflowEventExample/README.md): Wait for workflow events.
- [`BasicExample/`](./BasicExample/README.md): Minimal hello-world workflow.
- [`CheckpointTasksExample/`](./CheckpointTasksExample/README.md): Checkpoint tasks.
- [`ChildThreadExample/`](./ChildThreadExample/README.md): Child-thread orchestration.
- [`ChildThreadsForeachExample/`](./ChildThreadsForeachExample/README.md): Foreach child-thread fanout.
- [`ConditionalsExample/`](./ConditionalsExample/README.md): `if/else` workflow logic.
- [`ConditionalsWhileExample/`](./ConditionalsWhileExample/README.md): `while` loops in workflows.
- [`CorrelatedEventExample/`](./CorrelatedEventExample/README.md): Correlated event handling.
- `EventRegistrationExample/`: Event registration example source (no README yet).
- [`ExceptionsHandlerExample/`](./ExceptionsHandlerExample/README.md): Error handling paths.
- [`ExternalEventExample/`](./ExternalEventExample/README.md): Wait for external events.
- [`InterruptHandlerExample/`](./InterruptHandlerExample/README.md): Interrupt handling.
- [`MaskedFieldsExample/`](./MaskedFieldsExample/README.md): Masked variable fields.
- [`MutationExample/`](./MutationExample/README.md): Variable mutation behavior.
- `RunChildWorkflow/`: Run-child-workflow example source (no README yet).
- [`StructDefExample/`](./StructDefExample/README.md): Struct definition and usage.
- [`UserTasksExample/`](./UserTasksExample/README.md): Human `UserTask` flows.
- [`WorkerContextExample/`](./WorkerContextExample/README.md): Worker context metadata.
