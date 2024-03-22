## Running Health Status Example

This is a simple example, which does:

1. Declare an "input-name" variable of type String
2. Pass that variable into the execution of the "greet" task.
3. Shows the cluster's health status.

Let's run the example in `HealthStatus.java`

```
poetry shell
python -m example_health_status
```

In another terminal, use `lhctl` to run the workflow:

```
# The "input-name" variable here is treated as null
lhctl run health-status

# Here, we specify that the "input-name" variable = "Anakin"
lhctl run health-status input-name Anakin

# This call fails since there is no defined "foo" variable
lhctl run health-status foo bar
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
