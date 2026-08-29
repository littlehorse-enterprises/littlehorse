# Proposal: sdk-js Parity — the wfsdk Layer

- Status: **Draft** (partially implemented; see "What exists today")
- Scope: `sdk-js/src/wfsdk`, `sdk-js/src/feature-matrix`, `sdk-js/golden`,
  `sdk-js/golden/generator`
- Parent: [README.md](./README.md) (the doctrine and the layer map) ·
  Audit record: [audit.md](./audit.md) · Sequencing: [roadmap.md](./roadmap.md)

## Contents

- [Why the wfsdk is provable at all](#why-the-wfsdk-is-provable-at-all)
- [What exists today](#what-exists-today)
- [Known weaknesses (found by our own audit)](#known-weaknesses-found-by-our-own-audit)
- [Design 1: the freshness check — implemented](#design-1-the-freshness-check--implemented)
- [Design 2: probe pairs (micro-goldens)](#design-2-probe-pairs-micro-goldens)
- [Design 3: the reference workflows, demoted honestly](#design-3-the-reference-workflows-demoted-honestly)
- [Design 4: randomized dual-compile (endgame)](#design-4-randomized-dual-compile-endgame)
- [What remains judgment, on purpose](#what-remains-judgment-on-purpose)

## Why the wfsdk is provable at all

The wfsdk is the layer users call to *define* workflows —
`thread.declareStr(...)`, `thread.execute(...)`, `thread.doIf(...)`. The key
fact about it: **it never runs a workflow.** A user's thread function executes
exactly once, at registration time, and its only product is a
`PutWfSpecRequest` proto. The *server* executes that proto.

That makes the wfsdk a compiler, and compilers can be held to an unusually hard
standard:

> If sdk-js compiles a workflow to bytes identical to sdk-java's, runtime
> behavior is identical **by construction** — there is nowhere left for
> divergence to hide.

So for this layer, both doctrine questions get byte-grade answers:

- **What to test:** every public method across the 21 public types in
  `sdk-java/src/main/java/io/littlehorse/sdk/wfsdk/` (the `internal/` folder is
  implementation machinery users cannot call, and is excluded). `WorkflowThread`
  is the big one (~71 declared methods); `LHExpression` (~56) is mostly Java
  argument-type overloads that collapse to single methods in JS.
- **Whether it's correct:** sdk-java's own serialized output, checked in as
  fixtures ("goldens") that sdk-js must reproduce byte-for-byte.

## What exists today

**The enumeration lives in the test suite.** The Java surface was walked into
one test per capability in `sdk-js/src/feature-matrix/areas/`, each naming the
Java method it covers inside its own title:

```ts
test('conditionally execute a body — Java: WorkflowThread#doIf', () => { … })
```

Running the suite *is* the status: passed = ported and proven, `test.todo` =
missing, and a deleted entry shows up in a diff needing justification. The
citation format (`— Java: Class#method`) is what makes the whole design
mechanizable — it is the machine-readable link between our tests and Java's
surface.

**Goldens are the correctness oracle.** A small Java program
(`sdk-js/golden/generator`, gradle module `:sdk-js-golden-generator`) uses real
sdk-java to compile 14 reference workflows and serialize them into
`sdk-js/golden/*.json`. TypeScript twins of the same workflows
(`src/feature-matrix/harness/referenceWorkflows.ts`) must compile to
byte-identical protos, asserted by
`expectMatchesGolden` (JSON comparison first for a readable diff, proto
equality second as the authoritative check). Regeneration:

```sh
./gradlew :sdk-js-golden-generator:run --args="$(pwd)/sdk-js/golden"
```

Two protections already in force, both learned the hard way: `golden/` is in
`.prettierignore` (the formatter once rewrote the fixtures; tests kept passing
because the JSON still parsed — which is exactly why it was dangerous), and the
proto codegen plugin is pinned to `@protobuf-ts` (switching plugins invalidates
every fixture).

**Operational rule for the fixtures:** a reference workflow exists twice — the
Java original in the generator and its TypeScript twin — and editing one means
editing **both** and regenerating, in that order (Java first, so Java's output
remains the thing being matched). A few features currently have no golden at
all (type adapters, retention policies, format-string and dynamic task names);
the probe rollout below is what forces those gaps closed.

## Known weaknesses (found by our own audit)

Recorded with their current statuses in [audit.md](./audit.md); the designs
below exist to close them.

1. **The enumeration was hand-written once and nothing kept it fresh.** An
   audit found public Java methods with no entry — one of them
   (`UserTaskOutput#withOnCancellationException`) was even *implemented* in JS
   with zero tests, and a new Java method turned nothing red.
   **Closed 2026-08-28** by Design 1 below; the day-one triage it forced is
   recorded there.
2. **Many tests lean on fixtures that may not exercise their feature.** 57
   entries are a bare "the whole workflow matches its golden" assertion,
   resolving to only 12 distinct fixtures. Live example: the "spawn a child
   thread with input variables" test passes while the fixture records
   `"variables": {}` on every spawn node — the feature is absent from the
   fixture, so the byte-comparison is satisfied trivially. → Designs 2 and 3.

## Design 1: the freshness check — **implemented**

A deterministic comparison of two machine-produced lists, run as a normal Jest
test on every `npm test` (~0.3s, no Java installed). As built:

- **List #1 — what Java offers.**
  `golden/generator/.../SurfaceGenerator.java` uses reflection — Java's
  built-in ability to ask a class "what methods do you have?" — to list every
  public method of the 21 types, and saves the result as
  `sdk-js/golden/fixtures/java-surface.json` (committed; sorted; overloads
  collapsed to one entry per name, which shrank the surface to **144 distinct
  methods**). Regenerate with:

  ```sh
  ./gradlew :sdk-js-golden-generator:runSurface --args="$(pwd)/sdk-js/golden"
  ```

- **List #2 — what we cover.**
  `src/feature-matrix/harness/citations.ts` collects the
  `— Java: Class#method` citations from the test titles (including
  `test.todo` titles — a todo is visible coverage-in-progress). It handles the
  quirks counted in the real suite: comma-separated double citations, grouped
  citations like `castToInt etc.` (spelled out in an explicit alias table),
  and dot-vs-`#` separators on statics.
- **The comparison.** `src/feature-matrix/conformance/surface.test.ts`, four
  assertions: everything on list #1 is cited or exempted; every citation of a
  covered class names a real member (catches typos and Java renames); the
  exemption/alias file is alive (dead entries, stale entries for now-cited
  symbols, unexpanded `etc.` groups, and excuse-chains all fail); deprecated
  members are skipped but always printed.

**The one rule that keeps it honest:** a red has exactly two legal resolutions
— a `test.todo` (a real feature we owe; the banner shows the debt) or an
exemption *with a written reason* in
`src/feature-matrix/harness/javaSurfaceExemptions.ts` (never applicable —
overload collapsed, Java-only idiom). There is no third path. "We'll get to
it" is spelled `test.todo`, so it stays visible.

**What day one actually found (2026-08-28):** 31 uncovered members. Triage:
one **implemented-but-never-tested feature** now has a real test
(`UserTaskOutput#withOnCancellationException` — the audit's predicted first
catch); two genuinely missing features became visible todos
(`InterruptHandler#withEventType`, placeholder resolution via
`Workflow#getPlaceholderValues`); five test titles under-cited siblings their
bodies already exercise (`isNotEqualTo`, `doesNotContain`, `isNotIn`, `or`,
the comparison quartet) and were extended; the remaining 15 became reasoned
exemptions (functional-interface methods, `SpawnedThreads#buildNode` as a
compiler hook, trivial accessors, and the nine thread-level expression
factories that duplicate cited `LHExpression` operations). The banner now
reads **192 done / 2 todo — 99%**, and for the first time that denominator is
computed from Java's real surface rather than remembered.

Acceptance included breaking the check on purpose three ways — a fake surface
entry, a misspelled citation, a dead exemption — and watching each turn red
before trusting it. Still pending from this design: wiring the surface
regeneration into the CI drift gate, which lands with the CI job
([roadmap.md](./roadmap.md), item 2).

## Design 2: probe pairs (micro-goldens)

The per-feature evidence, replacing "somewhere in a big fixture" with an
isolated, Java-anchored proof per feature.

A **probe** is a minimal, deliberately boring workflow whose only job is to
make the compiler exhibit one feature. A **probe pair** is that workflow twice
— a control (**base**) and a treatment (**base + feature**) — identical except
for the one feature under test, and *with the same workflow name*, so whatever
differs in the compiled output is, by construction, that feature's entire
effect.

Both variants are compiled **by sdk-java** in the generator and emitted as two
small fixtures (e.g. `golden/probes/spawnthread-inputvars.base.json` and
`.feature.json`). The matrix test then makes three assertions, none of which
contains a hand-authored expected value:

```ts
expectMatchesGolden(compileProbe(base),    'probes/<name>.base')    // JS ≡ Java, without
expectMatchesGolden(compileProbe(feature), 'probes/<name>.feature') // JS ≡ Java, with
expect(goldensDiffer('probes/<name>.base',
                     'probes/<name>.feature')).toBe(true)           // the toggle did something
```

Why this closes the audit findings:

- The expected value is **transcribed, never authored** — the "pinned fact" is
  implicit in the pair of Java-generated fixtures, every field of it. Nobody
  chose which fields matter, and under-selection is impossible.
- **A vacuous probe cannot pass.** If the "feature" variant doesn't actually
  engage the feature (the exact failure mode of the `"variables": {}` hole),
  the two fixtures compile identically and the third assertion goes red.
- The base fixture also pins how each SDK encodes the feature's *absence*
  (empty maps, unset fields) — historically a real divergence surface between
  protobuf-ts and Java's builders.

Authoring rules (the entire human contribution): same workflow name in both
variants; the base is the feature's nearest do-nothing neighbor; one feature
per probe. An over-broad toggle — one that removes more than the feature — is
the one way to fool the pair, and it is visible in review because the fixtures
it produces are materialized files in the pull request.

## Design 3: the reference workflows, demoted honestly

Once probes carry the per-feature evidence, the 14 reference workflows stop
vouching for individual features — feature tests cite their probe, and the
many-to-one "proven by golden X" pattern is retired. The references keep two
narrower jobs they are genuinely good at:

1. **An interaction sample.** The compiler is stateful — pending variable
   mutations attach to whatever edge comes next; `doElseIf` performs edge
   surgery with a special case for empty bodies; node names encode global
   position; `spawnThread` plants a hidden mutation that lands on the following
   edge. These are properties of *sequences*, which one-feature probes
   structurally cannot reach. The reference workflows are today's only offline
   coverage of that surface, they are already built, and keeping them costs
   nothing.
2. **Realistic payloads for the integration tier.** The server-acceptance suite
   registers every reference workflow against a real server, proving realistic
   *compositions* are accepted — not just minimal probes.

## Design 4: randomized dual-compile (endgame)

The systematic answer to the interaction surface, replacing the hand-picked 14
with generated coverage:

- A generator (seeded, reproducible) emits random *valid* workflow descriptions
  in a small neutral format; a Java driver and a JS driver each interpret the
  description and compile it; the two protos are compared directly. The oracle
  is Java *live* — no stored fixture, because Java answers on demand.
- Any mismatch on a valid input is a real divergence. The failing case is
  automatically shrunk to its minimal form — which is, structurally, a probe —
  and checked in with Java-backed fixtures as a permanent regression pair.
  Exploration feeds the fixture corpus; nothing found is ever lost.
- Because scenarios are structured data, every run prints what it exercised
  ("68/70 constructs, 44% of ordered pairs, 0 mismatches") — so "did we sample
  enough?" is a computed number, not a feeling.
- It needs both toolchains at run time, so it lives in a scheduled CI job, not
  the 14-second inner loop. When it lands, the reference workflows' offline
  interaction role is retired; they remain as integration-tier payloads.

## What remains judgment, on purpose

Stated so nobody discovers it later:

- **The exemption list** (Design 1) — each entry a permanent, reasoned claim
  that a Java member needs no JS counterpart. Reviewed like code; dead entries
  fail the suite.
- **Probe authoring** (Design 2) — a usage statement, not an expectation;
  vacuity is machine-caught, over-broadness is visible in fixture diffs.
- **The generator's grammar** (Design 4) — what it knows how to generate;
  incompleteness shows up in the printed coverage numbers rather than
  silently.

Everything else — the list of features, the expected bytes, the deltas, even
the sensitivity of the fixtures — is read out of files that sdk-java generated
or comparisons between them. Where TypeScript is trusted in this design:
nowhere. Every assertion terminates in something Java produced.
