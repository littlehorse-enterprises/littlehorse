## Running ExpressioncExample

This is a simple example, which does two things:

1. Declare the variables `quantity`, `price`, and `taxes`,
2. Execute an Expression to calculate the total to pay. `quantity * (price * (1 + (taxes / 100)))`.

Let's run the example in `ExpressionsExample.java`

```sh
./gradlew example-expressions:run
```

In another terminal, use `lhctl` to run the workflow:

```sh
# Here, we specify that the "quantity" = 1, "price" = 0.8 and "taxes" = 12
lhctl run example-expressions quantity 1 price 0.8 taxes 12
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
