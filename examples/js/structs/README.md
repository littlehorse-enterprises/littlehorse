## Running StructDef Example [Experimental]

`StructDef`s allow you to define schemas for the data in your workflows.

This example will show you how to define a `StructDef` and use it in a workflow.

### Struct schema modules

In this example, you will find TypeScript types (with Zod) for three StructDefs: `Person`, `Address`, and `ParkingTicketReport` in `src/schemas.ts`.

#### Task workers

In `src/index.ts`, the task `get-car-owner` takes a `ParkingTicketReport` as its first parameter and returns the `Person` who owns the car.

You will also find the task `mail-ticket` which takes a `Person` as its first parameter.

See how StructDefs are registered before the workflow is put to the server.

### Register our `StructDef`s

Let's run the application in `src/index.ts` to:

* Register your `StructDef`s
* Register your `TaskDef`s

```
npm install
npm start
```

In another terminal, call the following command to verify that the `Person` StructDef was created on the server:

```
lhctl get structdef person 0
```

> [!NOTE]
> You can try this with any of the StructDefs we created; use the name from the schema definition in `src/schemas.ts`.

### Running a workflow

Keep `npm start` running. In a separate terminal, pass make, model, and license plate into the workflow input struct:

```
npm run run-wf -- Toyota Camry ABC123
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

The workflow name registered by this example is `example-issue-parking-ticket`.
