# The LittleHorse SDK Conformance Suite

One shared exam for every LittleHorse SDK. The suite holds a frozen corpus
of test cases with canonical expected answers. Each participating SDK
provides a small *testee* program that answers the cases, and an
SDK-neutral runner grades every answer and publishes the results as a
matrix. Every SDK takes the exam, including sdk-java, the reference that
produced the answer key in the first place.

Design rationale and governance:
[proposals/sdk-conformance/](../proposals/sdk-conformance/README.md).

## If you are new here

Three facts about LittleHorse are enough to understand this suite.

First, an SDK here is a *compiler* as much as a client library. When a user
writes a workflow in Java or TypeScript, the SDK turns that code into one
protobuf message (a `PutWfSpecRequest`) and sends it to the server. The
server only ever sees the message, never the user's code. So if two SDKs
produce the same message for the same workflow, they behave identically at
runtime by construction.

Second, that property is fragile. Five SDKs in five languages can each pass
their own tests while quietly disagreeing about what a workflow, a value,
or an event definition compiles to. Nothing errors when that happens; data
and events just stop matching across languages, far from the cause.

Third, this suite exists to make such disagreement impossible to miss. The
expected answers are frozen files generated from sdk-java and reviewed
like any other code. Every SDK, sdk-java included, is graded against them
on every run. A drifting SDK turns its own column red.

The suite currently has three areas (subjects of examination):

1. **wfsdk**: workflow compilation. Does your builder produce the
   canonical `PutWfSpecRequest` for each feature?
2. **serde**: value conversion. Does your SDK encode a string, an int, a
   timestamp, and so on into the same `VariableValue` proto as everyone
   else?
3. **registrations**: the paperwork a workflow files besides its WfSpec,
   such as the event definitions it promises to create and the task
   definitions it requires.

## Layout

```text
sdk-conformance/
├── README.md              this file
├── FUZZ.md                the random dual-compile check (no canon involved)
├── runner/                SDK-neutral grading: plain Node, builds nothing
│   ├── all.mjs            the whole suite as one command, with a report
│   ├── freshness.mjs      corpus self-checks + coverage ratchets
│   ├── run.mjs            runs every testee, grades, writes results/
│   ├── compare.mjs        semantic JSON equality + diff printing
│   ├── matrix.mjs         regenerates MATRIX.md (--check = staleness gate)
│   ├── fuzz.mjs           seeded random workflows, cross-compared
│   └── lib.mjs            shared helpers
├── testees.json           registry: sdk → the command that runs its testee
├── areas/                 THE CORPUS, one directory per area (frozen canon)
│   ├── wfsdk/
│   │   ├── rules.md       the recipe every workflow builder must implement
│   │   ├── manifest.json  case registry: id, title, level, covers, variants
│   │   ├── surface.json   the reference builder's public capabilities
│   │   ├── exemptions.yaml  capabilities excused from needing a case
│   │   └── cases/<id>/    scenario.md + base.json + feature.json
│   ├── serde/
│   │   ├── rules.md       the typed-input recipe
│   │   ├── manifest.json  minted: id, level, typed input per case
│   │   ├── surface.json   minted: the VariableValue oneof arms
│   │   ├── exemptions.yaml  arms excused from needing a case
│   │   └── cases/<id>.json  the canonical VariableValue for that input
│   └── registrations/     same shape as wfsdk; see its rules.md
├── ledgers/<sdk>.yaml     per-SDK excuses: todo (debt) / not_applicable
├── results/<sdk>.json     machine outcomes per case, stamped with revision
└── MATRIX.md              generated report card, never edited by hand
```

## Running the suite

Build the testees with their own toolchains:

```bash
./gradlew :sdk-java-conformance:installDist
```

```bash
npm --prefix sdk-js run build && npm --prefix sdk-js run build:conformance
```

Then run everything with one command:

```bash
node sdk-conformance/runner/all.mjs
```

The gates can also run individually: `freshness.mjs` (corpus self-checks),
`run.mjs` (grading), `matrix.mjs` (report card; `--check` fails when it is
stale relative to results/), and `fuzz.mjs` (random dual-compile; see
[FUZZ.md](./FUZZ.md)). `freshness` and `run` exit nonzero on any gate
failure.

## The testee contract (normative)

To join the suite, an SDK registers one command in `testees.json`. The
command must implement these verbs:

