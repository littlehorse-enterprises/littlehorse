## Running SpawnParallelThreadsFromJsonArrVariableExample

In this example you will see how to spawn multiples threads base on a INPUT json array.

Let's run the example in `ConditionalsWhileExample.java`

```
gradle example-spawn-parallel-threads-from-json-arr-variable:run
```

In another terminal, use `lhctl` to run the workflow:

```
lhctl run spawn-parallel-threads-from-json-arr-variable approval-chain '{"description": "demo for approvals", "approvals":  [{"user": "yoda"}, {"user": "chewbacca"}, {"user": "anakin"}]}'
```

In addition, you can check the result with:

```
# This call shows the result
lhctl get wfRun <wf_run_id>

# This will show you all nodes in tha run
lhctl list nodeRun <wf_run_id>

# This shows the task run information
lhctl get taskRun <wf_run_id> <task_run_global_id>
```
