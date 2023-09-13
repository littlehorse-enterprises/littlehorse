## Running with an External Event

This example demonstrates the error handling functionality.
We will use "thread.handle_error" to handle any task failure and completes the WfRun execution.
It execute "handler" task as a error handler

Let's run the example:

```
poetry shell
python -m example_error_handling
```

In another terminal, use `lhctl` to run the workflow:

```
# Start the workflow
lhctl run example-error-handling

# Then inspect the wfRun:
# Note it is 'COMPLETED'
lhctl get wfRun <wf run id>

# Then inspect the output of the ExternalEvent and how it completed the nodeRun:
lhctl get nodeRun <wf run id> 0 1
lhctl get taskRun <wf run id> <task runid>
```