```text
testee list
    Print the case ids this SDK implements, one per line, to stdout.
    Exit 0. Must cover every area's cases the SDK implements.

testee compile --case <id> --variant <base|feature>
    Build the workflow that the case's scenario.md describes (with or
    without the feature under test, per the variant) using the SDK's own
    workflow builder, and print the compiled PutWfSpecRequest as proto
    JSON to stdout. Nothing else may be written to stdout. Exit 0 on
    success, nonzero with a message on stderr on error.

testee registrations --case <id> --variant <base|feature>
    Build the case's workflow, compile it, and print the registrations
    document (registrations/rules.md, G1): the side-registration protos
    and required-names sets. Same stdout and exit discipline.

testee convert --type <t> [--value <v>]
    Map the typed input (serde/rules.md, S1) to a native value, run it
    through the SDK's value converter, and print the resulting
    VariableValue as proto JSON to stdout. Same discipline.

testee fuzz --seed <uint32> --ops <n>
    Generate the seeded random workflow defined in FUZZ.md and print its
    compiled PutWfSpecRequest. An optional trailing --register flag asks
    the testee to also register the workflow with the server named by the
    SDK's usual configuration and exit nonzero on rejection.
```

Optional accelerator verbs let one process answer everything; the runner
probes for them and falls back to per-case spawns. `compile-all` and
`registrations-all` print `{"<case>/<variant>": <answer JSON>, ...}` for
their areas, and `convert-batch` reads one `{"id","type","value"}` JSON
object per stdin line and prints `{"<id>": <VariableValue JSON>, ...}`.

Comparison is semantic. The runner parses both sides and compares content,
so key order and whitespace never matter. Emit default values (the canon
does), and emit int64 fields as JSON strings per proto3 JSON.

The testee is built by its SDK's own toolchain; the runner never builds
anything. Each registered SDK also needs a ledger file
(`ledgers/<sdk>.yaml`), and empty lists are fine:

```yaml
todo: []            # cases this SDK will pass eventually (tracked debt)
not_applicable: []  # cases this SDK can never take, with reasons (rare)
```

The ledger is a two-way ratchet. An unexcused failure fails the run, and
an excused case that unexpectedly passes also fails the run until the
stale excuse is deleted.

## Changing the canon

The fixtures, the surfaces, and the serde manifest are generated from
sdk-java by the reference testee's `mint` verb and then frozen:

```bash
sdk-java/conformance/build/install/sdk-java-conformance/bin/sdk-java-conformance mint sdk-conformance
```

Regenerating is only ever done inside a PR, where the fixture diff is the
review surface. A new case lands in one PR with its manifest entry,
scenario, fixtures, and a `todo` ledger line for every SDK that does not
pass it yet, so the suite always lands green and debt is visible from
birth.

## Glossary

Plain-English definitions for every term of art this suite uses. When a
definition and a file disagree, the file wins; report the bug here.

- **area**: one subject the suite examines; a directory under
  [areas/](./areas/) carrying the five standard files (`rules.md`,
  `manifest.json`, `surface.json`, `exemptions.yaml`, `cases/`). Areas are
  *discovered* from the directory listing, and an area the runner cannot
  grade fails freshness. There are three: `wfsdk`, `serde`, and
  `registrations`.
- **arm**: one alternative of a proto `oneof` field. The serde area's
  surface is the list of `VariableValue`'s arms (`str`, `int`,
  `utcTimestamp`, and so on): every encoding a value can have.
- **base / feature**: the two variants of a probe pair. Base is the
  minimal workflow *without* the feature under test; feature is the same
  workflow *with* exactly that feature added.
- **canon**: the frozen expected answers: every fixture plus the minted
  surfaces and the serde manifest. Canon changes only by reviewed
  regeneration (see **mint**), never by hand-editing.
- **case**: one exam question, with an id that is stable forever (ledgers
  and results key on it).
- **corpus**: the exam itself: everything under `areas/`. Shared and
  opinion-free; no SDK's excuses or outcomes live in it. The corpus is the
  exam, the ledgers are the excuses, the results are the report card.
- **corpus revision**: the git hash of the last commit touching `areas/`
  (`+dirty` while edited), stamped into every results file so outcomes are
  only ever compared against the same exam.
- **denominator**: the independently derived list of everything an area's
  exam must cover; lives in that area's `surface.json`. It makes
  completeness a checkable fact instead of a hope.
- **drift gate**: the (planned) CI step that re-runs **mint** and fails on
  any diff, so the checked-in canon can never silently lag the reference.
- **excuse**: a ledger or exemption entry: a written, reasoned statement
  of why a case or denominator entry is not currently green. Every excuse
  is ratcheted (see **two-way ratchet**).
- **exemptions** (`areas/<area>/exemptions.yaml`): corpus-level excuses:
  denominator entries that have no case yet (`todo`) or never will
  (`not_applicable`), each with a reason. Distinct from **ledgers**, which
  excuse one SDK, not the corpus.
- **fixture**: one frozen expected-answer file (for example
  `cases/sleep-seconds/feature.json`): the canonical proto JSON a testee's
  output is compared against.
