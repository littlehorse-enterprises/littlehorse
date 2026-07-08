## Running MapExample

This example demonstrates using LittleHorse typed Maps.

Run the example Java app:

```
./gradlew example-maps:run
```

In another terminal, start a workflow run with `lhctl`.

The `my-map` input variable is a typed `Map<STR, INT>`. Provide it as a JSON object; the
keys and values are coerced to the declared key/value types:

```
# Override the map input with your own entries
lhctl run example-maps my-map '{"apples": 10, "grapes": 7}'
```

`my-map` has a default value, so you can also run it with no input:

```
# Uses the default map: {"apples": 3, "bananas": 5, "cherries": 12}
lhctl run example-maps
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
