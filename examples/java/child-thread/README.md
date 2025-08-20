## Running ChildThreadExample

In this example you will see how to instantiate a child thread
and then wait until it has finished its execution before
executing another task.

We will use the thread.spawnThread() function for that.

```
public SpawnedThread spawnThread(
    ThreadFunc threadFunc,
    String threadName,
    Map<String, Object> inputVars
);
```

Let's run the example in `ChildThreadExample.java`

```
./gradlew example-child-thread:run
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