- **freshness**: the corpus's self-checks
  ([runner/freshness.mjs](./runner/freshness.mjs)): area conventions,
  manifest and cases agreement, fixture parseability, the vacuity guard,
  and the coverage ratchet reconciling each surface against cases and
  exemptions.
- **fuzz**: the random dual-compile check ([FUZZ.md](./FUZZ.md)). Each
  testee independently generates the same seeded random workflow and the
  runner cross-compares the outputs. There is no canon; agreement itself
  is the verdict. With `--register`, a live server must also accept each
  workflow.
- **gated / report-only**: the two participating rungs for an SDK. Gated
  means suite failures fail that SDK's CI; report-only means results
  publish but nothing blocks. Today the suite's CI job is report-only for
  everyone; gating is each team's opt-in.
- **ledger** (`ledgers/<sdk>.yaml`): one SDK's excuses, per case: `todo`
  (will pass eventually) or `not_applicable` (can never take the case,
  with the reason). The runner still runs excused cases when it can; that
  is how stale excuses are caught.
- **level** (`required` / `recommended`): per-case strictness. Only
  `required` cases can fail a gated SDK's run; `recommended` outcomes
  appear in results and the matrix but never gate.
- **matrix** ([MATRIX.md](./MATRIX.md)): the generated report card, one
  table per area: cases as rows, SDKs as columns, every cell an executed
  outcome. Never edited by hand; CI fails if it goes stale.
- **mint**: the reference testee's verb that deterministically regenerates
  the canon (fixtures, surfaces, serde manifest) from the reference SDK.
  Same input, same bytes, which is what makes the drift gate possible.
- **outcome**: a case's grade for one SDK: `PASS`, `FAIL` (ran and
  diverged), `SKIP` (excused by the ledger), or `MISSING` (the testee does
  not offer the case and no excuse exists).
- **probe pair**: the wfsdk case shape: one minimal workflow compiled
  twice (base and feature), so the diff between the two fixtures *is* the
  feature's entire effect. It proves the feature was expressed, not merely
  that outputs happened to match.
- **reference (implementation)**: sdk-java, the SDK that mints the canon.
  After minting it holds no privilege; it is graded against the frozen
  canon like every other SDK, so drift in the reference reddens its own
  column.
- **results** (`results/<sdk>.json`): the machine record of one SDK's
  outcomes at one corpus revision. Generated by the runner; the matrix is
  rendered from these.
- **rules.md** (one per area): the area's normative recipe: numbered rules
  (`R1`, `S1`, `G1`, and so on) that scenarios and SDK code cite. The
  fixtures are the authority; the rules restate them so no implementer has
  to reverse-engineer another SDK.
- **scenario** (`cases/<id>/scenario.md`): the prose half of a
  behavior-shaped case: what to build, precise enough to implement without
  reading another SDK's source. Serde cases are data-shaped and need none.
- **semantic comparison** ([runner/compare.mjs](./runner/compare.mjs)):
  how answers are judged. Both sides are parsed and compared by content,
  so key order and whitespace never matter, and `null` equals absent
  (proto3 JSON treats null as "field not present").
- **serde**: serialize/deserialize: each SDK's translator between native
  language values and the `VariableValue` proto everything on the wire
  uses. Area 2 examines it.
- **single-variant case**: a case with no meaningful base because the
  capability under test is the precondition of any output at all
  (`workflow-minimal`: you cannot subtract "having a workflow"). It ships
  one `feature` fixture and is declared in the manifest, so reviewers see
  every use.
- **surface** (`areas/<area>/surface.json`): the area's denominator file.
  wfsdk's lists the reference builder's public capabilities
  (`Class#method`); serde's lists the proto's arms. Same role, different
  ground truth; each area's `rules.md` says which.
- **testee**: the small command-line program each SDK registers in
  [testees.json](./testees.json). It answers cases through the SDK's own
  code and knows nothing about expected values. Built by its SDK's own
  toolchain (the "two homes" rule: runner in the suite, testees in the
  SDKs).
- **two-way ratchet**: the discipline keeping excuses honest. An unexcused
  failure fails the run, and an excused case that *passes* also fails the
  run until the stale excuse is deleted. Excuses can never silently
  outlive their reason.
- **units** (inside results): the per-part grades within one case. A wfsdk
  pair has units `base` and `feature`; a serde case has one unit,
  `convert`. A case passes only when every unit passes.
- **vacuity guard**: the freshness assertion that a pair's base and
  feature fixtures differ. A pair that toggles nothing proves nothing, and
  is rejected instead of counting as coverage.
- **variant**: one graded compilation of a case (`base` or `feature`),
  declared per case in the manifest.
- **wfsdk**: the workflow-builder layer of an SDK: the user-facing API
  that compiles workflow code into a `PutWfSpecRequest`. Area 1 examines
  it.
