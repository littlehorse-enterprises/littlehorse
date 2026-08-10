# LittleHorse JavaScript Examples

JavaScript/TypeScript SDK examples for LittleHorse.

Most of these define their `WfSpec` **in TypeScript with the wfsdk** and register
it from code — no checked-in JSON and no `lhctl deploy`. Each one registers its
own metadata, starts its worker(s), launches a `WfRun`, waits for it to finish,
and prints the result, so a single `npm start` is a complete round trip.

## Prerequisites

- A running LittleHorse server. The quickest option:

  ```bash
  docker run --rm -d -p 2023:2023 ghcr.io/littlehorse-enterprises/littlehorse/lh-standalone:master
  ```

- Node.js 20+.

## Running an example

```bash
cd examples/js/basic
npm install
npm start
```

Every example connects with `LHConfig.from({})`, which defaults to
`localhost:2023`. Point it elsewhere with `LHC_API_HOST` / `LHC_API_PORT`.

To watch what they produce, run the [dashboard](../../dashboard/README.md)
against the same server.

## Example index

### Workflow basics

- [`basic/`](./basic/README.md) — run a single task from a workflow.
- [`variables/`](./variables/README.md) — every variable type, with
  `required` / `searchable` / `masked` / default modifiers.
- [`expressions/`](./expressions/README.md) — arithmetic and comparison
  evaluated server-side.
- [`mutation/`](./mutation/README.md) — `ADD` / `SUBTRACT` / `MULTIPLY`
  variable mutations.
- [`json/`](./json/README.md) — reach into JSON variables with `jsonPath`.
- [`arrays-and-maps/`](./arrays-and-maps/README.md) — native typed `ARRAY` and
  `MAP` variables, including nested ones.

### Control flow

- [`conditionals/`](./conditionals/README.md) — `doIf` / `doElseIf` / `doElse`.
- [`conditionals-while/`](./conditionals-while/README.md) — `doWhile` loops.
- [`wait-for-condition/`](./wait-for-condition/README.md) — block until a
  variable satisfies a condition.

### Concurrency

- [`child-thread/`](./child-thread/README.md) — `spawnThread` and
  `waitForThreads`.
- [`spawn-thread-foreach/`](./spawn-thread-foreach/README.md) — one child
  thread per array element.
- [`child-workflow/`](./child-workflow/README.md) — run another `WfSpec` as a
  child and wait for it.

### Events and humans

- [`external-event/`](./external-event/README.md) — block on an `ExternalEvent`
  posted from outside.
- [`interrupt-handler/`](./interrupt-handler/README.md) — interrupt a running
  workflow at any point.
- [`workflow-event/`](./workflow-event/README.md) — emit a `WorkflowEvent` that
  outside systems can await.
- [`user-tasks/`](./user-tasks/README.md) — assign a UserTask and complete it
  the way a UI would.

### Failure handling

- [`exception-handler/`](./exception-handler/README.md) — business `EXCEPTION`
  vs technical `ERROR`, and how each is handled.
- [`saga/`](./saga/README.md) — compensate completed steps when a later step
  fails.
- [`worker-context/`](./worker-context/README.md) — `WorkerContext` ids,
  logging, and checkpoints that survive a retry.

### Earlier examples

These predate wfsdk support and register their `WfSpec` from checked-in JSON
with `lhctl deploy`:

- [`quickstart/`](./quickstart/README.md) — KYC identity verification
  quickstart (matches the Java/Go/Python quickstarts).
- [`simple-worker/`](./simple-worker/README.md) — basic task worker.
- [`structs/`](./structs/README.md) — struct-based worker and workflow.
