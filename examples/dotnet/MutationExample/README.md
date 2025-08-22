## Running MutationExample

In this example you will see how to mutate variables.

Let's run the example in `MutationExample`

```
dotnet build
dotnet run
```

In another terminal, use `lhctl` to run the workflow:

```
# Execute with Peter or Miles
lhctl run example-mutation name Peter

# Execute with other names
lhctl run example-mutation name Pepito
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
