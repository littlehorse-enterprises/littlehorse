## Running StructDef Example [Experimental]

`StructDef`s allow you to define schemas for the data in your workflows.

This example will show you how to define a `StructDef` and use it in a workflow.

### Generating the `StructDef`

#### StructDef Classes

In this example, you will find three Java classes each representing their own StructDef: `Person`, `Address`, and `ParkingTicketReport`.

Note that each class is a POJO (Plain Old Java Object) with a simple `@LHStructDef` annotation attached to it.

In `Person`, the `homeAddress` field is marked with `@LHStructField(masked = true)`. This is a common privacy use case: your task workers still need the full address to deliver or mail something, but public API responses should not expose that sensitive PII. 

#### Task Workers

In `MyWorker.java`, you will find a Task Method `get-car-owner` which takes in a `ParkingTicketReport` as its first parameter and returns the `Person` who owns the car.

You will also find the Task Method `mail-ticket` which takes in a `Person` as its first parameter.

See how we register these `StructDef`s in the `StructDefExample#registerStructDef()` method.

### Register our `StructDef`s

Let's run the `StructDefExample.java` application to:
* Register your `StructDef`s
* Register your `TaskDef`s

```
./gradlew example-struct-def:run
```

In another terminal, call the following command to verify that the `Person` StructDef was created on the server:

```
lhctl get structdef person 0
```

> [!NOTE]
> You can try this with any of the StructDefs we created, just make sure you use the name identified in the `@LHStructDef` annotation!

### Running a workflow

Let's run the `StructDefExample.java` application again to:
* Run our workflow
* Pass in a `Struct` object to the workflow as an input variable

In a separate terminal, we'll run the same command again but this time with some arguments that we'll pass into our Workflow.

```
./gradlew example-struct-def:run --args 'BARC Speeder 1HGCM82633A004352'
```

To showcase nullable Struct fields, run with a plate that starts with `NOADDR`:

```
./gradlew example-struct-def:run --args 'Starfighter Naboo NOADDR-42'
```

In this branch, `MyWorker#getCarOwner` returns a `Person` with `homeAddress = null`.
The `Person.homeAddress` field is declared as nullable via `@LHStructField(isNullable = true)`.

You can check the results of this workflow run using `lhctl`:

```
# This call shows the result
lhctl get wfRun <wf_run_id>

# This will show you all nodes in the run
lhctl list nodeRun <wf_run_id>

# This shows the task run information
lhctl get taskRun <wf_run_id> <task_run_global_id>
```

## Coming Soon

Additional `StructDef` functionality and demos are coming soon, including:
- Examples of `StructDef` evolution
- Examples of `Struct` validation failures
- `lhctl` support for passing Structs as input variables when running workflows