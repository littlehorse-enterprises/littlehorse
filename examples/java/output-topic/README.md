## Running Output Topic Example

This simple example demonstrates the Output Topic features. It:

1. Creates a `WfSpec` with a public variable and a private variable.
2. Runs a task worker for it.
3. Starts a Kafka Consumer that listens to the output topic and prints out all output topic records.

Let's run the example in `OutputTopicExample.java`

```
./gradlew example-output-topic:run
```

In another terminal, use `lhctl` to run the workflow:

```
# The "input-name" variable here is treated as null
lhctl run output-topic

# Here, we specify that the "input-name" variable = "Obi-Wan"
lhctl run output-topic input-name Obi-Wan
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

You'll see records printed to the output topics.
