# JavaScript Structs Example — Issue Parking Ticket

[`examples/java/struct-def`](../../../java/struct-def/README.md): StructDefs + `Workflow.create` for `issue-parking-ticket`.

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

```bash
npm start
```

```bash
npm run run-wf -- Toyota Camry ABC123
```

## Check Results

```bash
lhctl get wfRun <wf_run_id>
lhctl list nodeRun <wf_run_id>
lhctl get taskRun <wf_run_id> <task_run_global_id>
```
