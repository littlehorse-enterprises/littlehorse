# Proposal: sdk-js Parity — the Task Worker Layer

- Status: **Draft** (runtime implemented and tested; the judgment-removal
  designs below are planned)
- Scope: `sdk-js/src/worker/`, the fake server in
  `sdk-js/src/feature-matrix/harness/`, and the worker-facing integration
  suites
- Parent: [README.md](./README.md) · Real-server details:
  [integration.md](./integration.md)

The worker is the hardest layer to prove, and this file explains both why and
what we do about it. Short version: the worker's output is *behavior over
time*, not an artifact you can diff — so the plan is to manufacture an
artifact, by treating everything the worker causes the **server** to record as
its output, and comparing a Java worker's records against ours.

## Contents

- [What the worker is, and why it can genuinely diverge](#what-the-worker-is-and-why-it-can-genuinely-diverge)
- [What exists today](#what-exists-today)
- [The fake server](#the-fake-server)
- [What only a real server can prove](#what-only-a-real-server-can-prove)
- [The honest gap: where judgment still lives](#the-honest-gap-where-judgment-still-lives)
- [The plan: differential conformance](#the-plan-differential-conformance)
- [Supporting designs](#supporting-designs)
- [Limits, stated](#limits-stated)

## What the worker is, and why it can genuinely diverge

A task worker is a process the *user* runs. It registers with the server,
long-polls for scheduled tasks, executes the user's function, and reports the
result — continuously, for weeks. The server decides *what* happens and
*when*; the worker is the only place that knows *how*.

This makes it the one layer where two SDKs can genuinely behave differently at
runtime. The wfsdk compiles and exits — byte-identical output means identical
behavior. The worker makes live decisions: how a thrown error is classified,
how many tasks it holds in flight, what it does when a poll stream breaks
mid-task, whether shutdown drains or drops, how reporting retries. Each of
those is a place Java and JS can quietly disagree.

A design principle inherited from the parity plan applies most strongly here:
**port semantics, not code.** Java's worker is built from thread classes
(`PollThread`, `RebalanceThread`, a liveness controller). The JS worker is a
genuinely different design for Node's event loop — what must match is the
*protocol behavior*, never the internal structure.

## What exists today

The JS worker (`src/worker/LHTaskWorker.ts` and friends) implements, with a
matrix test citing the Java source for each capability (40 entries):

- **Heartbeat-driven topology.** Every ~15s the worker re-registers with the
  server, which answers with the list of hosts this worker should poll
  (`yourHosts`). The worker reconciles its connections to that list — opening
  new ones, closing dropped ones. This is how rebalancing works: it is
  entirely server-driven, and the worker simply follows.
- **One long-poll stream per host**, with back-pressure: at the configured
  in-flight limit the worker stops *requesting* work rather than accepting
  tasks it cannot start.
- **Correct failure classification.** A thrown `LHTaskException` reports as a
  business `TASK_EXCEPTION`; any other throw as `TASK_FAILED`; a failure to
  map the server's input variables as `TASK_INPUT_VAR_SUB_ERROR`. The server
  treats these differently (retries, workflow-level handling), so
  classification is protocol behavior, not cosmetics.
- **Reporting that survives trouble**: result reporting retries on failure
  without duplicating; shutdown drains in-flight tasks before closing
  transports; an aborted poll call is what actually unblocks a graceful close.
- **Checkpoint replay** (`WorkerContext.executeAndCheckpoint`): on a retry,
  operations the previous attempt already completed are replayed from the
  server-stored checkpoint instead of re-executed — so side effects like
  charging a card are not repeated.
- **Authentication**: an explicit token, or one minted and refreshed from the
  config's OAuth provider. (This existed only because writing the OAuth
  integration test revealed the worker had *no way to authenticate at all* —
  see [integration.md](./integration.md).)
- **Health reporting** mirroring Java's states, including surfacing the
  server's own "cluster is rebalancing" signal.

## The fake server

`fakeServer.ts` is an in-process gRPC server that speaks the real LittleHorse
wire protocol on an ephemeral port, built from the generated proto types — no
Docker, no extra dependencies. Worker tests script it to produce situations a
real server won't produce on demand: break a poll stream mid-task, reassign
hosts between heartbeats, fail the first N report calls, deliver malformed
inputs. On top of it run a 300-task soak (asserting exactly-once delivery and
no memory growth), a restart-on-the-same-port chaos case, and sanity
benchmarks (absolute floors to catch order-of-magnitude regressions — a
deliberate framing: benchmarks are a smoke alarm, not a target).

**What it proves:** what the *client side* of the protocol does.
**What it structurally cannot prove:** that the real server agrees — it is a
model of the protocol written by the same people who wrote the worker, so it
can only confirm our own assumptions. That is why the integration tier exists.

Two hard-won notes for anyone extending it: `PollTask` is a long poll, so a
request must *park* until work exists — replying empty deadlocks the worker,
which only re-asks after a response. And breaking a stream requires *emitting*
an error; destroying the stream never reaches the client.

## What only a real server can prove

The `worker-runtime` integration suite covers behavior only a real server can
drive: retry counts actually recorded, timeout enforcement, two workers
sharing one task type, checkpoint replay against real storage. The `cluster`
suite proves host discovery and rebalancing against a real multi-node cluster
— including the measured facts that a joining node appears in ~3 seconds while
a dead node keeps being advertised for 54+ seconds, which is why node loss is
tested as *recovery*, not uninterrupted service. Details, and everything else
the server taught us, live in [integration.md](./integration.md).

## The honest gap: where judgment still lives

Both of this layer's current oracles leak judgment, in different places:

| Oracle today       | What it proves                          | The leak                                                                 |
| ------------------ | --------------------------------------- | ------------------------------------------------------------------------ |
| fake server        | the worker behaves as designed          | *expectations are authored* — by the people who wrote the worker         |
| worker-runtime     | reality agrees, for chosen scenarios    | *the scenario list is a judgment call* — a wrong call is invisible       |

A wrong judgment does not produce a failing test; it produces a test that
never gets written. Closing exactly that is the plan below.

## The plan: differential conformance

The reframe that makes the worker provable: **the worker does have an artifact
— everything it causes the server to record.** Attempt counts and result kinds
per task run, terminal workflow status, variable values, log output. The
server writes these down regardless of who asserts on them.

So, for each scenario: run it twice against the same server — once with a
**Java** worker, once with the JS worker — then diff *everything the server
recorded*, after normalizing away IDs, timestamps, and worker names. Any
surviving difference is a finding, and nobody had to decide in advance which
behaviors mattered: the diff covers all of them. Java's live behavior becomes
the worker's golden file, exactly as Java's bytes are the compiler's.

The cost is low: the golden-generator module already links `sdk-java` and
already runs small Java programs; the Java worker driver is one more class.

**Killing the scenario-list judgment the same way the feature matrix killed
the feature-list judgment: enumerate it.** Scenarios decompose into a visible
two-axis grid — *(what the task function does)* × *(what the infrastructure
does)*:

```text
task function axis                     infrastructure axis
─────────────────────────────          ─────────────────────────────
returns each value type                nothing (happy path)
throws a plain Error                   poll stream breaks mid-task
throws LHTaskException                 worker killed mid-task
returns undefined / null               server restarts
exceeds the task timeout               a second worker joins the task
```

Each cell is one differential run. The grid is checked in as its own todo
matrix, so an untested cell is *visible* instead of unthought-of.

## Supporting designs

- **Trace-differential.** The fake server speaks the real wire protocol —
  which means a *Java* worker can connect to it too. Run both workers against
  the same scripted scenario, record the full interaction trace (every poll,
  report, retry, reconnect), and diff the normalized traces. This sees what
  the server's records cannot — duplicate reports, retry cadence, behavior in
  the gap between a stream break and reconnection — and it converts the fake
  server from "our model" into a recording instrument whose expected behavior
  is whatever Java did on the same wire. Exact trace comparison for serial
  scenarios; concurrent ones are legitimately nondeterministic and are checked
  by invariants instead.
- **Universal invariants.** Some properties must hold in *every* scenario, so
  they need no scenario selection at all: exactly one accepted report per
  scheduled task (no drops, no duplicates), contiguous attempt numbers, no
  report after the drain deadline, terminal statuses from the legal set.
  Checked over the records of any run — including the soak pointed at a real
  server, which is where load-only bugs (leaks, deadlocks at high in-flight
  counts) live and where differential testing at low rates cannot see.
- **Benchmarks made relative.** The existing absolute floors move to a real
  server and gain a Java-worker baseline, so the assertion becomes "within N×
  of Java" instead of a magic number. Performance parity is a third axis,
  separate from correctness; being somewhat slower is a finding worth knowing,
  not a release blocker.

## Limits, stated

- **Differential testing proves parity, not correctness.** A Java worker bug
  reproduced faithfully passes the diff. Against this mission's definition —
  parity with the gold standard — that is the finish line; genuine
  shared-wrongness belongs to the protocol's owner. Hence the standing ask of
  the server team: a published worker **conformance kit** (a scripted harness
  plus invariant checkers any SDK must pass), so "is this worker correct?"
  eventually stops being any SDK author's question.
- **Grid completeness is still curated** — mitigated by making the grid a
  reviewed artifact, never eliminated.

Judgment residue for this layer, kept in writing: the scenario grid itself.
