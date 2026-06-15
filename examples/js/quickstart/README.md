# JavaScript Quickstart

A quickstart example that demonstrates using the LittleHorse JavaScript SDK to implement the KYC (Know Your Customer) identity verification workflow.

The workflow:
1. Executes a `verify-identity` task (with 3 retries) to kick off identity verification.
2. Waits up to 5 minutes for an `identity-verified` correlated event (keyed by email).
3. If the event arrives in time, notifies the customer whether they were verified or not.
4. If the event times out, notifies the customer they could not be verified and fails the run.

## Prerequisites

- Node.js >= 18
- A running LittleHorse server on `localhost:2023` (see [local-dev/README.md](../../local-dev/README.md))
- Build the SDK first: `cd ../../sdk-js && npm install && npm run build`

## Setup

```bash
npm install
```

## Start the Task Workers

In one terminal, start the task workers (this also registers the required `TaskDef`s):

```bash
npm start
```

This will:
1. Register the `verify-identity`, `notify-customer-verified`, and `notify-customer-not-verified` TaskDefs if they don't exist.
2. Start polling for tasks from the LH Server.

## Register the WfSpec

The JS SDK does not yet support creating WfSpecs programmatically. Once the workers are running (and the TaskDefs are registered), deploy the `ExternalEventDef` and then the `WfSpec` with `lhctl` in another terminal:

```bash
lhctl deploy externalEventDef identity-verified-external-event-def.json
lhctl deploy wfSpec quickstart-wfspec.json
```

## Run a Workflow

In another terminal, run the quickstart workflow:

```bash
# With defaults (Obi-Wan Kenobi, obiwan@jedi.temple, 123456789)
npm run run-wf

# With custom arguments: [full-name] [email] [ssn]
npm run run-wf -- "Luke Skywalker" "luke@jedi.temple" 987654321
```

This will:
1. Start a `quickstart` WfRun with the provided input variables.
2. Wait 3 seconds for the `verify-identity` task to complete.
3. Post a `CorrelatedEvent` (`identity-verified = true`) to unblock the workflow.

## Check Results

```bash
lhctl get wfRun <wfRunId>
lhctl list nodeRun <wfRunId>
lhctl list taskRun <wfRunId>
```

Or navigate to the dashboard at [http://localhost:8080](http://localhost:8080).

## Workflow Graph

```
[ENTRYPOINT]
     |
[verify-identity]  (3 retries, 60s timeout)
     |
[wait: identity-verified event]  (correlated by email, 5 min timeout)
     |
  (if true)----[notify-customer-verified]----[EXIT]
  (if false)---[notify-customer-not-verified]-[EXIT]
     |
  (on timeout)-[notify-customer-not-verified]-[FAIL: customer-not-verified]
```
