# StructDef Example

This example demonstrates how to define and use `StructDef`s in LittleHorse using Go.

## What's Going On

The file [workflow.go](./workflow.go) defines:

- Three Go structs (`Address`, `Person`, `ParkingTicketReport`) each implementing the `LHStructDef()` method to identify them as LittleHorse StructDefs.
- Two Task Functions: `GetCarOwner` (takes a `ParkingTicketReport`, returns a `Person`) and `MailTicket` (takes a `Person`).
- `MyWorkflow`, the Workflow Function that uses `DeclareStruct` to declare struct-typed variables.


## Deploy the Task Worker and `StructDef`s

Before we can create the `WfSpec`, we need to register our `TaskDef`s. And since our `TaskDef`s depend on our `StructDef`s, we need to register those too.

Run the task worker:

```
go run ./examples/go/structdef/worker
```

Leave that process running. This registered our `TaskDef`s and the `address`, `person`, and `parking-ticket-report` StructDefs.

Verify that the `StructDef`s were created in another terminal:

```
lhctl get structDef person 0
```

## Register the `WfSpec`

In another terminal, register the WfSpec:

```
go run ./examples/go/structdef/deploy
```

## Run a `WfRun`

In another terminal, run a workflow with default values:

```
go run ./examples/go/structdef/runworkflow
```

Or pass custom arguments (vehicleMake, vehicleModel, licensePlateNumber):

```
go run ./examples/go/structdef/runworkflow BARC Speeder 1HGCM82633A004352
```

Check the results:

```
lhctl get wfRun <the id from the previous step>
lhctl list nodeRun <wf_run_id>
```
