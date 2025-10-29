## Running WaitForConditionExample

This is a wait for condition example, which does three things:
1. Declare an "counter" variable of type Integer
2. Waits until the counter reaches 0.
3. An interrupt handler which decrements the counter variable when an external event is received.

Let's run the example in `BasicWaitForConditionExampleExample.java`

```sh
./gradlew example-wait-for-condition:run
```

In another terminal, use `lhctl` to run the workflow:

```sh
lhctl run example-wait-for-condition counter 1
```

Then trigger the interrupt handler with

```sh
lhctl postEvent <wf run id> subtract
```

In addition, you can check the result with:

```sh
# This call shows the result
lhctl get wfRun <wf_run_id>

# This will show you all nodes in the run
lhctl list nodeRun <wf_run_id>
```
