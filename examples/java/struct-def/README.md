## Running StructDef Example [Experimental]

`StructDef`s allow you to define schemas for the data in your workflows.

This example will show you how to define a `StructDef` and use it in a workflow.

### Setting your environment

`StructDef`s are unreleased and experimental, therefore you must enable them on your LittleHorse Server before you can make any RPC calls involving them.

You can enable `StructDef`s by setting the following environment variable in your LittleHorse Server environment:

```
LHS_X_ENABLE_STRUCT_DEFS=true
```

### Generating the `StructDef`

#### StructDef Classes

In this example, you will find three Java classes each representing their own StructDef: `Person`, `Address`, and `ParkingTicketReport`.

Note that each class is a POJO (Plain Old Java Object) with a simple `@LHStructDef` annotation attached to it.

#### Task Workers

In `MyWorker.java`, you will find a Task Method `get-car-owner` which takes in a `ParkingTicketReport` as its first parameter and returns the `Person` who owns the car.

You will alos find the Task Method `mail-ticket` which takes in a `Person` as its first parameter.

Once initialized, your Task Worker will automatically recognize these `StructDef` classes in your task method definitions, allowing you to perform additional operations like `register` or `validate` your `StructDef`s.

### Running the example

Let's run the `StructDefExample.java` application to:
* Register your `StructDef`s
* Register your `TaskDef`s

```
./gradlew example-struct-def:run
```

Now, you can see your `person` `StructDef` on the server by making an `RPC GetStructDef` call.

More functionality is coming soon, including:
* `Struct` variables in Workflows
* Receiving `Struct`s in your Task Methods
* `lhctl` commands for interacting with `StructDef`s