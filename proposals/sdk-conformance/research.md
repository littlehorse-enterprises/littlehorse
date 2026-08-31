# Proposal: SDK Conformance — the Research

- Status: **Record** (compiled 2026-08-30)
- Parent: [README.md](./README.md)

Before designing our suite, we studied how fourteen real multi-language
ecosystems keep many implementations provably in agreement — directly from
their repositories and CI configurations, with the most load-bearing claims
independently re-verified against the sources (44 of 46 confirmed; two minor
corrections applied). This file records what they do, what worked, and what
rotted, so our design decisions in [design.md](./design.md) are citations
rather than opinions.

## Contents

- [The five most instructive systems](#the-five-most-instructive-systems)
- [The rest, briefly](#the-rest-briefly)
- [Seven patterns that repeat](#seven-patterns-that-repeat)
- [Method](#method)

## The five most instructive systems

**Protocol Buffers conformance suite** (`protocolbuffers/protobuf`,
`conformance/`). One C++ *runner* owns every test case and every expected
value. Each language writes a tiny *testee* (the C++ one is ~150 lines) that
loops: read a length-prefixed request on stdin, parse and re-serialize, write
the response to stdout. The runner judges answers **semantically** — it
parses the testee's output back with the reference implementation and
message-diffs it, so legal alternate encodings pass. Strictness is encoded in
the case name (`Required.*` always gates; `Recommended.*` only under a flag),
and each implementation carries an expected-failure ledger
(`failure_list_java.txt`, …) that is a **two-way ratchet**: an unlisted
failure fails CI, and a listed test that unexpectedly *passes* also fails CI,
with the exact fix command printed. Their scar: unenforced `Recommended`
entries rotted into multi-year, 11-kilobyte parking lots — enforcement is
what keeps ledgers honest.

**Temporal features repo** (`temporalio/features`) — a workflow engine
keeping seven SDKs honest; our closest cousin. Each case is a directory: a
prose spec (`README.md`) plus one idiomatic implementation per language
(`feature.go`, `feature.java`, `feature.ts`, …) against a uniform harness
contract. A single Go CLI runs any language's cases against an ephemeral dev
server; testees stream results back as JSON lines
(`{Name, Outcome: PASSED|FAILED|SKIPPED}`). The suite exposes **reusable
per-language CI workflows**, so each SDK repo's own PR pipeline calls the
shared suite against its head ref — one suite, N gates, each team choosing
when it becomes blocking. Their weakness: a language that hasn't implemented
a case simply has no file, so coverage gaps accumulate invisibly — no report
tracks the matrix. (Temporal also runs a shared Rust core under four of its
SDKs — parity by construction — yet still needs the cross-SDK suite, because
Java and Go never moved onto the core and the per-language wrapper layers are
where user-visible divergence lives.)

**Ethereum execution-spec-tests.** Cases are authored once, then *filled*
into language-neutral JSON fixtures by a blessed **reference
implementation** — clients never own the oracle, they only consume. Fixtures
ship as **versioned release tarballs** in two channels: `fixtures_stable`
(each client pins a release and gates its CI on it) and `fixtures_develop`
(report-only, watched). A community server continuously runs every client
against everything and publishes public dashboards. "Reference generates,
everyone consumes" is exactly our plan to mint canon from sdk-java
([design.md](./design.md#the-canon-lifecycle)), validated at ecosystem
scale; the stable/develop split is the graduation mechanism for new cases.

**AWS Smithy protocol tests** (`smithy-lang/smithy`). Cases are pure data in
the API model — each with a **stable ID**, typed input, and exact expected
wire output. Every language's code generator turns them into ordinary native
tests that run in normal CI like any unit test; each SDK keeps a
skip/expected-fail list keyed by case ID with a reason string; the canon is a
versioned artifact each SDK pins, so adopting new cases is an explicit,
reviewable bump. Their scar: the canonical cases themselves ship bugs
occasionally (one incident: 36 bad cases), and a bad case fails every SDK at
once — every code generator grew an escape-valve list because of it.

**web-platform-tests + wpt.fyi** (the browsers). The cleanest known answer
to "one implementation gated, another report-only": three cleanly separated
artifacts. The **corpus** (shared, opinion-free). Each browser's
**expectation ledger** (in its own tree, with vocabulary richer than skip —
expected-FAIL, disabled, intermittent, platform-conditional — updated from
run logs by a one-command tool). And uploaded **normalized results**, every
outcome stamped with the corpus revision, feeding a dashboard that only
compares runs on the same revision. Gated browsers diff against their ledger
in CI; everyone else just uploads results.

## The rest, briefly

| System | Mechanism in one line | The lesson we take |
| --- | --- | --- |
| gRPC interop | ~20 prose-specified scenario cases; every language implements a client+server honoring one CLI contract; a 13×10 docker matrix | prose cases only for scenario-shaped behavior, kept deliberately few — each costs N re-implementations |
| Apache Arrow | deterministic generator emits neutral JSON cases at test time; every producer×consumer pair cross-validated | with ≥3 implementations, pairwise agreement can replace a privileged reference |
| Apache Avro | one schema, every language writes a file, everyone reads everyone's | the cautionary tale: non-required CI + one orchestrator building every toolchain = silent matrix rot (their C++ line is literally commented out) |
| JSON-Schema-Test-Suite | pure-data cases, one-boolean oracle, consumed by dozens of validators | the smallest oracle that answers the question survives longest |
| Test262 (JavaScript) | the corpus ships no runner — a normative document specifies the runner contract; 18+ engines implement it natively | write the runner contract as a document; expected-failure ledgers belong in the consumer's repo, keeping the corpus opinion-free |
| html5lib-tests | a canonical text serialization of the parse tree is the comparator | proto JSON gives us this comparator for free; also: an under-specified error oracle is cheap now and expensive to retrofit |
| sass-spec | reference implementation regenerates expectations via a one-command reviewed flow; `:todo:` vs `:ignore_for:` ledgers | the exemption vocabulary that survives is "not yet" ≠ "never"; conflating them poisoned their ledger |
| OpenTelemetry | a 12-language compliance matrix generated from per-language YAML ledgers — self-attested | generate the matrix from data files, but back every cell with an executed test; self-reported cells measure claims, not code |
| CloudEvents | fixtures + shared Gherkin scenarios, per-SDK step shims — archived 2026 after 2 of 10 SDKs adopted | the death mode: a suite outside each SDK's default test command dies, however good its design |

## Seven patterns that repeat

1. **Cases are data; expected values are files.** Wherever behavior is
   data-shaped, durable suites use language-neutral cases with stable IDs and
   frozen expected outputs. Prose or per-language code cases survive only for
   genuinely scenario-shaped behavior, in small numbers.
2. **A reference implementation minting the canon is the norm.** Protobuf's
   C++ runner, Ethereum's reference filler, sass's Dart implementation, AWS's
   core team. What matters is that regeneration is an explicit, reviewed
   event — not that the reference is infallible.
3. **The two-way ratchet beats the skip.** Skips go silent forever;
   expected-failure ledgers that also fail on unexpected *pass* shrink.
4. **Strictness is a per-case, per-SDK dial** — required/recommended levels,
   stable/develop channels, gated vs report-only participation.
5. **The per-language hook must be nearly free** (~150-line testees, a
   handful of CLI verbs, generated native tests). Where joining costs more,
   languages don't join.
6. **Conformance must ride each SDK's normal CI.** Every suite that died or
   rotted required someone to remember to run it.
7. **Results are a published artifact** — normalized per-case outcomes
   stamped with the corpus version. At our scale, a generated markdown matrix
   in the repo is enough.

## Method

Five parallel research passes studied the fourteen systems directly from
their repositories, documentation, and CI configuration; every claim above
derives from files actually retrieved. Two independent verification passes
then re-fetched sources for the 46 most load-bearing claims: 44 confirmed,
2 corrected in minor ways (a relocated documentation site; a nuance in how
Temporal's Rust core binds to some languages), with corrections applied
before this document was written.
