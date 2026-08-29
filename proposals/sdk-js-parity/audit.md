# Proposal: sdk-js Parity — the Audit

- Status: **Record** (the review happened 2026-07-29; statuses below verified
  2026-08-26)
- Parent: [README.md](./README.md)

A verification system that never gets verified is just confidence with extra
steps. So the parity system was turned on itself: an adversarial review of the
*system* — not the SDK code — whose findings, refutations, and self-corrections
are recorded here. This file exists because "we already considered that, and
here is why not" is the most perishable knowledge a project has.

## Contents

- [How the review was run](#how-the-review-was-run)
- [The findings, with current status](#the-findings-with-current-status)
- [Proposals that were made and refuted — do not re-propose](#proposals-that-were-made-and-refuted--do-not-re-propose)
- [Corrections to our own claims](#corrections-to-our-own-claims)
- [The meta-lesson](#the-meta-lesson)

## How the review was run

Five independent reviewers each took a different lens over the repository
(test bodies, infrastructure, docs, missing capabilities, right-sizing) and
returned 20 raw findings. Those were deduplicated and ranked to 8 — and then
**each surviving finding was handed to a separate skeptic instructed to refute
it**, with full access to the code. Only 2 of the 8 survived unchanged; in
several cases the diagnosis was right but the proposed cure was wrong or
actively harmful. That last step is the part worth copying: a review without a
refutation pass produces findings that sound right, which is not the same as
findings that are right.

## The findings, with current status

| # | Finding (plain English)                                                                                                                                                                       | Status                                                                                     |
| - | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------ |
| 1 | **The "100%" banner divides the enumeration by itself — and the enumeration was already incomplete on day one.** Public Java methods with no test entry were found; one is implemented in JS with zero tests. Nothing notices when Java grows. | **Fixed (2026-08-28).** The freshness check is implemented ([wfsdk.md](./wfsdk.md), Design 1): the denominator is now computed from sdk-java's reflected surface, and day one confirmed the finding — 31 uncovered members, including the predicted implemented-but-untested `withOnCancellationException` (now tested) and two missing features (now visible todos: banner reads 192/2, 99%). Remaining tail: wiring regeneration into CI (finding 3). |
| 2 | **The published npm package ships the test harness.** The build config only excludes `*.test.ts`, so the fake server and Docker harness compile into `dist/` — including modules that require a dev-only dependency and would crash for any consumer. | **Open.** One-line build-config fix; verify with `npm pack --dry-run`. **Do not cut a release before this lands.** |
| 3 | **Nothing runs any of this in CI.** Every other SDK has a test job; sdk-js has only a publish job. A large regression suite that never runs, gating a package that ships.                          | **Partially cleared.** The blockers found alongside it (fixtures being reformatted by the code formatter; 20 lint failures) are fixed — the goldens are formatter-protected and lint is green. The CI job itself is still absent. |
| 4 | **The documentation stated the opposite of what is true** — two READMEs said the JS SDK cannot create workflows (the headline feature of this effort), and the package README's own examples crash. | **Partially cleared.** 19 runnable examples now exist under `examples/js/`. The false claims and broken snippets remain to fix. |
| 5 | **A real parity break inside the "100%".** sdk-js invented `LHC_`-prefixed environment keys for worker settings that Java reads from the `LHW_` namespace; a Java user's working config would be silently ignored. | **Open.** Straight rename (the wrong spellings never shipped); then mechanized forever by the config-key check ([client.md](./client.md)). |
| 6 | **A test file claimed a completeness property it does not have** — its header said its list was "derived mechanically," but nothing derives it, and 8 constructs have no coverage above the matrix. | **Open.** Fix the header's claim; add the 8 as visible todos — making the banner show non-zero todo, which is the system working, not a regression. |
| 7 | **57 of 191 matrix entries were a bare "the whole workflow matches its fixture" assertion**, resolving to only 12 fixtures — and at least one cited fixture provably does not contain the feature its test names. | **Fixed 2026-08-28.** The 57 bare entries were retired in favor of Java-backed probe pairs with a coverage ratchet ([wfsdk.md](./wfsdk.md), Design 2) — the vacancy class is now mechanically impossible, and the once-vacuous spawnThread inputVars fixture records the feature for real. |
| 8 | **The honesty documents were stale and partly untracked.**                                                                                                                                      | **Fixed, then superseded**: the documents were corrected and committed, and have since been reorganized into this proposals directory. |

## Proposals that were made and refuted — do not re-propose

Each of these was argued for by a reviewer and rejected on evidence. Recorded
prominently, because rediscovering-and-rejecting the same ideas is pure waste:

- **"Delete the fake server; the real-server suite covers it."** Examined
  directly: every one of its options and recorders has live callers, and its
  scenarios (breaking a poll stream mid-task, scripted host reassignment,
  failing the first N reports) are things a real server cannot be made to do
  on demand. It earns its keep.
- **"Merge the two server-readiness helpers; they're near-duplicates."** The
  difference is load-bearing: one builds its client *inside* the retry loop
  because the OAuth test mints a fresh, expiring token per attempt. Naive
  merging leaks an unclosed gRPC channel every second of a long wait.
- **"Collapse the several polling loops into one generic `pollUntil`."** Their
  differing timeout behaviors *are* the diagnostics — one returns false, one
  throws with the RPC error, one throws with the last observed status, one
  dumps container logs. Flattening loses exactly the information you need
  when they fire.
- **"Merge the two cluster test groups to save startup time."** Saves ~10s of
  a ~116s tier; costs the ability to run the node-loss test alone, and makes
  one root cause produce two misleading failures.
- **"Share one Kafka across the three heavy suites."** Buys ~30 seconds while
  coupling three suites that currently fail independently and diagnose
  cleanly.
- **"Graduate the test-title enumeration into a `features.yaml` manifest."** A
  manifest outside the tests reintroduces exactly the drift the design exists
  to prevent. The real need was a *computed denominator* (finding 1), not the
  same hand-written list in a different file.
- **"Shrink the custom test reporter; it's long."** The length is
  presentation and costs nothing; its real issue (what the banner's percentage
  *means*) is finding 1, not line count.
- **"Delete the workflow-constructs suite; it never caught a bug."** Rests on
  a one-commit sample and on the false premise that byte-identical protos
  imply identical execution *for constructs that have no fixture*. Downgraded
  to the honesty fix in finding 6.

## Corrections to our own claims

The review also caught the system's author being wrong, which is worth
recording for calibration:

- "The freshness check is a few hours of work" → **a day-plus**; the direction
  that catches misses needs a large reasoned exemption list, and writing that
  list *is* the audit.
- "The full integration run takes ~5 minutes" → measured **115.8s**.
- A commit message claimed "51 verified" integration checks → it was **55**.
- "Kafka Streams restore takes ~2 minutes" → true only on a cold first boot
  under machine contention; not representative.

## The meta-lesson

The sharpest single result: the one place a file *claimed* a mechanical
guarantee that no code enforced (finding 6) is exactly where a false belief
survived longest. **A claimed oracle that is not wired to fail is worse than
no oracle**, because it stops people from checking. Every design in this
proposal family since — the freshness check, probe pairs, the scenario grid —
is an application of that lesson: never state a guarantee; wire it.
