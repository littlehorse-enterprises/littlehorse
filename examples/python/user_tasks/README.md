## Running a User Tasks Example

This is a simple example of user tasks usage scheduling a reminder task before the user task is completed:

Let's run the example:

```
poetry shell
python ./examples/user_tasks/user_tasks.py
```

In another terminal, use `lhctl` to run the workflow:

```
lhctl run example-user-tasks
```

When you run the example-user-tasks workflow, the taskworker should call the `greet` task method and print
the following message:

```
Hello Sam!. WfRun {{wfRunId}} Person: {'identification': '1258796641-4', 'Address': 'NA-Street', 'Age': 28}
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
