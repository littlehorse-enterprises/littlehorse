## Running SagaExample

LH support advanced microservices patterns like Sagas.
Here you are going to be able to see how to define a workflow for a saga transaction.

Let's run the example in `SagaExample.java`

```
./gradlew example-saga:run
```

In another terminal, use `lhctl` to run the workflow:

```
lhctl run example-saga
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
