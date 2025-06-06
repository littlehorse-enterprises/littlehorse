## Running Async Task Workers Example

This is a simple example, which creates various tasks workers that return async Tasks


Let's run the example in `AsyncTaskWorkersExample`:

```
dotnet build
dotnet run
```

In another terminal, use `lhctl` to run the workflow:

```
lhctl run example-async-workers receiver pedro@testmail.com
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
