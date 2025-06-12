## Running ExceptionHandlerExample

This is a simple demonstration of a workflow that handles the failure of a task with
the handleException() functionality, which spawns a child thread and then
resumes execution when the handler thread completes.

Let's run the example in `ExceptionHandlerExample.java`

```
./gradlew example-exception-handler:run
```

In another terminal, use `lhctl` to run the workflow:

```
lhctl run example-exception-handler
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
