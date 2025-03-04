## Running ChildThreadExample

In this example you will see how to instantiate a child thread
and then wait until it has finished its execution before
executing another task.

We will use the thread.SpawnThread() function for that.

```
public SpawnedThread SpawnThread(
    Action<WorkflowThread> threadFunc, 
    string threadName, 
    Dictionary<string, object>? inputVars
);
```

Let's run the example in `ChildThreadExample`

```
dotnet run
```

In another terminal, use `lhctl` to run the workflow:

```
# Here, we specify that the "parent-var" variable = 2
lhctl run example-child-thread parent-var 2
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
