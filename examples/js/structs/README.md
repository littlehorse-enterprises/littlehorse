# JavaScript Structs Example — Issue Parking Ticket

Mirrors the Java `struct-def` example. Demonstrates using the LittleHorse JavaScript
SDK with **StructDefs**, including nested structs (`Person` → `Address`).

This example:
1. Registers three StructDefs: `address`, `person` (with nested `address`), and `car`
2. Registers two TaskDefs: `get-car-owner` (returns a `Person`) and `mail-ticket`
3. Starts task workers that process the `issue-parking-ticket` workflow

## Prerequisites

- Node.js >= 18
- A running LittleHorse server on `localhost:2023` (see [local-dev/README.md](../../../local-dev/README.md))
- `lhctl` installed (`cd ../../../lhctl && go install .`)
- Build the SDK first: `cd ../../../sdk-js && npm install && npm run build`

## Setup

```bash
npm install
```

## Run

### 1. Start the Task Workers

```bash
npm start
```

This registers the StructDefs (`address`, `person`, `car`), the TaskDefs
(`get-car-owner`, `mail-ticket`), and starts polling for tasks.

### 2. Deploy the WfSpec

In another terminal:

```bash
lhctl deploy issue-parking-ticket-wfspec.json
```

### 3. Run a Workflow
r
```bash
npm run run-wf -- Toyota Camry ABC123
```

This creates a `ParkingTicketReport` and runs the `issue-parking-ticket` workflow.

## Check Results

```bash
lhctl get wfRun <wf_run_id>
lhctl list nodeRun <wf_run_id>
lhctl get taskRun <wf_run_id> <task_run_global_id>
```
