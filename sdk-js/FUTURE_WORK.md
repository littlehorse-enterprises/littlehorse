# sdk-js — future work

Working notes, not part of the plan yet. Everything here is deliberately
deferred: the parity system described in `PARITY_PLAN.md` works and is at
100% on its own terms, and each item below closes a gap that system currently
cannot close by itself. Fold what survives scrutiny into `PARITY_PLAN.md` and
delete the rest.

## The gaps these address

Today's coverage proves three things, each with a source of truth independent
of anyone's judgment:

| Layer                                       | Oracle                             | Status                                                    |
| ------------------------------------------- | ---------------------------------- | --------------------------------------------------------- |
| Does JS emit the same proto as Java?        | Java's serialized output (goldens) | solid, 14 workflows + 24 serde values                     |
| Does the client behave correctly?           | the fake server                    | solid, but it only confirms our own model of the protocol |
| Does the real server accept and execute it? | the server                         | solid, 55 tests, 100% of an enumerated matrix             |

What none of them close:

1. **Enumeration freshness.** The matrix was derived by reading sdk-java once.
   If Java adds a public method, nothing fails and no todo appears.
2. **Golden coverage.** A wfsdk construct with no golden is unproven, and
   nothing counts which constructs lack one.
3. **Worker semantic divergence.** The worker is a live protocol participant,
   not a proto generator, so Java and JS _can_ genuinely disagree — on error
   classification, in-flight behavior, shutdown draining, retry semantics.
   Which of those get tested is currently a judgment call.

Note that wfsdk _runtime_ divergence is not on this list, and shouldn't be: the
server executes the proto, so a byte-identical proto means identical execution
by construction. The wfsdk risk is coverage (#2), not semantics.

---

## 1. Differential worker conformance — highest leverage

**Gap:** #3. Replaces "which runtime behaviors should I think to test?" with
"run both SDKs and diff everything the server recorded."

**Build:** a scenario harness that runs each scenario twice against the same
server — once with a Java worker, once with the JS worker — then diffs the
server-side artifacts:

- `TaskRun.attempts[]` — count, each `result` oneof kind, failure name, failure message
- terminal `WfRun.status`
- resulting variable values
- which attempt succeeded, when a retry was involved

Normalize away IDs, timestamps, and worker IDs before comparing. Any remaining
difference is a finding, and nobody had to decide in advance that it mattered.

Scenarios are a task-function behavior plus a trivial one-node workflow:

```
task function throws a plain Error
task function throws an LHTaskException (business EXCEPTION)
task function returns undefined / null
task function exceeds the TaskDef timeout
task function returns a value needing every serde type
worker killed mid-task
poll stream breaks between execute and report
two workers on one TaskDef
```

**Cost: lower than it looks.** `golden/generator/build.gradle` already declares
`implementation project(':sdk-java')` and already uses the `JavaExec` task
pattern (see `runSerde`). A Java worker driver is one more class and one more
task in a module that is already wired up — not new infrastructure.

**Limit, stated honestly:** it only compares behaviors both SDKs exhibit in
scenarios you actually run. If both are wrong the same way, it agrees
enthusiastically. It shrinks the judgment problem; it does not remove it.

## 2. Java API freshness check — cheapest real win

**Gap:** #1. Makes "are we still at parity?" a command instead of a claim.

**Build:** extract the public members of the sdk-java classes the matrix is
derived from, and fail if any lacks a corresponding entry. The mapping already
exists in the test names — **187 of 191 entries cite their Java symbol** as
`— Java: Class#method`, so the check is a set difference, not an inference.

Classes currently covered, by entry count: `WorkflowThread` (51), `Workflow`
(18), `LHExpression` (17), `LHConfig` (14), `WfRunVariable` (12),
`LHTaskWorker` (11), `WorkerContext` (9), `LHLibUtil` (5), plus smaller ones.

The 4 entries with no Java citation are intentional — golden-files-exist, soak,
chaos, and the two benchmark entries cite a plan tier instead. The check must
allow that exemption explicitly rather than by accident.

**Effort:** hours. Reflection over the sdk-java jar, or parsing the sources.

## 3. Randomized dual-compile for the wfsdk

**Gap:** #2. Turns golden coverage from "14 workflows someone thought of" into
however many thousand you care to run.

**Build:** generate random _valid_ workflows from a shared description, compile
in both SDKs, assert byte-identical `PutWfSpecRequest`. The real cost is the
shared scenario format — a small serializable program description that a Java
driver and a JS driver both interpret. Everything else is a loop and a
comparison we already do.

**Effort:** the most expensive item here. Worth it only after 1 and 2, and
worth prototyping narrowly first (e.g. random expressions and mutations only,
which is where the semantic corners live — see the INT/DOUBLE literal note in
`src/wfsdk/builder.ts`).

## 4. Soak and benchmarks against a real server, with a Java baseline

**Gap:** partially #3 — specifically the divergence that only appears under
concurrency or duration (dropped tasks, double reports, leaks, deadlocks at
high in-flight counts). Load is the only way to surface those, and differential
testing at 1 task/sec will not.

**What exists today** (all in `src/feature-matrix/worker.test.ts`, all against
the **fake** server):

- 300-task soak asserting exactly-once delivery and no heap growth
- restart-on-the-same-port chaos case
- throughput floor: >20 tasks/sec, absolute, no Java comparison
- latency ceiling over 25 samples, likewise absolute

**What to add:** point all four at a real server, and add a Java worker
baseline so the assertion becomes relative ("within Nx of Java") instead of a
magic number. Keep the framing already in `PARITY_PLAN.md` — benchmarks are a
sanity check for gross regressions (50x), not a target. Being 3x slower than
Java is a finding worth knowing and not a release blocker.

Note this is a **third axis**, separate from correctness: performance parity
and semantic parity fail independently, and neither implies the other.

## 5. Smaller items

- **Injectable logger for the worker.** It currently writes to `console.*`
  unconditionally, which is why integration runs print worker chatter that
  reads like failures. Offered before, never picked up.
- **Prove the heavy integration suites under CI load.** `cluster`, `tls`, and
  `oauth` have exactly one clean full run behind them. They flaked twice on
  hook timeouts before the readiness deadline was moved strictly inside the
  hook budget (see `cluster.ts`). Run them repeatedly, and in CI, before
  trusting them.
- **Graduate `test.todo` to `features.yaml`** — only when todo strings can no
  longer carry the metadata. `PARITY_PLAN.md` describes the migration; it is
  mechanical because the enumeration already exists in the todo names. Don't
  start there.

---

## Suggested order

1. Differential worker conformance — attacks the largest gap, cheap given the existing Java module
2. Java API freshness check — hours of work, different gap
3. Soak/benchmarks on a real server with a Java baseline — incremental on what exists
4. Randomized dual-compile — most expensive, do last

Items 1 and 2 are the ones that change what the system can _prove_. The rest
raise confidence in things it already proves.
