## Running the `ParallelApprovalExample.java` Example

In this example you will see the poser of the thread.waitForThreads feature.
When multiples thread are running in parallel and you need to wait for all of them to finish,
it is possible to use thread.waitForThreads.

Let's run the example in `ParallelApprovalExample.java`

```
gradle example-parallel-approval:run
```

In another terminal, use `lhctl` to run the workflow:

```
# Run the workflow
lhctl run parallel-approval

# Note that is RUNNING
lhctl get wfRun <wf run id>
```

In addition, you can check the result with:

```
# This call shows the result
lhctl get wfRun <wf run id>

# This will show you all nodes in tha run
lhctl list nodeRun <wf_run_id>

# This shows the task run information
lhctl get taskRun <wf_run_id> <task_run_global_id>
```
