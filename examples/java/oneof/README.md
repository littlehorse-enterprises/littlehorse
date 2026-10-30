## Running OneOfExample

This example passes a payment to two tasks: one uses oneof annotations on individual
fields, and the other uses a separate `PaymentMethod` type. Both read the same payment.

A running LittleHorse server with oneof support is required.

Register the workflow and start the workers:

```sh
./gradlew example-oneof:run
```

In another terminal, submit a payment using either Java model:

```sh
# Annotations on individual fields
./gradlew example-oneof:run --args='fields'

# Separate PaymentMethod type
./gradlew example-oneof:run --args='helper'
```

Both tasks return `Order order-123 uses a card`. Use the printed WfRun ID to inspect the results:

```sh
lhctl get wfRun <wf_run_id>
lhctl list nodeRun <wf_run_id>
lhctl get taskRun <wf_run_id> <task_run_global_id>
```
