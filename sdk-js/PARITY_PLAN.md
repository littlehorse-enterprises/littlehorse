# sdk-js Feature Parity Plan

Goal: bring `sdk-js` to feature parity with `sdk-java` (the gold-standard SDK),
using the Java SDK as the reference implementation and a test harness as the
definition of done. This document records the plan and the rules so the effort
stays honest and doesn't drift.

## Background: what an SDK is here

A LittleHorse SDK is three distinct components on top of the shared gRPC API:

1. **Client** — thin wrapper over generated gRPC stubs: config loading, TLS,
   OAuth, retries. `sdk-js` largely has this (`LHConfig`, `client.ts`,
   `grpcRetry`).
2. **Workflow SDK (wfsdk)** — the DSL where users define workflows
   (`wf.execute(...)`, `wf.doIf(...)`). Key mental model: this code never
   *runs* a workflow — it runs once, at registration time, to **compile** a
   graph into a `PutWfSpecRequest` proto that the server executes. The wfsdk is
   a compiler from the host language into a proto spec. **This is entirely
   missing from sdk-js** and is the biggest gap (~21 public classes in Java,
   `WorkflowThread` being the largest).
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

- Step one of the project (before any porting) is to walk the Java SDK's
  public API — every public method on `WorkflowThread`, `Workflow`,
  `WfRunVariable`, `LHTaskWorker`, `LHConfig`, the usertask helpers — and
  write the **entire enumeration** as Jest `test.todo(...)` entries, organized
  into `describe` blocks (`wfsdk`, `worker`, `config`, `usertask`). Expect
  roughly 80–150 entries.
- Porting a feature means converting its `test.todo` into a real test.
- Running the suite *is* the matrix: **passed = done, todo = missing,
  failed = broken.**
- Deleting a todo is visible in a diff, so the enumeration can't silently
  shrink.

Semantic gotchas discovered during porting (enum serialization quirks,
variable mutation semantics, timestamp handling) are recorded as comments next
to the relevant tests — they are the hardest-won knowledge in the port.

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
   primary oracle for the wfsdk.
2. **Integration tests against a real server** — `lh-standalone` in Docker:
   register a WfSpec, run a workflow end-to-end with a JS worker, assert it
   completes with the right variable values. Mirror the Java SDK's e2e
   scenarios.
3. **Soak/chaos tests for the worker** — sustained load over time; kill and
   restart the server mid-run; verify reconnect with no dropped or
   double-reported tasks. Worker bugs are overwhelmingly lifecycle bugs, not
   logic bugs.

**Benchmarks** come last and are a sanity check, not a target: JS worker
throughput/latency vs the Java worker on the same server, to catch gross
regressions (50x), not to win.

## Ordering

1. Feature enumeration as `test.todo` entries (the matrix).
2. Golden-test harness (proto comparison infrastructure + Java golden-file
   generation).
3. wfsdk port — biggest gap, best oracle.
4. Worker hardening — integration + soak tests, connection management,
   rebalancing, liveness.
5. Benchmarks.

## Known risks

- **Proto codegen choice must be settled before golden tests exist.** The
  dashboard's ts-proto → @protobuf-ts migration surfaced enum/oneof/timestamp
  gotchas; switching plugins later invalidates all golden files. Audit what
  `sdk-js` generates with today and lock the decision first.
- **AI-assisted porting will produce plausible-but-wrong mappings** for subtle
  semantics (variable mutation, JSON path handling, retry policies). The
  golden tests are the catch mechanism — which is why the harness is built
  before the mass port.
- **"Feature complete" is unfalsifiable without the enumeration.** The todo
  list is written first, in full, before any porting starts. That commitment
  is the load-bearing part of the whole plan.
