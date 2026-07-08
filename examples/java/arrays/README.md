## Running ArrayExample

This example demonstrates using LittleHorse typed Arrays. It takes an `Array<INT>` as input, merges it with another Array produced by the `produce-array` task using `EXTEND` (native Array concatenation), and passes the merged Array to the `consume-array` task.

Run the example Java app:

```
./gradlew example-arrays:run
```

In another terminal, start a workflow run with `lhctl` with the input variable `my-array` and an Array value:

The `my-array` input variable is a typed `Array<INT>`. Provide it as a JSON array; the elements are coerced to the declared element type. The workflow then appends `[1, 2, 3` (from `produce-array`) to it:

Input [10, 20, 30] is merged with [1, 2, 3] => [10, 20, 30, 1, 2, 3]
```
lhctl run example-arrays my-array '[10, 20, 30]'
```

`my-array` has a default value, so you can also run it with no input:


Uses the default [1, 2, 3], merged with [1, 2, 3] => [1, 2, 3, 1, 2, 3]
```
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
