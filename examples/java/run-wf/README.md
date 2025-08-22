## Running RunWfExample.java

In This example you are going to learn how to request a wf run
programmatically using:

```
public String runWf(
   String wfSpecName,
   Integer wfSpecVersion,
   String wfRunId,
   Arg... args
)
```

Let's run the example in `RunWfExample.java`

```
./gradlew example-run-wf:run
```

Check the results with:

```
# This call shows the result
lhctl get wfRun <wf_run_id>

# This will show you all nodes in the run
lhctl list nodeRun <wf_run_id>

# This shows the task run information
lhctl get taskRun <wf_run_id> <task_run_global_id>
```
