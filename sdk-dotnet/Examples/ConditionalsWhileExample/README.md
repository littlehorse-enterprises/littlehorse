## Running ConditionalsWhileExample

In this example you will see how to define a while loop with LH.
Use DoWhile(WorkflowCondition condition, ThreadFunc whileBody) to define the loop,
the end condition and the handler (body) of the while.

Let's run the example in `ConditionalsWhileExample`

```
dotnet build
dotnet run
```

In another terminal, use `lhctl` to run the workflow:

```
lhctl run example-conditionals-while number-of-donuts 5
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
