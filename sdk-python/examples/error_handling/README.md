## Running with an Error Handling

This example demonstrates the error handling functionality.
We will use `thread.handle_error` to handle any task failure and completes the WfRun execution.
It execute "handler" task as a error handler.

You can use the `thread.handle_exception`, `thread.handle_error` and `thread.handle_any_failure` methods to elegantly handle
the exceptions.

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
lhctl get wfRun <wf run id>
```

There are two types of failure:

- Exceptions: represents a "business process failure". You can use `thread.handle_exception` for this type of failure.
- Error: represents a "technical error". You can use `thread.handle_error` for this type of failure.
