# LittleHorse SDK Conformance Suite

This directory contains the conformance tests that keep every LittleHorse
SDK compiling workflows the same way. The expected outputs are frozen
files generated from sdk-java, and every SDK is graded against them on
every run, sdk-java included. Two SDKs that pass compile the same
workflows into the same protos, so they behave the same at runtime.

[QUICKSTART.md](./QUICKSTART.md) will introduce you to running the suite,
breaking it on purpose, and adding a feature to the exam.

## Run the suite

Build every testee, then run everything:

```bash
node sdk-conformance/runner/build.mjs
```

```bash
node sdk-conformance/runner/suite.mjs
```

The build command runs each SDK's own build, as registered in
[testees.json](./testees.json).

Each gate of the suite also runs on its own:

```bash
node sdk-conformance/runner/freshness.mjs   # corpus self-checks
node sdk-conformance/runner/run.mjs         # grade every SDK on every case
node sdk-conformance/runner/matrix.mjs      # regenerate MATRIX.md (--check fails if stale)
node sdk-conformance/runner/fuzz.mjs        # random dual-compile, see FUZZ.md
```

## How it works

An SDK here is really a compiler. You write a workflow in Java or
TypeScript, and the SDK turns it into one protobuf message, a
`PutWfSpecRequest`. The server only ever sees that message. If two SDKs
produce the same message for the same workflow, they agree by
construction.

This suite freezes the expected messages as files, minted from sdk-java
and reviewed like any other code, and grades every SDK against them. An
SDK that drifts turns its own column red in [MATRIX.md](./MATRIX.md).
That includes sdk-java.

Three areas are covered so far. `wfsdk` covers workflow compilation (119
cases), `serde` covers converting values like strings and timestamps into
`VariableValue` protos (20 cases), and `registrations` covers the event
definitions and required names a workflow files alongside its spec (8
cases). Each area directory has a `rules.md` outlining the process for
recreating the output, so a new SDK never has to reverse engineer the
results.

## Layout

```text
sdk-conformance/
├── README.md
├── QUICKSTART.md          run it, break it, extend it
├── FUZZ.md                the random dual-compile check
├── runner/                grading scripts in plain Node
├── testees.json           sdk → the command that runs its testee
├── areas/<area>/          the frozen corpus (rules.md, manifest.json,
│                          surface.json, exemptions.yaml, cases/)
├── ledgers/<sdk>.yaml     per-SDK excuses (todo or not_applicable)
├── results/<sdk>.json     outcomes, stamped with the corpus revision
└── MATRIX.md              the generated report card, never edited by hand
```

## Adding a case

When a new feature is created in the reference SDK (Java), the feature's
output must be captured, and `freshness.mjs` forces this. It detects
public builder capabilities with no case and no written excuse, which
fails the test.

Build the feature in sdk-java, add a probe pair (the same minimal
workflow compiled once without the feature and once with it, so the diff
between the two outputs is exactly what the feature does), then
regenerate the canon:

```bash
node sdk-conformance/runner/mint.mjs
```

## Adding an SDK

An SDK joins by providing a testee, a small command-line program that can
retrieve the SDK's answers to the suite's cases.

1.  Register its command in [testees.json](./testees.json)
2.  Add a `ledgers/<sdk>.yaml`

The section below outlines what the testee command-line program should provide.

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

Optional accelerator verbs let one process answer everything, and the
runner probes for them before falling back to per-case spawns.
`compile-all` and `registrations-all` print
`{"<case>/<variant>": <answer JSON>, ...}` for their areas.
`convert-batch` reads one `{"id","type","value"}` JSON object per stdin
line and prints `{"<id>": <VariableValue JSON>, ...}`.

Comparison is semantic. The runner parses both sides and compares
content, so key order and whitespace never matter. Emit default values
(the canon does), and emit int64 fields as JSON strings per proto3 JSON.

Each testee is built by its own SDK's toolchain. The runner never builds
anything itself, and `runner/build.mjs` only shells the build command each
SDK registers. The ledger holds the SDK's written excuses, and empty
lists are fine:

```yaml
todo: [] # cases this SDK will pass eventually (tracked debt)
not_applicable: [] # cases this SDK can never take, with reasons (rare)
```

Excuses are checked in both directions. An unexcused failure fails the
run, and an excused case that unexpectedly passes also fails it until the
stale line is deleted. Today CI runs the suite report-only for everyone.
Gating is each SDK team's opt-in.
