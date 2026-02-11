## Running StructDef Example [Experimental]

`StructDef`s allow you to define schemas for the data in your workflows.

This example will show you how to define a `StructDef` and use it in a workflow.

### Generating the `StructDef`

#### StructDef Classes

In this example, you will find three C# classes each representing their own StructDef: `Person`, `Address`, and `ParkingTicketReport`.

Note that each class is a POCO (Plain Old CLR Object) with a simple `[LHStructDef]` attribute attached to it.

#### Task Workers

In `MyWorker.cs`, you will find a Task Method `get-car-owner` which takes in a `ParkingTicketReport` as its first parameter and returns the `Person` who owns the car.

You will also find the Task Method `mail-ticket` which takes in a `Person` as its first parameter.

Once initialized, your Task Worker will automatically recognize these StructDef classes in your task method definitions, allowing you to perform additional operations like `register` or `validate` your StructDefs.

### Register our `StructDef`s

Run the application to:
* Register your StructDefs
* Register your TaskDefs
* Register your WfSpec
* Start the workers

```
dotnet build
dotnet run
```

In another terminal, call the following command to verify that the `Person` StructDef was created on the server:

```
lhctl get structdef person 0
```

> [!NOTE]
> You can try this with any of the StructDefs we created, just make sure you use the name identified in the `[LHStructDef]` attribute!

### Running a workflow

Run the application again with arguments to:
* Run our workflow
* Pass in a Struct object to the workflow as an input variable

```
dotnet run -- BARC Speeder 1HGCM82633A004352
```

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
