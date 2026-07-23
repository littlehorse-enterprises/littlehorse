# sdk-js Feature Parity Plan

Goal: bring `sdk-js` to feature parity with `sdk-java` (the gold-standard SDK),
using the Java SDK as the reference implementation and a test harness as the
definition of done. This document records the plan, the current status, and the
rules so the effort stays honest and doesn't drift. It is written to be enough
context to start contributing cold.

**Live status — don't trust prose, run the suite:**

```sh
cd sdk-js && npx jest src/feature-matrix
```

Passed = done, todo = missing, failed = broken. That output *is* the feature
matrix (see below). Snapshot as of 2026-07-23: 141 passing / 84 todo. The core
wfsdk compiler is implemented (`src/wfsdk/`) with all 12 reference workflows
matching the Java goldens; the remaining todos are mostly worker lifecycle,
config, and advanced wfsdk features (details under "Ordering and status").

## Background: what an SDK is here

A LittleHorse SDK is three distinct components on top of the shared gRPC API:

1. **Client** — thin wrapper over generated gRPC stubs: config loading, TLS,
   OAuth, retries. `sdk-js` largely has this (`LHConfig`, `client.ts`,
   `grpcRetry`).
2. **Workflow SDK (wfsdk)** — the DSL where users define workflows
   (`wf.execute(...)`, `wf.doIf(...)`). Key mental model: this code never
   *runs* a workflow — it runs once, at registration time, to **compile** a
   graph into a `PutWfSpecRequest` proto that the server executes. The wfsdk is
   a compiler from the host language into a proto spec. It was entirely
   missing from sdk-js when this effort started; the port now lives in
   `src/wfsdk/` (Java reference: ~21 public classes, `WorkflowThread` being
   the largest).
3. **Task worker** — long-running runtime: registers/validates the `TaskDef`,
   long-polls for scheduled tasks, deserializes inputs, invokes the user
   function, reports results, plus operational hygiene (liveness/heartbeats,
   server rebalancing, reconnection). `sdk-js` has a basic worker
   (`LHTaskWorker.ts`); Java has a full `worker/internal/` package
   (`LHServerConnectionManager`, `PollThread`, `RebalanceThread`,
   `LHLivenessController`). The gap is production hardening, not the happy
   path.

Plus supporting pieces: `common/` (exceptions, proto ↔ native value
conversion) and `usertask/` helpers.

## Where everything lives

| Path | What it is |
|---|---|
| `sdk-js/src/feature-matrix/*.test.ts` | The feature matrix: one test/`test.todo` per Java SDK capability, by area |
| `sdk-js/src/feature-matrix/golden.ts` | `loadGolden` / `expectMatchesGolden` helpers |
| `sdk-js/src/feature-matrix/referenceWorkflows.ts` | TS twins of the Java reference workflows |
| `sdk-js/src/feature-matrix/wfsdk-golden.test.ts` | Conformance: every TS twin must compile to its golden |
| `sdk-js/src/wfsdk/` | The wfsdk port (Track A) |
| `sdk-js/golden/*.json` | Golden files: the Java SDK's compiled `PutWfSpecRequest` per reference workflow |
| `sdk-js/golden/generator/` | Java program (gradle `:sdk-js-golden-generator`) that emits the goldens |
| `sdk-java/src/main/java/io/littlehorse/sdk/` | The reference implementation being ported |

## Core principles

- **The proto contract is the real gold standard, not the Java source.** All
  SDKs compile to the same protobufs and talk to the same server. The Java
  source tells us *what* features exist and their edge-case semantics; the
  protos define *correct*.
- **Port semantics, not code.** Java idioms (annotation scanning, overloading,
  thread pools) must not be transliterated. The JS SDK should be idiomatic
  TypeScript (plain functions, options objects, event-loop async) that
  preserves the same *protocol behavior*. The worker internals in particular
  need a genuinely different async design, not a translation of
  `PollThread`.
- **A feature is done only when a test proves it.** Nothing gets marked
  complete because it compiles or "looks ported."

## The feature matrix: `test.todo` as the enumeration

The feature matrix lives **in the test suite**, not in a separate document, so
it cannot rot out of sync:

