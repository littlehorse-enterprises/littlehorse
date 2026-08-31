# The LittleHorse SDK Conformance Suite

One shared exam for every LittleHorse SDK. The suite holds a frozen corpus
of test cases with canonical expected answers; each participating SDK
provides a tiny *testee* program that answers the cases; an SDK-neutral
runner grades every answer and publishes the results as a matrix. Every SDK
— including sdk-java, the reference that minted the canon — is an examinee.

Design rationale and governance: [proposals/sdk-conformance/](../proposals/sdk-conformance/README.md).

## Layout

```text
conformance/
├── runner/            SDK-neutral grading (plain Node, builds nothing)
│   ├── freshness.mjs  corpus self-checks + surface coverage ratchet
│   ├── run.mjs        runs every testee, grades, writes results/
│   ├── compare.mjs    semantic JSON equality + diff printing
│   └── matrix.mjs     regenerates MATRIX.md (--check = staleness gate)
├── testees.json       registry: sdk → the command that runs its testee
├── areas/             THE CORPUS — one directory per area (frozen canon)
│   ├── wfsdk/         area 1: workflow compilation
│   ├── manifest.json  one entry per case: id, title, level, covers, variants
│   ├── surface.json   reference SDK capabilities the corpus must cover
│   ├── rules.md       the normative recipe: every rule a builder must
│   │                  implement, each pinned to the fixture enforcing it
│   ├── exemptions.yaml  capabilities excused from needing a case, with reasons
│   └── cases/<id>/    scenario.md + base.json + feature.json
│   └── serde/         area 2: value → VariableValue conversion
│   ├── rules.md       the typed-input recipe (see it for everything below)
│   ├── manifest.json  minted: one entry per case — id, level, typed input
│   ├── surface.json      minted: the VariableValue oneof arms (the denominator)
│   ├── exemptions.yaml  arms excused from needing a case, with reasons
│   └── cases/<id>.json  the canonical VariableValue for that input
├── ledgers/<sdk>.yaml per-SDK excuses: todo (debt) / not_applicable (never)
├── results/<sdk>.json machine outcomes per case, stamped with corpus revision
└── MATRIX.md          generated report card — never edited by hand
```

## Running the suite

Build the testees with their own toolchains, then run the graders:

```bash
./gradlew :sdk-java-conformance:installDist
```

```bash
npm --prefix sdk-js run build && npm --prefix sdk-js run build:conformance
```

```bash
node conformance/runner/freshness.mjs
```

```bash
node conformance/runner/run.mjs
```

```bash
node conformance/runner/matrix.mjs
```

`freshness` and `run` exit nonzero on any gate failure; `matrix.mjs --check`
fails if MATRIX.md is stale relative to results/.

## The testee contract (normative)

To join the suite an SDK registers one command in `testees.json`. The
command must implement exactly two verbs:

```text
testee list
    Print the case ids this SDK implements, one per line, to stdout.
    Exit 0.

testee compile --case <id> --variant <base|feature>
    Build the workflow that case's scenario.md describes (with or without
    the feature under test, per the variant) using the SDK's own workflow
    builder, and print the compiled PutWfSpecRequest as proto JSON to
    stdout. Nothing else may be written to stdout. Exit 0 on success,
    nonzero with a message on stderr on error.

testee convert --type <t> [--value <v>]
    Map the typed input (serde/rules.md, S1) to a native value, run
    it through the SDK's value converter, and print the resulting
    VariableValue as proto JSON to stdout. Same stdout/exit discipline.
```

`list` must cover every area's cases the SDK implements.

Comparison is semantic: the runner parses both sides and compares content,
so key order and whitespace never matter. Emit default values (the canon
does), and emit int64 fields as JSON strings per proto3 JSON.

The testee is built by its SDK's own toolchain; the runner never builds
anything. Each registered SDK also needs a ledger file (`ledgers/<sdk>.yaml`)
— empty lists are fine:

```yaml
todo: []            # cases this SDK will pass eventually (tracked debt)
not_applicable: []  # cases this SDK can never take, with reasons (rare)
```

The ledger is a two-way ratchet: an unexcused failure fails the run, and an
excused case that unexpectedly passes also fails the run until the stale
excuse is deleted.

## Changing the canon

The fixtures and `surface.json` are minted from sdk-java by the reference
testee's `mint` verb and then frozen:

```bash
sdk-java/conformance/build/install/sdk-java-conformance/bin/sdk-java-conformance mint conformance
```

Regenerating is only ever done inside a PR, where the fixture diff is the
review surface. A new case lands in one PR with its manifest entry,
scenario, fixtures, and a `todo` ledger line for every SDK that does not
pass it yet — the suite always lands green.

## Glossary

Plain-English definitions for every term of art this suite uses. When a
definition and a file disagree, the file wins — report the bug here.

- **area** — one subject the suite examines; a directory under
  [areas/](./areas/) carrying the five standard files (`rules.md`,
  `manifest.json`, `surface.json`, `exemptions.yaml`, `cases/`). Areas are
  *discovered* from the directory listing, and an area the runner cannot
  grade fails freshness. v1 has two: `wfsdk` and `serde`.
- **arm** — one alternative of a proto `oneof` field. The serde area's
  surface is the list of `VariableValue`'s arms (`str`, `int`,
  `utcTimestamp`, …): every encoding a value can have.
- **base / feature** — the two variants of a probe pair: the minimal
  workflow *without* the feature under test, and the same workflow *with*
  exactly that feature added.
- **canon** — the frozen expected answers: every fixture plus the minted
  surfaces and the serde manifest. Canon changes only by reviewed
  regeneration (see **mint**), never by hand-editing.
- **case** — one exam question, with an id that is stable forever (ledgers
  and results key on it).
