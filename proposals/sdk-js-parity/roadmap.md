# Proposal: sdk-js Parity — Roadmap

- Status: **Living document** — reorder as things land; delete entries when
  done (the suite, not this file, is the record of what works)
- Parent: [README.md](./README.md)

Ordered by leverage. Each item names the file where its full design lives.

## Now: the mechanical batch (hours each)

1. **Stop shipping the test harness in the npm package**
   ([audit.md](./audit.md) #2). Exclude `src/feature-matrix/**` and
   `src/integration/**` from the build; verify with `npm pack --dry-run`.
   **Release-blocking** — the current package would ship modules that crash
   for consumers.
2. **Add the sdk-js CI job** ([audit.md](./audit.md) #3). The offline suite is
   ~14s with no Docker; the prerequisites that used to make CI red-on-arrival
   (formatter rewriting fixtures, lint failures) are already fixed. Include a
   golden drift gate: regenerate all Java-derived fixtures and fail on any
   difference.
3. **Fix the lying docs** ([audit.md](./audit.md) #4). Two READMEs still say
   the JS SDK cannot create workflows; the package README's examples crash.
   Rewrite around the wfsdk and worker with snippets that run.
4. **The `LHW_` config-key rename** ([client.md](./client.md)) — no
   backward-compatibility aliases (the wrong spellings never shipped) — and
   decide the in-flight default (Java's 2 vs the current 8; Java should win as
   the stated gold standard). Then land the config-key containment check so
   the bug class is unrepeatable.
5. **Make the constructs suite honest** ([audit.md](./audit.md) #6): correct
   the header's "derived mechanically" claim and add the 8 uncovered
   constructs as visible todos. The banner will show non-zero todo — that is
   the system working.

## Next: the two structural upgrades

6. ~~**The freshness check**~~ — **done (2026-08-28)**; see
   [wfsdk.md](./wfsdk.md), Design 1 for what it found on day one. One tail
   remains, folded into item 2: the CI drift gate must also regenerate
   `golden/fixtures/java-surface.json` and fail on any difference.
7. **Probe pairs** ([wfsdk.md](./wfsdk.md), Design 2) — Java-backed
   base/feature fixture pairs become the per-feature evidence, retiring the
   57 bare fixture-citation tests and making vacuous coverage mechanically
   impossible. Extend reference workflows only where probe triage discovers a
   feature no fixture exercises.

## Then: the worker's oracle

8. **Differential worker conformance** ([worker.md](./worker.md)) — the
   scenario grid, the Java worker driver, and diff-everything-the-server-
   recorded. The highest-leverage item in the family: it removes authored
   expectations from the one layer where runtime divergence is genuinely
   possible. Cheap start: the Java-side module is already wired.
9. **Soak and benchmarks against a real server, with a Java baseline**
   ([worker.md](./worker.md)) — point the existing 300-task soak and the
   absolute-floor benchmarks at a real server and make the assertions relative
   ("within N× of Java"). Also the venue for the universal invariants.

## Later: the endgame

10. **Randomized dual-compile** ([wfsdk.md](./wfsdk.md), Design 4) —
    generated workflows compiled by both SDKs and compared live, with failing
    cases shrunk into permanent probes and a printed coverage report. Retires
    the hand-picked reference workflows' offline role. Most expensive item;
    do it after 6–8.

## Small fixes (fold into any nearby change)

- The value decoder's missing native `ARRAY`/`MAP` cases
  ([client.md](./client.md)) — collections currently decode to `undefined`.
- `searchUserTaskRun` has no run-ID filter; document/support the client-side
  match pattern the examples use.
- Delete `src/worker/struct.ts` — dead code from before the zod design;
  nothing imports it.
- Migrate the two pre-parity examples (`quickstart`, `simple-worker`) off the
  old codegen's `$case` unions; they no longer typecheck.
- An injectable logger for the worker — it currently writes to the console
  unconditionally, which makes integration output read like failures.
- Run the three heavy integration suites repeatedly in CI before trusting
  them under load ([integration.md](./integration.md), reliability notes).

## Standing asks of the server team

1. **A machine-readable public API surface per SDK** — turns every SDK's
   freshness check into a download plus a diff.
2. **Server-published conformance fixtures** — golden protos and expected
   execution records blessed by the server team, so each SDK tests against
   the server's truth instead of hand-rolling a generator.
3. **A supported multi-node test image** — we hand-rolled Kafka plus N server
   containers because the standalone image cannot exhibit rebalancing; that
   harness is infrastructure the server team is better placed to own. The
   long-term version of this ask is a worker protocol **conformance kit**
   ([worker.md](./worker.md), limits).
