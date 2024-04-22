## Running an example with task retries

This example shows how to configure retries in case of error.

Let's run the example:

```
poetry shell
python -m retries
```

In another terminal, use `lhctl` to run the workflow:

```
lhctl run example-retries start-timestamp $(date +%s)
```

Verify status:

```
lhctl get wfRun <wf run id>
```
