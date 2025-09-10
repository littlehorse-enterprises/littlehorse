## Running a Conditionals Example

In this example you will see how to define a while loop with LH.
Use do_while to define the loop, the end condition and the handler (body) of the while.

Let's run the example:

```
poetry shell
python -m example_conditionals_while
```

In another terminal, use `lhctl` to run the workflow:

```
lhctl run example-conditionals-while number-of-donuts 5
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
