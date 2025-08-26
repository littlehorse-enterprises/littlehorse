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

In the `Person.java` file, you will fine a Java class `Person` with the `@LHStructDef` annotation.

In `MyWorker.java`, you will find a Task Method `greet` which takes in a `Person` as its first parameter.

Once initialized, your Task Worker will automatically recognize that a `StructDef` is being used in your task method, allowing you to perform additional operations like `register` or `validate` your `StructDef`s.

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