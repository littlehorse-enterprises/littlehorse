# StructDef Example

This example demonstrates how to define and use `StructDef`s in LittleHorse using Go.

## What's Going On

The file [workflow.go](./workflow.go) defines:

- Three Go structs (`Address`, `Person`, `ParkingTicketReport`) each implementing the `LHStructDef()` method to identify them as LittleHorse StructDefs.
- Two Task Functions: `GetCarOwner` (takes a `ParkingTicketReport`, returns a `Person`) and `MailTicket` (takes a `Person`).
- `MyWorkflow`, the Workflow Function that uses `DeclareStruct` to declare struct-typed variables.

## Deploy the Task Worker

Before we can create the `WfSpec`, we need to register the `TaskDef`s. Run the task worker:

```
go run ./examples/go/structdef/worker
```

Leave that process running.

## Register the `WfSpec`

In another terminal, register the StructDefs and WfSpec:

```
go run ./examples/go/structdef/deploy
```

This will register the `address`, `person`, and `car` StructDefs, then create the `WfSpec`. Verify with:

```
lhctl get structDef person 0
lhctl get wfSpec issue-parking-ticket
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