- The full Java public API was enumerated up front as Jest `test.todo(...)`
  entries in `src/feature-matrix/`, organized by area (`wfsdk`, `worker`,
  `config`, `common`, `usertask`). Each entry names the Java API it maps to
  (`— Java: Class#method`).
- Porting a feature means converting its `test.todo` into a real test **in
  the same change as the implementation**.
- Running the suite *is* the matrix: **passed = done, todo = missing,
  failed = broken.**
- Never delete an entry; a removed todo must show up in a diff with a stated
  reason (e.g. genuinely not applicable to JS).

Semantic gotchas discovered during porting (enum serialization quirks,
variable mutation semantics, timestamp handling) are recorded as comments next
to the relevant tests — they are the hardest-won knowledge in the port.
Examples found so far: Java's `releaseToGroupOnDeadline` throws unless the
user task was assigned with both a user AND a group; JS `number` literals
compile to INT when integer-valued, DOUBLE otherwise (Java distinguishes
statically) — documented in `src/wfsdk/builder.ts`.

**Graduation path:** when `test.todo` strings can no longer carry the needed
metadata (Java API references, "partial" status), migrate to a
machine-readable `features.yaml` with tagged tests and a meta-test that fails
if a feature marked done has no passing tagged test. The migration is
mechanical because the enumeration already exists in the todo names. Don't
start there.

## Test harness: three tiers

1. **Golden/conformance tests** (cheap, fast, no server) — define the same
   workflow in Java and JS; assert both produce the same `PutWfSpecRequest`
   proto. Java's serialized output is checked in as golden files. This is the
   primary oracle for the wfsdk. **Conformance = "did we build the right
   thing on paper."**
2. **Integration tests against a real server** — `lh-standalone` in Docker:
   register a WfSpec, run a workflow end-to-end with a JS worker, assert it
   completes with the right variable values. **Integration = "does it survive
   contact with reality."**
3. **Soak/chaos tests for the worker** — sustained load over time; kill and
   restart the server mid-run; verify reconnect with no dropped or
   double-reported tasks. Worker bugs are overwhelmingly lifecycle bugs, not
   logic bugs.

**Benchmarks** come last and are a sanity check, not a target: JS worker
throughput/latency vs the Java worker on the same server, to catch gross
regressions (50x), not to win.

## Ordering and status

1. Feature enumeration as `test.todo` entries (the matrix). **Done:**
   `src/feature-matrix/*.test.ts`.
2. Golden-test harness (proto comparison infrastructure + Java golden-file
   generation). **Done** — see "Golden harness" below.
3. wfsdk port — biggest gap, best oracle. **Mostly done:** `src/wfsdk/`
   compiles all 12 reference workflows to protos identical to the Java SDK's
   goldens; 104 of 119 wfsdk matrix entries are now real passing tests.
   Remaining todos: registerWfSpec/doesWfSpecExist (need client integration),
   compileAndSaveToDisk, structs/StructDefs, declareArray/declareMap,
   `registeredAs` payload auto-registration, withCorrelatedEventConfig.
4. Worker hardening — integration + soak tests, connection management,
   rebalancing, liveness. **Not started** (Track B below).
5. Benchmarks. **Not started.**

## Golden harness

- **Proto codegen decision (settled):** sdk-js proto code is generated by
  `@protobuf-ts/plugin` via `local-dev/compile-proto.sh` (the ts-proto config
  in the root `buf.gen.yaml` is stale and does not apply to sdk-js). Golden
  comparison uses `PutWfSpecRequest.fromJsonString` / `.toJson` / `.equals`
  from the generated code.
- **Java generator:** gradle module `:sdk-js-golden-generator`
  (`sdk-js/golden/generator`). Twelve reference workflows, one per wfsdk
  matrix area. Regenerate goldens with:
  `./gradlew :sdk-js-golden-generator:run --args="$(pwd)/sdk-js/golden"`
  (output verified deterministic across runs).
- **Golden files:** `sdk-js/golden/*.json` — the Java SDK's
  `compileWfToJson()` output, checked in.
