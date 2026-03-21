## Running Json Example

This workflow demonstrates JSON variables and JsonPath when wiring data into tasks.
More information about json-path at https://github.com/json-path/JsonPath.

Let's run the example in `src/index.ts`

```
npm install
npm start
```

In another terminal, use `lhctl` to run the workflow:

```
lhctl run example-json person '{"name": "Obi-Wan", "car": {"brand": "Ford", "model": "Escape"}}'
```

Or:

```
npm run run-wf
npm run run-wf -- '{"name":"Obi-Wan","car":{"brand":"Ford","model":"Escape"}}'
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
