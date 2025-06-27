## Running BasicExample

This is a simple example, which does two things:

1. Declare an "input-name" variable of type String
2. Pass that variable into the execution of the "greet" task.

Let's run the example in `BasicExample.java`

```
./gradlew example-basic:run
```

In another terminal, use `lhctl` to run the workflow:

```
# The "input-name" variable here is treated as null
lhctl run example-basic

# Here, we specify that the "input-name" variable = "Obi-Wan"
lhctl run example-basic input-name Obi-Wan

# This call fails since there is no defined "foo" variable
lhctl run example-basic foo bar
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
