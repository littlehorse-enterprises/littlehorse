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
matrix (see below). Snapshot as of 2026-07-28: **191 of 191 entries pass —
100% of the enumerated Java surface**, plus 61 supporting tests and 45 of 51
integration checks against a real server (`npm run test:integration`).

That number means every capability enumerated from the Java SDK's public API
has a JS implementation and a test proving it. It does **not** mean the SDK is
finished: the enumeration is a snapshot of Java's API as of this port, the
integration suite covers a slice of behavior rather than everything, and areas
like OAuth are proven against a stand-in issuer rather than a real one. Treat
100% as "the port is complete and honest", not as "nothing left to do".

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
   (`LHTaskWorker.ts`), now hardened and fully covered by protocol tests,
   mirroring what Java splits across its `worker/internal/` package
   (`LHServerConnectionManager`, `PollThread`, `RebalanceThread`,
   `LHLivenessController`).

Plus supporting pieces: `common/` (exceptions, proto ↔ native value
conversion) and `usertask/` helpers.

## Where everything lives

| Path | What it is |
|---|---|
| `sdk-js/src/feature-matrix/*.test.ts` | The feature matrix: one test/`test.todo` per Java SDK capability, by area |
| `sdk-js/src/feature-matrix/golden.ts` | `loadGolden` / `expectMatchesGolden` helpers |
| `sdk-js/src/feature-matrix/referenceWorkflows.ts` | TS twins of the Java reference workflows |
| `sdk-js/src/feature-matrix/wfsdk-golden.test.ts` | Conformance: every TS twin must compile to its golden |
| `sdk-js/src/feature-matrix/fakeServer.ts` | In-process gRPC server used by the worker tests |
| `sdk-js/src/integration/` | Tier-2 tests against a real `lh-standalone` (`npm run test:integration`) |
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
   contact with reality."** **Done** — see "Integration tests" below.
3. **Soak/chaos tests for the worker** — sustained load over time; kill and
   restart the server mid-run; verify reconnect with no dropped or
   double-reported tasks. Worker bugs are overwhelmingly lifecycle bugs, not
   logic bugs. **Done** against the fake server (`worker.test.ts`): 300-task
   soak asserting exactly-once delivery and no heap growth, plus a
   restart-on-the-same-port chaos case.

**Benchmarks** come last and are a sanity check, not a target: JS worker
throughput/latency vs the Java worker on the same server, to catch gross
regressions (50x), not to win.

## Ordering and status

1. Feature enumeration as `test.todo` entries (the matrix). **Done:**
   `src/feature-matrix/*.test.ts`.
2. Golden-test harness (proto comparison infrastructure + Java golden-file
   generation). **Done** — see "Golden harness" below.
3. wfsdk port — biggest gap, best oracle. **Done.** `src/wfsdk/` compiles all
   14 reference workflows to protos identical to the Java SDK's goldens, and
   every wfsdk matrix entry passes.
4. Config surface. **Done.** Source-composing builder
   (`LHConfig.newBuilder()`), env-var loading, keepalive options, TLS/mTLS
   credentials, client creation, `getTaskDef`, worker id/version/concurrency,
   OAuth client-credentials with refresh, and the type adapter registry.
5. Worker hardening. **Done** — all 40 worker entries pass: registration and
   signature/StructDef validation, execution semantics, WorkerContext
   (including user/group context and `executeAndCheckpoint`), topology
   discovery, rebalance, reconnect, report-retry-without-duplicates, graceful
   close, liveness via `isClusterHealthy`, a real concurrency ceiling
   (`maxInflightTasks`), plus soak, server-restart chaos, and sanity
   benchmarks. See "Fake server" below for how, and what it does not prove.

   The two benchmark entries measure absolute floors against the fake server
   rather than comparing to a live Java worker — running sdk-java inside this
   suite was judged not worth the coupling. They catch order-of-magnitude
   regressions, which is what the plan asks of them, but they are *not* a
   cross-SDK comparison.
6. OAuth (client-credentials, refresh, `isOauth`). **Done** —
   `src/common/oauth.ts` fetches a token with HTTP Basic + the
   `client_credentials` grant, caches it, refreshes inside a configurable skew
   before expiry, and collapses concurrent refreshes into one request.
   Verified against a stand-in issuer, **not** a real identity provider.
