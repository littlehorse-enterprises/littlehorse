# Proposal: SDK Conformance, the v1 design

- Status: **Implemented** (2026-09); kept as the design record. See the
  implementation note below for where the built suite grew beyond this
  document.
- Parent: [README.md](./README.md)

v1 as proposed here is deliberately narrow: workflow-compilation
conformance for two SDKs, sdk-js and sdk-java. This file states that
architecture in full, then a dedicated
[Future versions](#future-versions) section covers what later versions can
entail. How SDKs join and how the suite is governed live in
[adoption.md](./adoption.md).

## Implementation note (2026-09)

The suite landed as designed, and bigger. Current truth lives in
[sdk-conformance/README.md](../../sdk-conformance/README.md); the deltas
from this document are:

- Three areas shipped instead of one: `wfsdk` (119 cases), `serde` (20),
  and `registrations` (8), each under `areas/<area>/` with its own
  normative `rules.md`. The corpus conventions below (probe pairs,
  surfaces, exemptions, ratchets) generalized to all three unchanged.
- The testee contract grew with the areas: `convert` (serde),
  `registrations`, and `fuzz` verbs joined `list` and `compile`, plus
  optional batch accelerators. The contract still lives normatively in the
  suite README.
- The randomized dual-compile sketched under Future versions (v3) landed
  early in two-SDK form: seeded random workflows generated independently
  by every testee and pairwise cross-compared, with optional live-server
  registration ([FUZZ.md](../../sdk-conformance/FUZZ.md)).
- A single-command reporter (`runner/all.mjs`) runs freshness, grading,
  matrix, and fuzz as one gate.
- CI: the suite job exists but currently runs report-only for everyone
  (including the two v1 SDKs), and the canon drift gate is not wired yet.
  Both remain the plan; gating is each team's opt-in per
  [adoption.md](./adoption.md).

## Contents

- [Why this layer first](#why-this-layer-first)
- [The layout](#the-layout)
- [Where the code lives: two homes](#where-the-code-lives-two-homes)
- [The corpus](#the-corpus)
- [The testee contract](#the-testee-contract)
- [The runner](#the-runner)
- [The ledgers](#the-ledgers)
- [Results and the matrix](#results-and-the-matrix)
- [The canon lifecycle](#the-canon-lifecycle)
- [CI wiring](#ci-wiring)
- [Acceptance criteria](#acceptance-criteria)
- [Future versions](#future-versions)

## Why this layer first

An SDK's workflow layer is a compiler: user code in, one proto message
(`PutWfSpecRequest`) out. That makes this layer's conformance data-shaped.
A case's entire verdict is "does this SDK produce this message," judged
against a frozen file. Two properties follow:

- It is the strongest guarantee per unit cost. The server only ever sees
  the compiled proto, so two SDKs that emit identical protos have
  identical runtime behavior for that workflow *by construction*: no live
  server in the loop, no flakiness, no timing.
- It is where every durable suite starts. The research's most repeated
  pattern: language-neutral data cases with stable IDs and frozen expected
  outputs survive, while scenario-shaped cases (live processes, real
  servers) cost an order of magnitude more each and are kept deliberately
  few. Live-behavior conformance is therefore sequenced into
  [Future versions](#future-versions), not skipped.

## The layout

One new top-level directory in the monorepo:

```text
sdk-conformance/
├── README.md                the suite's own doc: what it is, how to run it,
│                            and the normative testee contract an SDK
│                            implements to join (the Test262 lesson: specify
│                            the contract as a document)
├── runner/
│   ├── run.mjs              shells each testee, compares output to canon,
│   │                        writes results/<sdk>.json
│   ├── compare.mjs          semantic proto equality + the JSON diff printed
│   │                        on failure
│   ├── freshness.mjs        checks every capability in surface.json has a
│   │                        case or an exemptions.yaml entry
│   └── matrix.mjs           regenerates MATRIX.md from results/ + ledgers/
├── testees.json             registry: sdk → the command that invokes its
│                            testee (the seam between suite and SDKs)
├── areas/wfsdk/       (one directory per corpus area)
│   ├── manifest.json        the case registry: one entry per case (stable
│   │                        id, title, capability keys, level)
│   ├── surface.json         the reference SDK's reflected public
│   │                        workflow-building surface, a checked-in
│   │                        artifact its generator task emits
│   ├── exemptions.yaml      corpus-level bookkeeping: todo (capabilities
│   │                        awaiting a case) + not_applicable (no
│   │                        compiled-output effect), each with a reason
│   └── cases/<id>/
│       ├── scenario.md      a few lines of prose: what to build, and what
│       │                    the base and feature variants differ by
│       ├── base.json        canonical proto WITHOUT the feature
│       └── feature.json     canonical proto WITH it (single-fixture cases
│                            omit base; see The corpus)
├── ledgers/
│   ├── js.yaml              per-SDK: todo (tracked debt) +
│   ├── java.yaml            not_applicable (permanent, with reasons)
│   └── …one per SDK as they join
├── results/
│   ├── js.json              machine outcomes per case:
│   └── java.json            PASS|FAIL|SKIP|MISSING + corpus git revision
└── MATRIX.md                generated report card, never edited by hand
```

## Where the code lives: two homes

Split deliberately, per the research:

- The runner lives in the suite (`sdk-conformance/runner/`): SDK-neutral
  orchestration in plain Node, no other toolchain required. It shells
  programs and compares JSON; it never builds anyone's SDK.
- Each testee lives in its SDK's own directory, built by that SDK's own
  toolchain in that SDK's own CI job, and registered in `testees.json`. A
  testee is a thin shim over the SDK's existing workflow-builder entry
  points; it needs no compilation machinery of its own.

This is the Avro lesson applied: their cross-language matrix rotted
because one orchestrator tried to build every toolchain in one
environment. Ours inverts that: per-SDK builds, one dumb comparer.

## The corpus

Three rules give the corpus its authority.

Derived, not curated. The case list is derived mechanically from the
public workflow-building surface of the reference SDK (sdk-java; see
[The canon lifecycle](#the-canon-lifecycle)): its generator task reflects
over that API and emits `wfsdk/surface.json`, a checked-in artifact. The
suite-side freshness check (`runner/freshness.mjs`) then fails CI when any
capability in that surface has neither a case covering it (via `covers`)
nor an entry in `wfsdk/exemptions.yaml`, which is corpus-level bookkeeping
with the same two-state vocabulary as the per-SDK ledgers: `todo` for
capabilities awaiting a case, `not_applicable` (with a reason) for
capabilities with no compiled-output effect, such as pure accessors. The
check ratchets both ways: a capability that gains a case must leave the
exemption file. New API surface cannot appear without the exam noticing,
and which workflows to test is never a matter of taste.

Every case is a probe pair, with one narrow exception. A case is a
minimal workflow built twice: once without the feature under test (base)
and once with exactly that feature added (feature). Both variants'
compiled protos are frozen as canonical fixtures, so the diff between them
*is* the feature's entire effect: a passing SDK is proven to express the
feature, not merely to build workflows that happen to match. A structural
assertion that base and feature fixtures must differ guards against
vacuous cases that toggle nothing. The exception: a feature that cannot be
subtracted from a minimal workflow (the ability to compile a workflow at
all) has no meaningful base, so it ships one `feature` fixture, declares
`variants: ["feature"]`, and the differ-guard applies only to pairs.

Prose beside the data. Each case carries a `scenario.md`: a few lines
describing what the testee must build, precise enough that a new SDK can
implement the case without reading another SDK's code (the Temporal
pattern: a prose spec beside the case, idiomatic implementations
elsewhere).

One entry in `manifest.json` per case:

```json
{
  "id": "spawn-thread-input-vars",
  "title": "spawn a child thread with input variables",
  "level": "required",
  "covers": ["WorkflowThread#spawnThread"],
  "variants": ["base", "feature"]
}
```

- `id` is stable forever (the Smithy lesson: ledgers and results key on
  it, so it survives reorganizations).
- `level` is `required` or `recommended` (the protobuf lesson: strictness
  lives in the case). Only `required` cases can fail a gated SDK's CI; see
  [The runner](#the-runner).
- `covers` carries the capability keys (`Class#method`) that tie the case
  back to the freshness check.

## The testee contract

The entire cost of joining the suite. Specified normatively in
`sdk-conformance/README.md`; an SDK provides one small command-line
program:

```text
testee list
    → prints the case ids this SDK implements, one per line

testee compile --case <id> --variant base|feature
    → prints the compiled workflow proto as JSON on stdout, exit 0
    → exit nonzero with a message on error
```

That was the whole v1 contract as proposed: two verbs, tens of lines per
SDK, wrapping the workflow builder the SDK already ships. (As built, each
new area added a verb of the same shape; the suite README carries the
current contract.)

## The runner

For every case × variant in the manifest, for every SDK in
`testees.json`:

| Outcome | When |
| --- | --- |
| `MISSING` | the testee's `list` does not include the case |
| `SKIP` | the SDK's ledger excuses it (`todo` or `not_applicable`) |
| `PASS` | testee output ≡ canonical fixture, judged by semantic proto equality: both sides parsed into message objects and compared by content, so field order and formatting never matter (the protobuf lesson: judge meaning, not characters) |
| `FAIL` | anything else, with a JSON diff printed |

Every outcome is recorded in `results/`. What *gates* is narrower: an
unexcused `FAIL` or `MISSING` on a `required` case fails the run;
`recommended` outcomes appear in the results and the matrix but never fail
CI. Then the ledger reconciliation runs in both directions (the two-way
ratchet): an excused case that unexpectedly *passes* also fails the run,
with the exact ledger line to delete printed, at either level, because the
ratchet polices ledger honesty, not case strictness. The ledger must equal
reality at all times.

## The ledgers

One YAML per SDK, in the suite. Even at the eventual five SDKs we are
below the scale where consumer-side ledgers (each SDK keeping its
skip-list in its own tree, as the browsers do) pay off; revisit if the
suite ever outgrows the repo:

```yaml
# ledgers/<sdk>.yaml: an SDK part-way through the corpus
todo:                       # tracked debt: will pass eventually
  - id: spawn-thread-input-vars
    reason: wfsdk port in progress
not_applicable: []          # permanent, with reasons; use sparingly
```

The vocabulary is sass-spec's, proven over a decade: "not yet" and
"never" are different states, and conflating them poisons the ledger.

## Results and the matrix

`results/<sdk>.json` is the machine record: every case's outcome plus the
corpus git revision it ran against (the wpt lesson: results are only
comparable on the same corpus). `MATRIX.md` is generated from results plus
ledgers: capability areas as rows, SDKs as columns, per-SDK totals, and a
"generated at revision X, do not edit" footer. The matrix is trustworthy
for one reason the research insists on: every cell is an executed test,
never a self-attested claim. CI fails if `MATRIX.md` is stale relative to
`results/`.

## The canon lifecycle

Canon is minted once from a reference implementation (sdk-java, via a
dedicated generator task whose output is the fixtures and `surface.json`)
and frozen from then on. Three consequences:

- The reference becomes an examinee. The Java testee *compares* against
  the frozen fixtures like everyone else. If a Java refactor changes
  compiled output, Java's column goes red: the suite detects drift in the
  gold standard itself, which no bilateral scheme can.
- A deliberate canon change is a reviewed event. Regenerate via the
  generator task in a PR to `sdk-conformance/areas/wfsdk/`; the fixture
  diff is the review surface (the sass-spec flow: the reference
  regenerates, humans approve the diff). Canon and ledger PRs require a
  second reviewer ([adoption.md](./adoption.md), governance).
- New cases land green everywhere by construction. One PR adds the
  manifest entry, `scenario.md`, the fixtures (generated by the reference,
  reviewed as readable JSON), and a `todo` ledger entry for every SDK that
  doesn't pass yet, so the suite never lands red and debt is visible from
  birth (the protobuf same-PR convention).

## CI wiring

- The suite job, on every PR: the runner for all registered testees, the
  freshness check, and the matrix staleness check. Plain Node, building
  nothing.
- The canon drift gate runs in the reference SDK's own toolchain step,
  respecting the two-homes rule: it re-runs the generator task and fails
  when the regenerated fixtures or `surface.json` differ from the
  checked-in files. That is what makes canon changes *reviewed*: the
  checked-in canon can only change in a PR whose diff shows the
  regeneration. (Distinct from the Java column reddening. The column
  catches Java's output diverging from canon at exam time; the drift gate
  catches the checked-in canon and surface going stale relative to the
  reference's code.)
- Each testee builds in its own SDK's toolchain step; the runner job only
  invokes their commands and compares JSON.
- The fixtures are ordinary checked-in files, so each SDK's own unit tests
  are free to assert against them natively too. Conformance failures then
  surface twice: in the matrix, and as ordinary test failures in the SDK's
  own pipeline.

## Acceptance criteria

v1 is done when all of these hold:

- `sdk-conformance/` exists with the corpus, fixtures, manifest, normative
  testee contract, and both testees registered.
- The freshness check passes: every capability in `wfsdk/surface.json` has
  a case or an `exemptions.yaml` entry.
- `MATRIX.md` shows `PASS` in every cell for both SDKs, with both v1
  ledgers' `todo` lists empty, at a stamped corpus revision.
- Break-it-on-purpose passes (the house rule: a check we have never seen
  fail is a check trusted on faith):
  - corrupt one fixture: both columns redden;
  - alter one testee's output: its column reddens with a readable diff;
  - add a ledger todo for a passing case: the ratchet reddens with the
    fix printed;
  - remove a case from a testee's `list`: `MISSING` reddens;
  - add a public method to the reference's surface without a case:
    freshness reddens.
- CI runs the suite job and the drift gate.

## Future versions

Everything below is explicitly out of v1's scope, named so the scope is a
decision, not an accident. None of it is assumed by v1; each step is
optional and independently valuable, and the version tags are a likely
order, not a schedule. (Two things are out of scope in *any* version: web
dashboards, because `MATRIX.md` is the dashboard, and a case-authoring
DSL, because cases stay data plus prose.)

**More SDKs (v1.x).** sdk-go, sdk-python, and sdk-dotnet could join at the
report-only rung ([adoption.md](./adoption.md)); each would cost its team
a testee shim and a ledger file, and buy them a public, always-current
inventory of exactly what their SDK covers. Every new column is also an
empirical measurement of how cheaply an SDK can be brought to green
against a frozen exam, data the org can then plan with instead of
guessing.

**Config resolution (v1.x).** Client configuration is a translation like
any other: a set of sources (`LHC_*` environment variables, a properties
file, explicit arguments) resolves to one effective config, and every SDK
must resolve it the same way. That makes it a natural fourth area: a
`resolve` testee verb prints the resolved config as JSON, and the shared
`LHC_*` key list is the denominator. The need is proven, not hypothetical:
sdk-java layered environment variables under explicit arguments while
sdk-js ignored the environment entirely, and neither SDK's own unit tests
could see the disagreement, because each SDK's tests only check its own
opinion (found and fixed during v1). Retry behavior splits across tiers:
its defaults are config, the retry decision table (error code plus
RetryInfo in, retry plus delay out) could be a small data area of its own,
and live retry timing belongs to the runtime area below, adjudicated by
the server. Purely language-local glue, such as how a runtime constructs
its TLS credential objects, stays in unit tests; the cross-SDK contract is
which mode the inputs select, and that is config.

**Runtime conformance (v2).** The worker layer could join as a second case
type: a deliberately small inventory of scenario cases (task execution,
retries, interrupts, user tasks) whose outcomes are adjudicated by the
server's own records, never by any SDK's opinion of itself. This follows
the gRPC/Temporal split the research found: data cases in the hundreds,
scenario cases in the tens, because each scenario case costs every SDK a
real implementation.

**Channels (v2).** New cases land in a `develop` channel (report-only for
everyone) and graduate to `stable` (gated) once two SDKs pass them: the
Ethereum mechanism by which a corpus grows without ever breaking a
participant.

**The client layer.** The hand-written convenience layer above the
generated RPC stubs could join once the wfsdk pattern is proven org-wide;
the generated stub surface itself is parity-by-construction and needs no
cases.

**N-way agreement (v3).** With three or more SDKs green, pairwise
cross-validation (the Arrow model) could adjudicate disagreements without
privileging any reference: the moment "gold standard" becomes an earned
streak rather than a definition. Randomized dual-compile plugs in here
(and its two-SDK form already shipped; see the implementation note):
generated workflows compiled by every SDK, with any divergence shrunk into
a new frozen case.

**Server-side canon validation.** A CI step that submits every fixture to
a real server and asserts acceptance: the canon's own test, closing the
loop on the one thing a reference implementation cannot prove about
itself. (The fuzz `--register` flag is the first slice of this.)

**Custody.** The suite's natural end-state owner would be the server team,
since the canon encodes their protocol's meaning; the transfer path is in
[adoption.md](./adoption.md).
