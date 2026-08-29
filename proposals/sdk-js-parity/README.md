# Proposal: sdk-js Parity — Overview

- Status: **Draft**
- Scope: `sdk-js`, `sdk-js/golden/generator`, CI

> This directory is the complete record of the sdk-js parity effort. It
> replaces three former documents that lived in `sdk-js/`
> (`PARITY_PLAN.md`, `SYSTEM_REVIEW.md`, `FUTURE_WORK.md`); everything they
> recorded lives on, reorganized, across the files below.

## The files

| File                               | What it covers                                                                                    |
| ---------------------------------- | ------------------------------------------------------------------------------------------------- |
| **README.md** (this file)          | The mission, the reasoning, and the ideal end state — start here                                  |
| [wfsdk.md](./wfsdk.md)             | The workflow-definition layer: golden fixtures, the freshness check, probe pairs, dual-compile    |
| [client.md](./client.md)           | The client/config layer: parity by construction, the config-key check, shared value encoding      |
| [worker.md](./worker.md)           | The task-worker layer: the fake server, and the differential-conformance plan                     |
| [integration.md](./integration.md) | The real-server tier: the suites, the cluster harness, and everything the server taught us        |
| [audit.md](./audit.md)             | The adversarial review of the system itself: findings, statuses, and refuted proposals            |
| [roadmap.md](./roadmap.md)         | What to do next, in order, and the standing asks of the server team                               |

## The problem

`sdk-java` is the gold-standard SDK. Until this effort, `sdk-js` was a thin
generated gRPC client: it could call the server, but it could not define
workflows at all, and its task worker was minimal. The mission is to close
that gap.

The trap is that "bring it to parity" is not a checkable statement. There is
no point at which you can *prove* you are done, and no way to tell a real gap
from one nobody has thought about yet. The usual shortcuts all fail the same
way:

- **A checklist document** drifts from the code the day after it is written,
  and nothing notices.
- **Code coverage** measures which of *our* lines ran — it cannot see a Java
  feature we never ported, and it cannot see a feature that runs happily and
  does the wrong thing.
- **"It looks complete"** is an opinion, offered by the person who did the
  porting.

This matters even more because much of the porting is AI-assisted. AI-assisted
porting reliably produces *plausible-but-wrong* mappings for subtle semantics.
Plausible-but-wrong survives code review. It does not survive a byte-for-byte
comparison against the real thing.

## The doctrine

Every surface we test is held to the same discipline, built from two
questions:

1. **What do we write tests for?** The list must be *derived* from `sdk-java`
   (its public classes, its shared protos, its live behavior) — never recalled
   from someone's memory of it.
2. **How do we know the test expectations are correct?** The expected values
   must be *transcribed* from `sdk-java` (its serialized output, its recorded
   runs) or from the server's own records — never authored by the person
   writing the test.

Where human judgment genuinely cannot be eliminated, it is **quarantined into
written, reviewed, diffable files** (exemption lists, allowlists, scenario
grids). Judgment may annotate the derived list ("this Java-only helper needs
no port, because…"); it never gets to author an expected value.

The reason for this shape: a wrong judgment is invisible. It does not produce
a failing test — it produces a test that never existed, or an assertion that
agrees with its author's mistake. Facts extracted from `sdk-java` can be wrong
too, but they fail *loudly*, as red builds and mismatched bytes.

Two supporting principles, inherited from the original plan:

- **The proto contract is the real gold standard.** All SDKs compile to the
  same protobufs and talk to the same server. Java's source tells us *what*
  exists and its edge-case semantics; the protos define *correct*.
- **Port semantics, not code.** Java idioms (annotation scanning, method
  overloading, thread pools) are not transliterated. The JS SDK is idiomatic
  TypeScript that preserves the same *protocol behavior* — the worker in
  particular is a different design for Node's event loop, not a translation of
  Java's thread classes.

## The three layers, and why each needs different machinery

An SDK here is three different kinds of machine, and each one's shape decides
what kind of proof is available:

| Layer      | What it is                                                     | Its output                | Where its design lives             |
| ---------- | -------------------------------------------------------------- | ------------------------- | ---------------------------------- |
| **wfsdk**  | a compiler: user code runs once and emits a `PutWfSpecRequest` | a static proto — diffable | [wfsdk.md](./wfsdk.md)             |
| **client** | thin request/response translation over generated gRPC stubs    | per-call translations     | [client.md](./client.md)           |
| **worker** | a long-lived runtime that polls, executes, reports, reconnects | behavior over time        | [worker.md](./worker.md)           |

- The **wfsdk** is the best case: since the *server* executes the compiled
  proto, byte-identical output means identical runtime behavior by
  construction. Correctness is a diff against Java's actual bytes.
- The **client** is the free case: its RPC surface is generated from the same
  `service.proto` as Java's, so it cannot diverge except through stale
  generated code. Only the small hand-written layer (config, auth, retries)
  needs the full treatment.
- The **worker** is the hard case: it has no static artifact, so the oracle
  must be manufactured — by treating everything the worker causes the *server*
  to record as its output, and comparing a Java worker's records against ours
  for the same scenarios.

Beneath all three sits one **real-server tier** ([integration.md](./integration.md)):
models can only confirm their authors' assumptions, and every genuinely
surprising finding in this project came from a real server refusing to
cooperate.

## The ideal end state

When this proposal family is fully implemented, the following are all true:

1. **The test list is computed, not remembered.** A script derives the
   required coverage from `sdk-java`'s public classes; a new Java method turns
   the build red until we either port it (a visible `test.todo`) or exempt it
   in writing.
2. **Every expected value traces to a file Java generated or a record the
   server wrote.** No assertion in the parity suites rests on what a test
   author believed the right answer to be.
3. **Judgment survives in exactly three reviewable files** — the wfsdk
   exemption list, the client config allowlist, and the worker scenario grid —
   each one small enough to read in a pull request, each entry carrying its
   reason.
4. **The parity banner's "100%" divides by a computed denominator.** Today it
   divides the enumeration by itself; the enumeration was hand-written once
   and is known to have missed things.
5. **CI runs all of it on every pull request**, including a drift gate that
   regenerates every Java-derived file and fails on any difference — so none
   of the saved facts can silently go stale.

## Where things stand

The suite currently reports **192 done / 2 todo (99%)** on the enumerated
surface offline (257 tests + 2 todos total, ~12s, no Docker) plus 55
integration tests against real servers — and since 2026-08-28 that denominator
is **computed** from sdk-java's reflected public surface by the freshness
check, not remembered from a hand-written list (ideal-state point 1 is live
for the wfsdk). The two todos are real, visible debt the check surfaced. The
live status is always the suite itself — run it rather than trusting any
document, including this one:

```sh
cd sdk-js && npm test                      # offline: matrix + goldens + fake server
cd sdk-js && npm run test:integration      # real servers (~2 min; see integration.md)
```

The honest limits — an enumeration that was incomplete on day one, tests that
lean on fixtures which may not exercise their feature, and the absence of any
CI — are recorded with their current statuses in [audit.md](./audit.md), and
the ordered plan to close them is [roadmap.md](./roadmap.md).
