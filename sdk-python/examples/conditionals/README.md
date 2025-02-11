## Running a Conditionals Example

In this example we are going to se how to use if/else conditionals in LH.

Let's run the example:

```
poetry shell
python -m example_conditionals
```

In another terminal, use `lhctl` to run the workflow:

```
lhctl run example-conditionals amount 200000
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
