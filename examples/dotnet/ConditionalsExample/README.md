## Running ConditionalsExample

In this example you will see how to use conditionals.
It will execute an "if" or "else" depending on the value of "bar".
If bar is greater than 10 then execute task-b else execute task-c.

Let's run the example in `ConditionalsExample`

```
dotnet build
dotnet run
```

In another terminal, use `lhctl` to run the workflow:

```
# Execute task-b
lhctl run example-conditionals foo '{"bar": 15}'

# Execute task-c
lhctl run example-conditionals foo '{"bar": 5}'
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