- **JS side:** `src/feature-matrix/golden.ts` provides `loadGolden` (strict —
  unknown fields fail, which detects stale JS proto codegen) and
  `expectMatchesGolden` (JSON diff for readability, then proto equality as
  the authoritative check). `golden.test.ts` is the harness self-test: every
  golden parses and round-trips. `referenceWorkflows.ts` holds the TS twin of
  each Java reference workflow; `wfsdk-golden.test.ts` asserts every twin
  compiles to its golden.
- **Editing a reference workflow means editing BOTH twins** (Java generator +
  `referenceWorkflows.ts`) and regenerating the goldens with the gradle
  command above.
- **Not yet covered by goldens:** structs/StructDefs (needs registered
  schemas), type adapters, retention policies, format-string task names,
  dynamic task names. Goldens are added lazily: when a todo for an uncovered
  feature is picked up, extend a reference workflow (or add a new one) on
  both sides, regenerate, then write the JS test.

## Work division: two independent tracks

The remaining work splits into two tracks that barely touch each other. Each
track has a **single owner** to avoid collisions.

### Track A — wfsdk compiler

Owns `src/wfsdk/`, `referenceWorkflows.ts`, and the Java golden generator.
The compiler's internals (node-graph builder, variable handling, control
flow) interlock tightly — that's why it's single-owner.

Work loop: pick a remaining wfsdk todo → if no golden covers the feature,
extend a reference workflow on both sides and regenerate → implement in
`src/wfsdk/` → convert the todo into a golden-backed test.

### Track B — worker hardening + integration/soak harness

Owns the worker (`src/worker/`), the config/common/usertask areas, and all
server-facing test infrastructure. **Fully independent of Track A**: worker
tests don't need the JS wfsdk, because the golden JSONs *are*
`PutWfSpecRequest` payloads — the integration rig can register them directly
through the existing JS client (`PutWfSpecRequest.fromJsonString` →
`putWfSpec`) or `lhctl`, then run workflows against a real server with a JS
worker. No waiting on the compiler.

Work items, roughly in order:

1. **Integration test rig** — scripts/fixtures to spin up `lh-standalone` in
   Docker plus a separate Jest project for e2e tests (keep them out of the
   default unit run). First e2e: register a golden WfSpec, run it with a JS
   worker serving a trivial TaskDef, assert the WfRun completes with the
   right variable values.
2. **Worker lifecycle parity** — the worker todos (~38). Reference:
   `sdk-java/.../worker/internal/` (connection management across server
   hosts, rebalance protocol, liveness/heartbeats, reconnection). This is a
   redesign for Node's event loop guided by Java's *protocol behavior* — do
   not transliterate the thread classes.
3. **Config/common todos** (~30) — mostly unit-testable against `LHConfig`
   and the value-conversion helpers; the Java reference is named in each
   todo.
4. **Soak/chaos tests** — worker under sustained load; kill/restart the
   server mid-run; assert no dropped or double-reported tasks.

### Either track (grab-bag)

- **CI wiring** — run the matrix suite + golden self-test on every PR so the
  matrix is enforced, not aspirational.
- **usertask todos** (4) — small, isolated.

### Coordination rules

- The matrix files in `src/feature-matrix/` are the only shared surface.
  Convert todos only alongside the implementing change; conflicts there are
  test-file-only and resolve trivially.
- Track A alone touches `src/wfsdk/` + goldens; Track B alone touches
  `src/worker/` + integration infra. Anything cross-cutting (proto regen,
  `client.ts` changes), flag to the other owner first.
- New semantic gotchas go in comments next to the relevant test, same as
  always.

## Known risks

- **Proto codegen is locked to `@protobuf-ts`** (see Golden harness). Do not
  switch plugins — it invalidates every golden file.
- **AI-assisted porting will produce plausible-but-wrong mappings** for subtle
  semantics (variable mutation, JSON path handling, retry policies). The
  golden tests are the catch mechanism — which is why the harness was built
  before the mass port.
- **"Feature complete" is unfalsifiable without the enumeration.** The todo
  list was written first, in full, before any porting started. Never let a
  feature be marked done outside the matrix.
