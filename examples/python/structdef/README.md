## Running StructDef Example

`StructDef`s allow you to define schemas for the data in your workflows.

This example shows you how to define a `StructDef` and use it in a workflow.

### StructDef Classes

In this example, you will find three Python classes each representing their own StructDef: `Address`, `Person`, and `ParkingTicketReport`.

Each class is decorated with `@lh_struct_def` and uses standard Python type annotations for its fields. Field names are automatically converted from `snake_case` to `camelCase`. You can use `Annotated` with `LHStructField(masked=True)` to mark sensitive fields.

### Task Functions

- `get_car_owner` takes a `ParkingTicketReport` and returns the `Person` who owns the car.
- `mail_ticket` takes a `Person` and returns a confirmation string.

The SDK automatically serializes and deserializes struct-typed parameters and return values.

### Running the Example

Let's run the example to register `StructDef`s, `TaskDef`s, and the `WfSpec`, then start the task workers:

```
poetry shell
python -m example_structdef
```

In another terminal, verify the `StructDef`s were created:

```
lhctl get structDef person 0
```

> [!NOTE]
> You can try this with any of the StructDefs we created (`address`, `person`, `parking-ticket-report`).

### Running a Workflow

In a separate terminal, run the workflow with some input:

```
python -m example_structdef run Toyota Camry ABC-123
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
