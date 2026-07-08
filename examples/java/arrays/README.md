## Running ArrayExample

This example demonstrates using LittleHorse typed Arrays. It takes in a required input variable `my-array`, which is typed as an Array of `Long`s (64-bit integers). It concatenates the input Array with an Array produced by another task, then passes the result into a new task.

Run the example Java app:

```
./gradlew example-arrays:run
```

In another terminal, start a workflow run with `lhctl` with the input variable `my-array` and an Array value:

```
lhctl run example-arrays my-array '[1,2,3]'
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