7. common/ serde. **Done** — `src/common/serde.ts` is now the single
   JS <-> VariableValue implementation (the wfsdk and worker previously had
   their own copies), verified against `golden/fixtures/serde.json` emitted by
   the Java SDK.
8. usertask. **Done** — `src/usertask/` describes User Task forms with zod in
   place of Java's annotated classes, compiling to the same
   `PutUserTaskDefRequest`.
9. Benchmarks. **Done** (sanity floors; see step 5's note).

### Integration tests (tier 2, real server)

`src/integration/` runs against a real `lh-standalone`. Kept out of `npm test`
so the default suite stays Docker-free. It manages its own server — nothing to
start first:

```sh
cd sdk-js && npm run test:integration
```

`globalSetup` starts a uniquely named container on a free port (via
Testcontainers, as the Java side of this repo already uses) and waits for it;
`globalTeardown` removes it. If the run is killed outright — Ctrl-C, CI
timeout — teardown never fires and Testcontainers' Ryuk reaper removes the
container instead (verified: reaped ~15s after a SIGKILL). Roughly 25s per
run, most of it server boot. Env overrides:

| Variable | Effect |
|---|---|
| `LH_IT_HOST` / `LH_IT_PORT` | Use a server you manage; no container started (~3.5s runs) |
| `LH_IT_KEEP=1` | Leave the container up after the run, for debugging |
| `LH_IT_IMAGE` | Test against a different image |

`npm run test:integration:core` skips the three infrastructure-heavy suites
(`cluster`, `tls`, `oauth`) for a fast inner loop. They dominate the runtime:
a real `lh-server` on a fresh Kafka spends ~2 minutes in Kafka Streams restore
before it serves, and each of those suites builds its own. The full run is
~5 minutes; core is ~25s.

Seven suites, and like the feature matrix they are **enumerated** so "are we
missing a test?" has an answer rather than a shrug:

| Suite | Proves | Infra |
|---|---|---|
| `wfspec-acceptance` | the server accepts every reference workflow, and rejects an invalid one | shared standalone |
| `execution` | real WfRuns driven by JS workers produce the right status and variable values | shared standalone |
| `workflow-constructs` | **each wfsdk construct actually executes** — enumerated from the methods on `WorkflowThread` that produce runtime behavior | shared standalone |
| `worker-runtime` | **behavior only the server can drive** — retries, timeouts, multi-worker sharing, checkpoint replay | shared standalone |
| `cluster` | host discovery and **rebalancing**, which a single node cannot exhibit | own Kafka + N `lh-server` |
| `tls` | a real TLS handshake, not just the credentials we build for one | own node + generated cert |
| `oauth` | a real issuer mints the token and the server validates it by introspection | own node + Keycloak |

`workflow-constructs` and `worker-runtime` exist because acceptance is not
execution: a spec can be valid and still behave wrong. The
`workflow-constructs` enumeration is derived mechanically from
`WorkflowThread`, so any construct there without an entry is a visible gap;
`test.todo` marks coverage deliberately not built, each with a stated reason.

The last three build their own infrastructure (`cluster.ts`) because
`lh-standalone` bundles its own Kafka — two standalones are two separate
clusters, so rebalancing is not observable there at all, and its listener is
fixed to plaintext with no authentication.

The suite is hermetic — a fresh container *and* a fresh tenant per run — which
is deliberate: a warm server can pass tests a cold one fails, and this suite
has done exactly that.

**Constraints this surfaced that no offline test could.** Each one was a real
failure first:

- **The advertised listener must be reachable.** Workers ask the server which
  hosts to poll; without `LHS_ADVERTISED_LISTENERS`, they connect to the
  bootstrap and then fail on the hosts they are handed.
- **Metadata is immutable.** Re-registering a TaskDef with different input
  vars fails with "already exists and is immutable", so reusing a namespace
  makes runs order-dependent. Hence a tenant per run.
- **A TaskDef's input vars must match the arity of the `execute()` call.**
- **ExternalEventDefs, WorkflowEventDefs, UserTaskDefs and child WfSpecs must
  exist before the WfSpec referencing them.**
- **Nested object fields inside a StructDef must reference a separately
  registered StructDef**; an `inlineStructDef` is rejected ("Forbidden JSON
  type: JSON_OBJ ... use native equivalents").
- **WfSpec registration is eventually consistent.** `putWfSpec` returns before
  the spec is queryable, so an immediate `runWf` can fail with "Couldn't find
  specified WfSpec" — reliably fast on a warm server, which is why it only
  appears on a cold one. `awaitWfSpecReady()` in the harness handles it.
- **Creating tenants from more than one test file in a run** produced "Tenant
  not allowed" on the second file; one tenant per run avoids it.
- **A struct-typed task input needs a struct-typed `VariableDef`.** Declaring
  it with a plain `z.object()` produces `JSON_OBJ`, and the server fails the
  TaskRun on the mismatch rather than coercing — use `lhStruct()` on both
  sides.
- **A node that dies keeps being advertised in `yourHosts` for 54s+**, because
  membership expires on a Kafka session timeout. A node that *joins* appears
  in ~3s. So rebalance-on-join is a fast assertion; node loss can only be
  tested as *recovery*, since RPCs genuinely fail while partitions reassign.
- **An OAuth issuer must have one canonical issuer URL.** Both Keycloak and
  mock-oauth2-server otherwise derive it from each request's `Host`, so a
  token minted by the client via `localhost` is rejected when the server
  introspects it via the Docker network alias. Keycloak can be pinned
  (`KC_HOSTNAME` + `KC_HOSTNAME_STRICT=false` +
  `KC_HOSTNAME_BACKCHANNEL_DYNAMIC=false`); mock-oauth2-server cannot, which
  is why the test uses a real IdP.

**SDK gaps the integration tier found**, each fixed rather than worked around
in the test — the point of the tier is that writing the test is what exposes
them:

- **The worker could not authenticate.** It had no way to present a token, so
  it was unusable against any secured server. It now takes an `accessToken` or
  pulls one from the config's OAuth provider, refreshing through the existing
  reconnect path.
- **Requests had to be complete messages.** protobuf-ts requires every
  repeated and map field to be present; omitting one failed deep inside
  serialization with `Cannot read properties of undefined (reading 'length')`.
  The client now accepts `PartialMessage` and normalizes through the message's
  own `create()`, matching what Java's builders let you leave out.
- **The integration harness had its own copy of `unwrap`** that predated the
  serde unification and silently lacked a `STRUCT` case, so struct assertions
  compared against raw protos. It now delegates to `varValToObj`, which is
  what a user's task function actually receives.

### Fake server (worker tests)

`src/feature-matrix/fakeServer.ts` is an in-process gRPC server that speaks
the real LittleHorse wire protocol on an ephemeral port, built from the
generated protobuf-ts message types (no extra dependency, no Docker). Worker
tests drive it to script scenarios that are otherwise hard to produce:
delivering tasks to a long poll, reassigning hosts mid-run, breaking a poll
stream with UNAVAILABLE, failing the first N `ReportTask` calls.

**What it proves:** what the *client* does. **What it does not prove:** that
the real server agrees. Tier-2 integration tests against `lh-standalone` are
still todo and are not replaced by this.

Two notes for anyone extending it: `PollTask` is a long poll, so a request
parks until work exists (replying empty would deadlock the worker, which only
re-asks after a response); and breaking a stream requires *emitting* an error
— `destroy()` alone never reaches the client.

### Resolved: type adapters

Previously parked. Resolved by building the JS analogue rather than declaring
it not-applicable: zod describes *schemas*, but not custom class instances,
which would otherwise fall through to the generic object branch and come back
as plain JSON. `LHConfig#addTypeAdapter` / `getTypeAdapterRegistry`
(`src/common/typeAdapters.ts`) close that gap.

One deliberate difference from Java: adapters apply automatically when
*writing*, but decoding stays with the built-ins unless a caller asks for an
adapter by name. An encoded value carries no marker saying which adapter
produced it, and guessing wrong would silently return the wrong type.

### Serde: one implementation, checked against Java

`src/common/serde.ts` is the only JS <-> VariableValue conversion in the SDK.
`golden/fixtures/serde.json` records how the *Java* SDK encodes representative
values, and the common tests assert byte agreement. Two findings came out of
building it, both invisible to any JS-only test:

- Java **drops null object fields**, recursively, but keeps nulls inside
  arrays. Plain `JSON.stringify` does neither, so `lhJsonStringify` exists.
- Structural type-sniffing is unsafe in JS: an early version detected
  `WfRunId` by shape and silently encoded the very common object
  `{id: 'abc'}` as a WF_RUN_ID, discarding its other fields. WF_RUN_ID must
  now be passed as an explicit `VariableValue`.

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
