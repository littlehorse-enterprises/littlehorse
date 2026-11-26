## Running EnumExample

This example demonstrates using LittleHorse inline Enums. It takes a ticket `summary` as input, and the `classify-priority` task returns a `Priority` Enum. The workflow assigns the output to the `priority` variable. If `priority` is greater than `MEDIUM`, the workflow passes the Enum to the `page-on-call` task. If not, it passes the member name (from `priority.name()`) as a `STR` to the `add-to-queue` task.

LittleHorse compares Enum members by their backing values. `LOW` is 0 and `MEDIUM` is 1. `HIGH` has the explicit backing value 10 (from `@LHEnumValueDef(10)`), so `CRITICAL` is 11. A task parameter or return type is an Enum only with `@LHType(isInlineEnum = true)`; without it, the type is `JSON_OBJ`.

Run the example Java app:

```
./gradlew example-enums:run
```

In another terminal, start a workflow run with `lhctl` with the input variable `summary`:

The summary contains "down", so the priority is CRITICAL (11) and the workflow pages the on-call engineer
```
lhctl run example-enums summary "The checkout service is down"
```

The summary contains "slow", so the priority is MEDIUM (1) and the workflow adds the ticket to the queue
```
lhctl run example-enums summary "The dashboard is slow"
```

Inspect the run and task/node outputs:

```
# Show the workflow run
lhctl get wfRun <wf_run_id>

# List all node runs for the workflow
lhctl list nodeRun <wf_run_id>

# Show task run details
lhctl get taskRun <wf_run_id> <task_run_global_id>
```
