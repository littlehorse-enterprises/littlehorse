# LittleHorse Java Examples

Java examples for LittleHorse workflow features.

## Prerequisites

- Running LittleHorse server (see [`../README.md`](../README.md)).
- `lhctl` installed and configured.
- Java 17+.
- Gradle (`./gradlew` from repo root works).

Verify server connectivity:

```bash
lhctl whoami
```

## Running Java examples

From the repository root: (substitute `{name}` for the name of the example you want to run)

```bash
./gradlew example-{name}:run
```

Most Java examples start a long-lived worker process. Keep that command running and use another terminal for `lhctl run ...`.

## Java Example Index

- [`await-workflow-event/`](./await-workflow-event/README.md): Wait for `WorkflowEvent`.
- [`basic/`](./basic/README.md): Minimal hello-world workflow.
- [`casting/`](./casting/README.md): Type conversion/casting behavior.
- [`checkpoint-tasks/`](./checkpoint-tasks/README.md): Checkpoint task usage.
- [`child-thread/`](./child-thread/README.md): Spawn and coordinate child threads.
- `child-workflow/`: Child workflow example source (no README yet).
- [`conditionals/`](./conditionals/README.md): `if/else` workflow logic.
- [`conditionals-while/`](./conditionals-while/README.md): `while` loops in workflows.
- [`correlated-event/`](./correlated-event/README.md): Correlate events into workflows.
- [`exception-handler/`](./exception-handler/README.md): Error handling paths.
- [`expressions/`](./expressions/README.md): Expressions and variable manipulation.
- [`external-event/`](./external-event/README.md): Wait for external events.
- [`hierarchical-workflow/`](./hierarchical-workflow/README.md): Hierarchical workflows.
- [`hundred-tasks/`](./hundred-tasks/README.md): High-volume task fanout example.
- [`interrupt-handler/`](./interrupt-handler/README.md): Interrupt handling patterns.
- [`json/`](./json/README.md): JSON variable examples.
- [`mutation/`](./mutation/README.md): Variable mutation behavior.
- [`output-topic/`](./output-topic/README.md): Output-topic event publishing.
- [`parallel-approval/`](./parallel-approval/README.md): Parallel approval flow.
- [`run-child-workflow/`](./run-child-workflow/README.md): Invoke child workflow.
- [`run-wf/`](./run-wf/README.md): Programmatic workflow-run operations.
- [`saga/`](./saga/README.md): Saga/compensation pattern.
- [`spawn-thread-foreach/`](./spawn-thread-foreach/README.md): Child-thread foreach fanout.
- [`struct-def/`](./struct-def/README.md): Struct definition and usage.
- [`timestamp/`](./timestamp/README.md): Timestamp handling.
- [`user-tasks/`](./user-tasks/README.md): Human `UserTask` flows.
- [`variables/`](./variables/README.md): Variable declaration and assignment.
- [`wait-for-condition/`](./wait-for-condition/README.md): Blocking until condition is met.
- [`wait-for-one-of/`](./wait-for-one-of/README.md): Wait for one of several outcomes.
- [`worker-context/`](./worker-context/README.md): Access worker context metadata.
