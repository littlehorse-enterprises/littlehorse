# Go Quickstart

This quickstart models a know-your-customer workflow:

1. Run `verify-identity`.
2. Wait for a correlated `identity-verified` event.
3. Notify the customer whether they were verified.

## Start the quickstart

From the repository root:

```bash
go run ./examples/go/quickstart
```

That single command registers the `TaskDef`s, the `ExternalEventDef`, and the `WfSpec`, then starts the task workers. Leave it running.

## Run a workflow

In another terminal:

```bash
lhctl run quickstart full-name 'Obi-Wan Kenobi' email obiwan@jedi.temple ssn 123456789
```

## Complete the waiting event

Post the correlated event to move the workflow forward:

```bash
lhctl put correlatedEvent obiwan@jedi.temple identity-verified BOOL true
```

Use `BOOL false` to drive the rejection branch instead.

## Inspect the result

```bash
lhctl get wfRun <wf_run_id>
lhctl list nodeRun <wf_run_id>
lhctl list taskRun <wf_run_id>
```
