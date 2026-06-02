## Running ArrayExample

This example demonstrates using LittleHorse typed Arrays.

Run the example Java app:

```
./gradlew example-arrays:run
```

In another terminal, start a workflow run with `lhctl`:

```
# No input variables are required for this example
lhctl run example-arrays
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
