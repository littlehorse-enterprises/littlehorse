# Proposal: sdk-js Parity — the Real-Server Tier

- Status: **Implemented** (55 tests across 7 suites); reliability notes below
- Scope: `sdk-js/src/integration/`
- Parent: [README.md](./README.md)

Everything else in this proposal family compares sdk-js against *models* —
Java's serialized output, a fake server we wrote. This tier is different: it
runs against **real LittleHorse servers**, because a model can only confirm
its author's assumptions. Every genuinely surprising finding in this project
came from this tier, and this file records both the machinery and the
knowledge it produced.

## Contents

- [Why this tier exists: acceptance is not execution](#why-this-tier-exists-acceptance-is-not-execution)
- [How to run it](#how-to-run-it)
- [How it stays hermetic](#how-it-stays-hermetic)
- [The seven suites](#the-seven-suites)
- [The assertion rule: only facts the server recorded](#the-assertion-rule-only-facts-the-server-recorded)
- [The cluster harness](#the-cluster-harness)
- [What the server taught us](#what-the-server-taught-us)
- [SDK gaps this tier exposed](#sdk-gaps-this-tier-exposed)
- [Reliability notes](#reliability-notes)

## Why this tier exists: acceptance is not execution

A workflow definition can be valid, registered, and completely wrong at
runtime. Before this tier was enumerated, several constructs — child threads,
interrupts, user tasks, wait-for-condition, JSON path access, native maps —
were *accepted* by the server in our tests but had never once been *executed*
by one. Byte-correct output (the wfsdk tier) says the instructions are right;
only a real run says the engine does what we think those instructions mean.

## How to run it

```sh
cd sdk-js && npm run test:integration        # full: ~2 minutes
cd sdk-js && npm run test:integration:core   # skips the 3 heavy suites: ~25s
```

The suite manages its own servers — nothing to start first. Setup launches a
uniquely named `lh-standalone` container on a free port (via Testcontainers,
as the Java side of the repo already uses) and waits for readiness; teardown
removes it. If the run is killed outright, Testcontainers' reaper removes the
container anyway (verified: reaped ~15s after a hard kill). Escape hatches:

| Variable                    | Effect                                                     |
| --------------------------- | ---------------------------------------------------------- |
| `LH_IT_HOST` / `LH_IT_PORT` | use a server you manage; no container started (~3.5s runs) |
| `LH_IT_KEEP=1`              | leave the container up after the run, for debugging        |
| `LH_IT_IMAGE`               | test against a different server image                      |

## How it stays hermetic

A fresh container **and a fresh tenant** per run. Both are deliberate: a warm
server passes tests a cold one fails (this suite has caught exactly that), and
server metadata is immutable, so reusing names across runs makes results
order-dependent. The tenant is created once in global setup and shared by all
files — because creating tenants from more than one test file in a run is
rejected by the server ("Tenant not allowed", learned the hard way).

## The seven suites

Enumerated, like everything else in this system, so "are we missing a test?"
has an answer:

| Suite                 | Proves                                                                                          | Infrastructure              |
| --------------------- | ----------------------------------------------------------------------------------------------- | --------------------------- |
| `wfspec-acceptance`   | the server accepts every reference workflow, and rejects an invalid one                         | shared standalone           |
| `execution`           | real workflow runs driven by JS workers produce the right status and variable values            | shared standalone           |
| `workflow-constructs` | **each wfsdk construct actually executes** — child threads, interrupts, user tasks, events, …   | shared standalone           |
| `worker-runtime`      | behavior only a server can drive — retries, timeouts, two workers sharing a task, checkpoints   | shared standalone           |
| `cluster`             | host discovery and **rebalancing**, which a single node cannot exhibit                          | own Kafka + N real servers  |
| `tls`                 | a real TLS handshake, not just the credentials we constructed                                   | own node + generated cert   |
| `oauth`               | a real identity provider mints the token and the server validates it by introspection           | own node + Keycloak         |

## The assertion rule: only facts the server recorded

Integration tests never assert on values the SDK under test produced. They
assert on protos the **server** wrote: the run's terminal status, variable
values read back, the number of thread runs created, task attempt records,
searchable user tasks, emitted events. The recipe for writing one is a single
question — *what fact would exist in the server's own records if this feature
worked?* — and the test asserts that fact. The child-threads test is the
canonical example: it checks the run completed, both child task functions
actually executed, the parent continued only afterwards, and
`threadRuns.length > 1` — the last one being the server's own testimony that
child threads truly existed.

## The cluster harness

Rebalancing, TLS, and OAuth cannot be tested on `lh-standalone`: it bundles
its *own* Kafka — so two standalones are two separate clusters with no shared
membership and nothing to rebalance — and its listener is fixed to plaintext
with no authentication. The harness (`cluster.ts`) therefore builds a real
cluster: one Kafka plus N real `lh-server` containers sharing a cluster ID on
a Docker network, each advertising `localhost:<hostPort>` because the worker
connects from the host, not from inside the network. Nodes can be added and
killed mid-test, which is what the rebalance and recovery tests do.

## What the server taught us

Each of these was a real test failure first. None could have been predicted by
reading sdk-java, and together they are documentation of observed server
behavior that other SDK authors will hit too:

- **The advertised listener must be reachable from the worker.** Workers ask
  the server which hosts to poll; without a reachable
  `LHS_ADVERTISED_LISTENERS`, they connect to bootstrap and then fail on the
  hosts they are handed.
- **Metadata is immutable.** Re-registering a task definition with different
  inputs fails with "already exists and is immutable" — hence tenant-per-run.
- **A task definition's inputs must match the workflow's `execute()` call**
  in number and type.
- **Referenced definitions must exist first** — external events, workflow
  events, user task definitions, and child workflow specs must all be
  registered before a workflow that references them.
- **Nested object fields inside a struct must reference a separately
  registered struct definition**; an inline one is rejected ("Forbidden JSON
  type: JSON_OBJ").
- **Workflow registration is eventually consistent.** Registration returns
  before the spec is queryable, so an immediate run request can fail with
  "Couldn't find specified WfSpec". Reliably fast on a warm server — which is
  why only a cold-container suite ever sees it — and handled by a readiness
  wait in the harness.
- **Only one tenant creation per test run** (see hermeticity above).
- **A struct-typed task input needs a struct-typed variable definition.** A
  plain JSON-object declaration makes the server fail the task on the
  mismatch rather than coerce.
- **A dead node keeps being advertised for 54+ seconds** (membership expires
  on a Kafka session timeout), while a joining node appears in ~3 seconds.
  RPCs genuinely fail while partitions reassign — so node loss is tested as
  *recovery*, which is what the system actually promises.
- **An OAuth issuer must have one canonical URL.** Both Keycloak and the mock
  otherwise derive the issuer from each request's `Host` header, so a token
  minted via `localhost` is rejected when the server introspects it through
  the Docker network alias. Keycloak can be pinned (`KC_HOSTNAME`,
  `KC_HOSTNAME_STRICT=false`, `KC_HOSTNAME_BACKCHANNEL_DYNAMIC=false`); the
  mock cannot — which is why the test runs a real identity provider.

## SDK gaps this tier exposed

The point of writing these tests is that writing them finds absences — each of
these was fixed in the SDK rather than worked around in the test:

- **The worker could not authenticate at all.** It had no way to present a
  bearer token, making it unusable against any secured server. Discovered by
  *writing* the OAuth test, before it ever ran.
- **Requests had to be complete messages** (the `@protobuf-ts`
  every-field-present requirement) — fixed by normalizing partial requests in
  the client, restoring what Java's builders allow.
- **The test harness itself had a private copy of the value decoder** that
  predated the serde unification and silently lacked a struct case. It now
  delegates to the SDK's own decoder — so assertions see exactly what a user's
  task function would receive.

## Reliability notes

The three infrastructure-heavy suites (`cluster`, `tls`, `oauth`) are young:
at review time they had exactly one clean full run, after two flakes on hook
timeouts under machine load. The underlying problem was diagnostic — the
readiness deadline equaled the test-hook budget, so the hook expired first and
swallowed the real error; readiness now sits strictly inside the hook budget.
Treat these three as unproven under CI load until they have run repeatedly in
automation — which is one reason getting this tier into CI is on
[roadmap.md](./roadmap.md). Measured runtimes for planning: full tier ~116s
(the cluster suite is ~64s of it), core loop ~25s.
