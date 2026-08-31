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
├── wfsdk/             THE CORPUS (frozen canon — changes need review)
│   ├── manifest.json  one entry per case: id, title, level, covers, variants
│   ├── surface.json   reference SDK capabilities the corpus must cover
│   ├── exemptions.yaml  capabilities excused from needing a case, with reasons
│   └── cases/<id>/    scenario.md + base.json + feature.json
├── ledgers/<sdk>.yaml per-SDK excuses: todo (debt) / not_applicable (never)
├── results/<sdk>.json machine outcomes per case, stamped with corpus revision
└── MATRIX.md          generated report card — never edited by hand
```

## Running the suite

Build the testees with their own toolchains, then run the graders:

```bash
./gradlew :sdk-java-conformance-testee:installDist
```

```bash
npm --prefix sdk-js run build
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
```

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
sdk-java/conformance-testee/build/install/sdk-java-conformance-testee/bin/sdk-java-conformance-testee mint conformance/wfsdk
```

Regenerating is only ever done inside a PR, where the fixture diff is the
review surface. A new case lands in one PR with its manifest entry,
scenario, fixtures, and a `todo` ledger line for every SDK that does not
pass it yet — the suite always lands green.