- **corpus** — the exam itself: everything under `areas/`. Shared and
  opinion-free — no SDK's excuses or outcomes live in it. *The corpus is
  the exam; the ledgers are the excuses; the results are the report card.*
- **corpus revision** — the git hash of the last commit touching `areas/`
  (`+dirty` while edited), stamped into every results file so outcomes are
  only ever compared against the same exam.
- **denominator** — the independently derived list of everything an area's
  exam must cover; lives in that area's `surface.json`. It makes
  completeness a checkable fact instead of a hope.
- **drift gate** — the (planned) CI step that re-runs **mint** and fails on
  any diff, so the checked-in canon can never silently lag the reference.
- **excuse** — a ledger or exemption entry: a written, reasoned reason a
  case or denominator entry is not currently green. Every excuse is
  ratcheted (see **two-way ratchet**).
- **exemptions** (`areas/<area>/exemptions.yaml`) — corpus-level excuses:
  denominator entries that have no case yet (`todo`) or never will
  (`not_applicable`), each with a reason. Distinct from **ledgers**, which
  excuse one SDK, not the corpus.
- **fixture** — one frozen expected-answer file (e.g.
  `cases/sleep-seconds/feature.json`): the canonical proto JSON a testee's
  output is compared against.
- **freshness** — the corpus's self-checks
  ([runner/freshness.mjs](./runner/freshness.mjs)): area conventions,
  manifest↔cases agreement, fixture parseability, the vacuity guard, and
  the coverage ratchet reconciling each surface against cases and
  exemptions.
- **gated / report-only** — the two participating rungs for an SDK: gated
  means suite failures fail that SDK's CI; report-only means results
  publish but nothing blocks. (v1 ships both SDKs gated.)
- **ledger** (`ledgers/<sdk>.yaml`) — one SDK's excuses, per case: `todo`
  (will pass eventually) or `not_applicable` (can never take the case, with
  the reason). The runner still runs excused cases when it can — that is
  how stale excuses are caught.
- **level** (`required` / `recommended`) — per-case strictness: only
  `required` cases can fail a gated SDK's run; `recommended` outcomes
  appear in results and the matrix but never gate.
- **matrix** ([MATRIX.md](./MATRIX.md)) — the generated report card, one
  table per area: cases as rows, SDKs as columns, every cell an executed
  outcome. Never edited by hand; CI fails if it goes stale.
- **mint** — the reference testee's verb that deterministically regenerates
  the canon (fixtures, surfaces, serde manifest) from the reference SDK.
  Same input, same bytes — which is what makes the drift gate possible.
- **outcome** — a case's grade for one SDK: `PASS`, `FAIL` (ran and
  diverged), `SKIP` (excused by the ledger), or `MISSING` (the testee does
  not offer the case and no excuse exists).
- **probe pair** — the wfsdk case shape: one minimal workflow compiled
  twice (base and feature), so the diff between the two fixtures *is* the
  feature's entire effect. Proves the feature was expressed, not merely
  that outputs happened to match.
- **reference (implementation)** — sdk-java: the SDK that mints the canon.
  After minting it holds no privilege — it is graded against the frozen
  canon like every other SDK, so drift in the reference reddens its own
  column.
- **results** (`results/<sdk>.json`) — the machine record of one SDK's
  outcomes at one corpus revision. Generated by the runner; the matrix is
  rendered from these.
- **rules.md** (one per area) — the area's normative recipe: numbered rules
  (`R1`…, `S1`…) that scenarios and SDK code cite. The fixtures are the
  authority; the rules restate them so no implementer reverse-engineers
  another SDK.
- **scenario** (`cases/<id>/scenario.md`, wfsdk only) — the prose half of a
  behavior-shaped case: what to build, precise enough to implement without
  reading another SDK's source. Serde cases are data-shaped and need none.
- **semantic comparison** ([runner/compare.mjs](./runner/compare.mjs)) —
  how answers are judged: both sides parsed and compared by content, so key
  order and whitespace never matter, and `null` equals absent (proto3 JSON
  treats null as "field not present").
- **serde** — **ser**ialize/**de**serialize: each SDK's translator between
  native language values and the `VariableValue` proto everything on the
  wire uses. Area 2 examines it.
- **single-variant case** — a case with no meaningful base because the
  capability under test is the precondition of any output at all
  (`workflow-minimal`: you cannot subtract "having a workflow"). Ships one
  `feature` fixture; declared in the manifest, so reviewers see every use.
- **surface** (`areas/<area>/surface.json`) — the area's denominator file.
  wfsdk's lists the reference builder's public capabilities
  (`Class#method`); serde's lists the proto's arms. Same role, different
  ground truth — each area's `rules.md` says which.
- **testee** — the small command-line program each SDK registers in
  [testees.json](./testees.json); it answers cases through the SDK's own
  code and knows nothing about expected values. Built by its SDK's own
  toolchain (the "two homes" rule: runner in the suite, testees in the
  SDKs).
- **two-way ratchet** — the discipline keeping excuses honest: an unexcused
  failure fails the run, and an excused case that *passes* also fails the
  run until the stale excuse is deleted. Excuses can never silently outlive
  their reason.
- **units** (inside results) — the per-part grades within one case: a wfsdk
  pair has units `base` and `feature`; a serde case has one unit,
  `convert`. A case passes only when every unit passes.
- **vacuity guard** — the freshness assertion that a pair's base and
  feature fixtures differ. A pair that toggles nothing proves nothing, and
  is rejected instead of counting as coverage.
- **variant** — one graded compilation of a wfsdk case (`base` or
  `feature`), declared per case in the manifest.
- **wfsdk** — the workflow-builder layer of an SDK: the user-facing API
  that compiles workflow code into a `PutWfSpecRequest`. Area 1 examines
  it.
