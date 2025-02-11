## Running MaskedFields Example

This is a simple example, that masks input params and output results in LH Task Methods.

Let's run the example in `MaskedFieldsExample`

```
dotnet build
dotnet run
```

In another terminal, use `lhctl` to run the workflow:

```
# The "masked-name" variable should mask the value
# And the input-name variable value will mantain the original plain text

lhctl run example-basic masked-name pii-info input-name foo
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
