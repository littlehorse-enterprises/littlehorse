# LittleHorse Python Examples

Python SDK examples for LittleHorse workflow features.

## Prerequisites

- Running LittleHorse server (see [`../README.md`](../README.md)).
- `lhctl` installed and configured.
- Python + Poetry (recommended in this repo).

Verify server connectivity:

```bash
lhctl whoami
```

## Running Python examples

Example (`basic`):

```bash
cd examples/python/basic
poetry shell
python -m example_basic
```

Use another terminal for `lhctl run ...` when needed.

## Python Example Index

- [`basic/`](./basic/README.md): Minimal hello-world workflow.
- [`checkpoint_tasks/`](./checkpoint_tasks/README.md): Checkpoint task usage.
- [`child_threads/`](./child_threads/README.md): Spawn and coordinate child threads.
- [`conditionals/`](./conditionals/README.md): `if/else` workflow logic.
- [`conditionals_while/`](./conditionals_while/README.md): `while` loops in workflows.
- `correlated_event/`: Correlated event example source (no README yet).
- [`error_handling/`](./error_handling/README.md): Error and retry handling.
- [`event_registration/`](./event_registration/README.md): Event registration examples.
- [`external_event/`](./external_event/README.md): Wait for external events.
- [`hierarchical_workflow/`](./hierarchical_workflow/README.md): Hierarchical workflow patterns.
- [`interruption/`](./interruption/README.md): Interrupt handling behavior.
- [`json/`](./json/README.md): JSON variable examples.
- [`mutation/`](./mutation/README.md): Variable mutation behavior.
- [`parallel_approvals/`](./parallel_approvals/README.md): Parallel approval flow.
- [`retries/`](./retries/README.md): Retry policies and behavior.
- `run_child_wf/`: Run-child-workflow example source (no README yet).
- [`structdef/`](./structdef/README.md): Struct definition and usage.
- [`user_tasks/`](./user_tasks/README.md): Human `UserTask` flows.
- [`wait_for_condition/`](./wait_for_condition/README.md): Wait-for-condition pattern.
