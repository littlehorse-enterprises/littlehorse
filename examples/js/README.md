# LittleHorse JavaScript Examples

JavaScript/TypeScript SDK examples for LittleHorse.

Most of these define their `WfSpec` **in TypeScript with the wfsdk** and
register it from code: no checked-in JSON and no `lhctl deploy`. Each example
is a long-lived worker: `npm start` registers the metadata and polls for
tasks until you stop it, and you run workflows against it from another
terminal, the same shape as the Java examples.

## Prerequisites

- A running LittleHorse server; see [`../README.md`](../README.md) for the
  one-command standalone setup (it exposes the gRPC port 2023, the dashboard
  on 8080, and Kafka on 9092).
- Node.js 20+.

Examples read `~/.config/littlehorse.config` when it exists, else they
connect to `localhost:2023`. `LHC_API_HOST` / `LHC_API_PORT` environment
variables override both.

## Running an example

```bash
cd examples/js/basic
npm install
npm start
```

The worker prints the exact `lhctl run ...` command for its workflow. Run it
in another terminal, then inspect the result:

```bash
lhctl get wfRun <wf_run_id>
```

No `lhctl`? `npm run trigger` starts one run and prints the result. The
[dashboard](../../dashboard/README.md) shows everything the examples produce.

## Example index

### Workflow basics

- [`basic/`](./basic/README.md): run a single task from a workflow.
- [`variables/`](./variables/README.md): every variable type, with
  `required` / `searchable` / `masked` / default modifiers.
- [`expressions/`](./expressions/README.md): arithmetic and comparison
  evaluated server-side.
- [`mutation/`](./mutation/README.md): `ADD` / `SUBTRACT` / `MULTIPLY`
  variable mutations.
- [`json/`](./json/README.md): reach into JSON variables with `jsonPath`.
- [`arrays-and-maps/`](./arrays-and-maps/README.md): native typed `ARRAY` and
  `MAP` variables, including nested ones. Covers Java's separate `arrays`
  and `maps` examples; the split waits on native array and map task IO in
  the worker.
- [`struct-def/`](./struct-def/README.md): schema-checked `Struct` variables
  from `lhStruct` zod schemas, with masked and nullable fields.
- [`struct-builder/`](./struct-builder/README.md): assemble a `Struct` value
  node by node with `buildStruct`.
- [`timestamp/`](./timestamp/README.md): `TIMESTAMP` variables and the shapes
  a timestamp takes in a task worker.
- [`type-adapter/`](./type-adapter/README.md): teach the worker's serde a
  custom type with `config.addTypeAdapter`.
- [`run-wf/`](./run-wf/README.md): start workflow runs programmatically.
- [`hundred-tasks/`](./hundred-tasks/README.md): one hundred TaskRuns in a
  single WfRun, as a small stress demo.

### Control flow

- [`conditionals/`](./conditionals/README.md): `doIf` / `doElseIf` / `doElse`.
- [`conditionals-while/`](./conditionals-while/README.md): `doWhile` loops.
- [`wait-for-condition/`](./wait-for-condition/README.md): block until a
  variable satisfies a condition.

### Concurrency

- [`child-thread/`](./child-thread/README.md): `spawnThread` and
  `waitForThreads`.
- [`spawn-thread-foreach/`](./spawn-thread-foreach/README.md): one child
  thread per array element.
- [`run-child-workflow/`](./run-child-workflow/README.md): run another
  `WfSpec` as a child and wait for it.
- [`hierarchical-workflow/`](./hierarchical-workflow/README.md): parent,
  child, and grandchild WfSpecs sharing inherited variables.
- [`wait-for-one-of/`](./wait-for-one-of/README.md): race spawned threads
  with `waitForAnyOf`.
- [`parallel-approval/`](./parallel-approval/README.md): parallel approval
  threads, reminders, and a shared verdict.

### Events and humans

- [`external-event/`](./external-event/README.md): block on an `ExternalEvent`
  posted from outside.
- [`interrupt-handler/`](./interrupt-handler/README.md): interrupt a running
  workflow at any point.
- [`await-workflow-event/`](./await-workflow-event/README.md): emit a
  `WorkflowEvent` and await it from outside with `rpc AwaitWorkflowEvent`.
- [`correlated-event/`](./correlated-event/README.md): correlate an
  `ExternalEvent` to the right run by a business key.
- [`user-tasks/`](./user-tasks/README.md): assign a UserTask and complete it
  the way a UI would.

### Failure handling

- [`exception-handler/`](./exception-handler/README.md): business `EXCEPTION`
  vs technical `ERROR`, and how each is handled.
- [`saga/`](./saga/README.md): compensate completed steps when a later step
  fails.
- [`checkpoint-tasks/`](./checkpoint-tasks/README.md): checkpoint side
  effects so retries never repeat them.
- [`worker-context/`](./worker-context/README.md): `WorkerContext` ids,
  logging, and checkpoints that survive a retry.

### Earlier examples

These predate wfsdk support and register their `WfSpec` from checked-in JSON
with `lhctl deploy`:

- [`quickstart/`](./quickstart/README.md): KYC identity verification
  quickstart (matches the Java/Go/Python quickstarts).
- [`simple-worker/`](./simple-worker/README.md): basic task worker.
