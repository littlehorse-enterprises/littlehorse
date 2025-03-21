## Running JsonExample

This workflow demonstrates the ability of the Java SDK to serialize/deserialize
JSON and allow Task developers to interact with real Java objects.
More information about json-path at https://github.com/json-path/JsonPath.

Let's run the example in `JsonExample.java`

```
./gradlew example-json:run
```

In another terminal, use `lhctl` to run the workflow:

```
lhctl run example-json person '{"name": "Obi-Wan", "car": {"brand": "Ford", "model": "Escape"}}'
```

In addition, you can check the result with:

```
# This call shows the result
lhctl get wfRun <wf_run_id>

# This will show you all nodes in the run
lhctl list nodeRun <wf_run_id>

# This shows the task run information
lhctl get taskRun <wf_run_id> <task_run_global_id>
```
