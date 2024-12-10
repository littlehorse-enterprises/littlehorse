## Running WorkerContextExample

This example shows how to get access to the context when executing a task.
Go to the class `MyWorker`.

Let's run the example in `WorkerContextExample`

```
dotnet build
dotnet run
```

In another terminal, use `lhctl` to run the workflow:

```
lhctl run example-worker-context
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

## Considerations

If you need get access to the context you have to add a `LHWorkerContext`
parameter to the signature of your task:

```
    [LHTaskMethod("task")]
    public void ProcessTask(long requestTime, LHWorkerContext context)
    {
        ...
    }
```

The `WorkerContext` should be the last parameter.