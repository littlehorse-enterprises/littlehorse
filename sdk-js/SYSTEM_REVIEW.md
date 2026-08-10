# sdk-js parity system — review findings

**Date:** 2026-07-29 · **Branch:** `feat/sdk-js/parity-plan` @ `283cf45c2`

A critical review of the _system_ built to prove sdk-js ↔ sdk-java parity — not of
the SDK code itself. Companion to [PARITY_PLAN.md](PARITY_PLAN.md) (the design) and
[FUTURE_WORK.md](FUTURE_WORK.md) (deferred work).

## How this was produced, and how much to trust it

Five independent reviewers, each given a different lens over the repo (test bodies,
infrastructure, docs, missing capabilities, right-sizing), returned 20 raw findings.
Those were deduped and ranked to 8, then **each surviving finding was handed to a
separate skeptic instructed to refute it**, with access to the code.

That last step matters: of the 8, only **2 survived unchanged**. Six were corrected —
in several cases the verifier found the diagnosis right but the proposed cure wrong or
actively harmful. Five findings were dropped at triage. Those corrections and rejections
are recorded below, because "we already considered that and here's why not" is the most
perishable knowledge in a review.

Where a claim below was checked directly (commands run, files read, code executed) it is
marked **[verified]**. Everything else is a reviewer's assertion that survived a refutation
attempt but was not independently reproduced.

---

## Findings

### 1. The "191/191 · 100%" banner divides the enumeration by itself — and the enumeration was already incomplete

**Severity: highest. This undermines the system's central claim.**

The reporter computes `totalDone / (totalDone + totalTodo + totalFailed)`, all three terms
sourced from Jest statuses in `src/feature-matrix/`. Nothing happening in sdk-java can move
that number. With zero `test.todo` entries remaining anywhere in the repo (`grep -rn '\.todo(' src/`
→ 0), the mechanism PARITY*PLAN.md describes as the matrix — *"passed = done, todo = missing"\_ —
now has no instances, and nothing pins the denominator.

I previously described this risk as theoretical ("if Java adds a method tomorrow, nothing
notices"). **It is not theoretical.** A set difference over sdk-java's 21 public wfsdk
interfaces found public capabilities that predate the matrix (authored 2026-07-23) and have
no entry:

| Missing from the matrix                                      | Public in Java since | Status in sdk-js                                                      |
| ------------------------------------------------------------ | -------------------- | --------------------------------------------------------------------- |
| `UserTaskOutput#withOnCancellationException`                 | 2024-04-12           | **implemented at `src/wfsdk/nodeOutputs.ts:64`, zero tests anywhere** |
| `InterruptHandler#withEventType`                             | 2026-02-08           | absent                                                                |
| `placeholderValues` overloads on `Workflow` / `LHTaskWorker` | 2026-07-09           | absent from sdk-js entirely                                           |
| `Workflow#saveProtoToFile`, `SpawnedThreads#buildNode`       | —                    | absent                                                                |

**Three is a floor, not a total.** The matrix was incomplete on the day it was written, so
"100%" has been reporting against a denominator that was never the real surface.

**Corrections from the verifier:**

- The percentage is _not_ frozen at 100 — `totalFailed` is in the denominator, so regressions
  do move it. What cannot move it is anything happening in sdk-java.
- **The freshness check is not "hours."** The matrix→Java direction is cheap; the
  **Java→matrix** direction — the one that actually catches misses — needs **~80+ exemptions**
  for grouped overloads and Java-only helpers (40 uncited names in wfsdk, 32 of 34 in
  `LHLibUtil`). Budget a day-plus, and treat _writing the exemption list as the parity audit
  itself_.
- Do **not** relabel the banner to "enumeration synced vs sdk-java @ `<sha>`" — that asserts a
  sync that hasn't been verified.

**Action:** build the freshness check as a test inside the matrix so a new or missed public
sdk-java member fails the suite. Until it exists, the banner should not print a bare "100%".

---

### 2. The published npm package ships the test harness **[verified]**

`tsconfig.json` has `include: ["src/**/*"]` and `exclude: ["**/*.test.ts"]`. The integration
harness and fake server are **not** `.test.ts` files, so they compile into `dist/`, and
`package.json` has `files: ["/dist"]`.

Verified by building and inspecting output:

```
dist/feature-matrix/fakeServer.js   dist/integration/cluster.js
dist/feature-matrix/golden.js       dist/integration/container.js
dist/feature-matrix/referenceWorkflows.js   dist/integration/globalSetup.js
```

`dist/integration/cluster.js` and `container.js` `require('testcontainers')` — a
**devDependency**, absent from the published tarball. So the package ships modules that throw
on require.

Both `src/feature-matrix/` and `src/integration/` are **new on this branch** (verified against
`origin/master`), so this branch introduced the defect. `release.yml` has a live
`publish-sdk-js` job, so it would ship.

**Action:** add `src/feature-matrix/**` and `src/integration/**` to `tsconfig.json`'s `exclude`
(or use a separate build tsconfig). One line. Verify with `npm pack --dry-run`.

**Hypotheses I checked and discarded** — recorded so nobody re-litigates them:

- _Root `"."` export lacks a `types` condition_ → types still resolve fine under
  `moduleResolution: node16`. **Not a bug.**
- _The `"import"` condition points at CommonJS output_ → a real ESM consumer imports
  `littlehorse-client/wfsdk` successfully at runtime. **Not a bug.**

---

### 3. Nothing runs any of this on a pull request

`.github/workflows/tests.yml` has jobs for sdk-java, sdk-go, sdk-python, sdk-dotnet, dashboard,
canary, server and test-utils. **There is no sdk-js job.** The only reference to sdk-js in
`.github/workflows/` is `publish-sdk-js` in `release.yml` — the package is published, but its
307-test parity suite has never run in automation. **[verified]**

**Blocking prerequisite the original finding missed:**

`.prettierignore` covers only `dist`, `node_modules`, `src/proto` — so `golden/*.json` and
`golden/fixtures/serde.json` are subject to `prettier --write`, and prettier's formatting
differs from the Java generator's output (`golden/basic.json`: 1326 bytes as generated, 1436
after prettier; array indentation and `"entrypoint": {}` collapse differ). The pre-commit hook
`format-sdk-js` is `always_run: true` and runs `npm run lint:fix`. Jest never notices because
`loadGolden` parses JSON.

Therefore a golden-drift gate (`git diff --exit-code sdk-js/golden/` after regeneration) would
be **permanently red** until `golden/` is added to `.prettierignore` _and_ the committed goldens
are restored to raw generator output. **Wire `.prettierignore` first, then the gate.**

Also: `pnpm run lint` **fails today** on 20 files (the 15 golden/fixture JSONs, `PARITY_PLAN.md`,
`FUTURE_WORK.md`, `pnpm-lock.yaml`, `src/integration/cluster.ts`, `src/worker/LHTaskWorker.ts`).
Adding lint to CI as-is is red on arrival.

**Action, in order:** (a) fix `.prettierignore` + restore goldens; (b) fix the 20 lint failures;
(c) add a `tests-sdk-js` job running `pnpm test` (252 tests, ~12s, Docker-free) and optionally
`pnpm run test:integration:core` (~25s); (d) add the golden-drift gate.

---

### 4. The documentation states the opposite of what is now true

**Verdict: confirmed, and the verifier found it worse than reported.**

- `README.md:154` (repo root) says: _"note that our JS SDK does not yet support creation of
  `WfSpec`s, so we use `lhctl` here"_. `examples/js/quickstart/README.md:37` repeats it. Both
  are now false — the wfsdk is the headline feature of this branch.
- `sdk-js/README.md` (69 lines) still describes the package as _"the generated library to
  perform gRPC calls"_ and points users to the server docs for usage. There is no JS-specific
  usage documentation.
- **Both code blocks in `sdk-js/README.md` throw before reaching gRPC** (verified by execution):
  line 38 uses `new LHConfig.fromPropertiesFile(...)` — that static does not exist; line 55 uses
  `new LHConfig.from({...})` — throws `LHConfig.from is not a constructor`, because static class
  methods are not constructible.
- A third latent bug, unreachable in both snippets today: `getClient({accessToken})` interpolates
  to `Bearer [object Object]` (`src/LHConfig.ts:283`; the signature at `:209` takes a bare string).
- `examples/java` contains 38 example directories; `examples/js` contains 3. **No workflow under
  `.github/workflows/` references `examples`**, so nothing builds or type-checks them — a
  converted JS example would rot silently.

**Action:** fix the two false claims, rewrite `sdk-js/README.md` around the wfsdk + worker, fix
both snippets, and decide whether examples/js gets a CI type-check.

---

### 5. A real parity break sitting inside the area the banner reports as 100%

Java reads worker configuration from the **`LHW_` namespace** (`LHConfig.java:60-62`:
`LHW_TASK_WORKER_ID`, `LHW_TASK_WORKER_VERSION`, `LHW_NUM_WORKER_THREADS`) plus
`LHC_INFLIGHT_TASKS` (`:65`); `ConfigBase` routes both prefixes (`:661` → `{"LHC_", "LHW_"}`).
sdk-js recognizes only `LHC_` and uses `LHC_`-prefixed spellings for the worker keys, so a user
porting a working Java worker config to JS gets silently ignored settings.

**Corrections from the verifier:**

- **Do not keep the `LHC_` worker spellings as back-compat aliases.** They were introduced by the
  parity commit `2fc23a42a` itself, which is not on master and appears in no release tag —
  master's `CONFIG_NAMES` has 8 keys, all matching Java. They have never shipped, so this is a
  **straight rename**. Keeping aliases would permanently add three key names Java rejects.
- A Java-derived fixture must assert **containment plus an explicit allowlist**, not set equality:
  sdk-js has `LHC_GRPC_MAX_RECEIVE_MESSAGE_LENGTH` (`src/LHConfig.ts:20`) with no counterpart in
  sdk-java, so strict equality fails on day one and invites weakening the check.
- The in-flight default of 8 is at `src/LHConfig.ts:131` (not `:324`), and it is not arbitrary —
  it matches `sdk-dotnet/LittleHorse.Sdk/LHInputVariables.cs:28`. Java uses 2. Java should win
  given it is the stated gold standard, but frame this as _choosing Java over .NET_, not as
  correcting a mistake.

---

### 6. `workflow-constructs.test.ts` claims a mechanical completeness property it does not have

The header at `src/integration/workflow-constructs.test.ts:31-34` says the enumeration is
_"derived mechanically from `WorkflowThread`, so any construct there without an entry is a
visible gap."_ Nothing derives it — it was hand-written. `PARITY_PLAN.md:213` and `:221-223`
amplify the same claim.

**The verifier confirmed the honesty defect and refuted the inflation around it** (a reviewer
proposed deleting the suite as "the one that never caught a bug"; that rests on a one-commit
sample and on the false premise that byte-identical protos imply identical execution — true only
for constructs that _have_ a golden, and the goldens cover 14 reference workflows).

**The corrected action is a docs edit plus real todos**, not a restructuring:

1. Replace the false claim with an explicit cross-reference block, as `worker-runtime.test.ts:247-254`
   already does — `doIfElse`/`doWhile`/`handleException` are covered in `execution.test.ts:87/112/143`;
   `withRetries` and node timeouts in `worker-runtime.test.ts:73/119`; `sleepUntil`,
   `handleAnyFailure`, `releaseToGroupOnDeadline`, `scheduleReminderTask` and
   `cancelUserTaskRunAfter` are spec-level only, via `referenceWorkflows.ts` → `wfspec-acceptance`.
2. Add real `test.todo` entries, with reasons, for constructs with **no coverage above the feature
   matrix**: `waitForAnyOf`, `waitForFirstOf`, `reassignUserTask`,
   `scheduleReminderTaskOnAssignment`, `cancelUserTaskRunAfterAssignment`,
   `setUserTaskOnCancellationException`, thread-level `withRetentionPolicy`,
   `handleAnyFailureOnChild`.

Those todos will make the integration banner show non-zero todo — which is the point. The
mechanism exists (`jest.reporter.js:133-154`) and demonstrably worked once (`caeb11f87` shipped 6
integration todos; `283cf45c2` closed them).

---

### 7. 57 of 191 matrix entries are a bare `provenByGolden()` resolving to only 12 distinct goldens

Their bodies are exactly one `provenByGolden(name)` call, and those 57 calls resolve to 12
distinct golden names (14× `variables`, 9× `expressions`, 6× `conditionals`, 5× `failure-handling`,
…). Each such entry re-runs the same comparison and pins nothing specific to itself.

**Live hole, not hypothetical:** `wfsdk.test.ts:743` — _"spawn a child thread with input variables
— Java: WorkflowThread#spawnThread"_ — passes today while `golden/child-threads.json` records
`"variables": {}` on all three spawn nodes, and no call site in the repo passes non-empty
`inputVars`. `src/wfsdk/WorkflowThread.ts:564-567` is never executed by anything.

**Corrections from the verifier:**

- Assert the named proto fact on the **JS compiled output**, not by reading the golden — asserting
  against the golden tests the Java fixture and says nothing about sdk-js.
- **The pattern already exists and does not need inventing.** Eight entries already do
  `provenByGolden` + a focused assertion on the compiled proto (e.g. `wfsdk.test.ts:313-330`
  `declareArray`, `:334-341` `declareMap`). The work is "extend the existing pattern from 8 entries
  to 65", not "rewrite 57 tests."

---

### 8. Stale status claims, and `FUTURE_WORK.md` is not in git

**Verdict: confirmed.**

| Location                   | Says                              | Actually                                    |
| -------------------------- | --------------------------------- | ------------------------------------------- |
| `PARITY_PLAN.md:17-18`     | "45 of 51 integration checks"     | **55 verified, 0 todo**                     |
| `PARITY_PLAN.md:299-300`   | tier-2 integration "still todo"   | done — 174 lines after `:125` marks it Done |
| `PARITY_PLAN.md:342`       | "Twelve reference workflows"      | **14** (`:144` already says 14)             |
| `PARITY_PLAN.md:223`       | `test.todo` marks deliberate gaps | zero todos remain                           |
| commit `283cf45c2` message | "51 verified, 0 todo"             | **55** — 6 todos became 10 tests            |

The 59-line **"Work division: two independent tracks"** section (`:364-422`) schedules ~38 worker
todos, ~30 config todos, 4 usertask todos, an integration rig and soak/chaos tests — _all marked
Done elsewhere in the same document_. Two lines are still live: the CI bullet at `:409` (→ finding 3)
and the insight at `:383-386` that golden JSONs are directly registerable `PutWfSpecRequest`
payloads (→ move to the Golden harness section).

**`FUTURE_WORK.md` is untracked and not ignored** (`git status --short` → `?? sdk-js/FUTURE_WORK.md`;
`sdk-js/.gitignore` contains only lockfiles). It is the only written record of what this system
_cannot_ prove — enumeration freshness, golden coverage, worker semantic divergence — i.e. exactly
the honesty layer the design depends on. It will not appear in the PR and will not survive a clean
clone.

Also: `FUTURE_WORK.md:89` lists **five** exemptions while saying "the 4 entries with no Java
citation". The intruder is `golden files exist`, which lives in `golden.test.ts` — a _support_
suite, outside the 191 by `jest.reporter.js:27-30`. "187 of 191" and "4" are both correct; drop
that one line item. This matters because that list is the spec for the freshness check in
finding 1, so it would be built against a wrong allowlist.

**Nuance:** `:223` is the weakest of the four — it states a convention that now describes an empty
set, rather than a false status claim.

---

## Cut (brevity, no information loss)

- The 59-line work-division section (`PARITY_PLAN.md:364-422`), after relocating its two live lines.
- The unique-suffix expression, triplicated verbatim at `harness.ts:95`, `globalSetup.ts:81`,
  `container.ts:67`.
- Five `.replace(/[^a-z0-9-]/g, '')` calls that are **provable no-ops** — `uniqueName` composes a
  prefix with two `toString(36)` values, so there is nothing to strip:
  `workflow-constructs.test.ts:467`, `cluster.test.ts:29`, `oauth.test.ts:155`, `tls.test.ts:95`,
  `worker-runtime.test.ts:200`.
- Optional tidy: `register()`/`track()`/`beforeAll` are byte-identical across three integration
  files (confirmed by `diff`); pull into `harness.ts` as `register(client, workflow, taskInputVars)`.
  **Ordinary hygiene, not a correctness safeguard** — see the rejection note below.

---

## Do NOT change — proposed and refuted

Recorded so these are not re-proposed. Each was argued for by a reviewer and rejected on evidence.

| Proposal                                                                        | Why not                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| ------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Delete or shrink `fakeServer.ts`** as redundant with `worker-runtime.test.ts` | All 8 `FakeServerOptions` and all 14 recorder arrays have live callers. Its scenarios (`dropPollStreams` mid-poll, scripted host rebalance, `failReportTaskTimes`) are **disjoint** from what a real server can be made to do on demand. It earns its keep.                                                                                                                                                                                                                                                |
| **Define `awaitReady` in terms of `awaitReadyWith`**                            | They differ in _where_ the client is built, and that is load-bearing: `awaitReadyWith` builds it **inside** the retry loop (`cluster.ts:178`) because `oauth.test.ts:89-92` mints a fresh, expiring token per attempt. `awaitReady` builds once (`:195`). Naive delegation constructs a new `GrpcTransport` — an unclosed grpc-js channel — every second of a 600s wait, to save ~10 lines.                                                                                                                |
| **Collapse the nine deadline loops into one `pollUntil(fn, ms, label)`**        | Their timeout semantics _are_ the diagnostics: `harness.ts:60-72` returns `false`; `:127-140` throws with the RPC error; `:162-173` throws with the last observed WfRun status; `globalSetup.ts:58-75` throws with the container's Docker logs; `cluster.test.ts:76-81` returns silently so the following `expect` produces the message. Flattening loses those or needs four option flags. A generic predicate-poll already exists (`fakeServer.ts:418`) and is _not_ a drop-in for the RPC-shaped loops. |
| **Merge the two `describe`s in `cluster.test.ts`**                              | Saves ~9–12s of a **measured 115.8s** tier. Costs: the 40s node-loss test becomes unrunnable via `jest -t` (it would inherit the 1-node `beforeAll` and fail "expected 2, received 1"), and any failure in _scaling up_ before `addNode(2)` produces a second, misleading red in _losing a node_.                                                                                                                                                                                                          |
| **Share one Kafka across `cluster`/`tls`/`oauth`**                              | Rejected on measurement: `tls`+`oauth` together are only 32.5s, mostly their own test bodies. Buys ~30s while coupling three suites that currently fail independently and diagnose cleanly.                                                                                                                                                                                                                                                                                                                |
| **Graduate `test.todo` → `features.yaml`** (a FUTURE_WORK bullet)               | Restates `PARITY_PLAN.md:108-113`, which already considered and rejected moving the enumeration outside the tests — that is the whole design point. A YAML manifest reintroduces exactly the drift the design avoids. The real need is a **computed denominator** (finding 1), not a different place to hand-write the same list.                                                                                                                                                                          |
| **Shrink `jest.reporter.js`** (186 lines of presentation)                       | Cosmetic; the line count costs nothing. Its substantive half — that the percentage is pinned and `classify()` buckets purely by file path, so deleting a matrix file silently shrinks the denominator — is preserved as finding 1. Rewrite what the banner _prints_; don't golf its length.                                                                                                                                                                                                                |
| **Delete `workflow-constructs.test.ts`** as the suite that never caught a bug   | One-commit sample, and the argument rests on "byte-identical proto ⇒ identical execution", true only for constructs that have a golden (14 reference workflows). Downgraded to the honesty fix in finding 6.                                                                                                                                                                                                                                                                                               |

---

## Corrections to earlier claims made in this project

- **"The Java API freshness check is a few hours of work."** Wrong — **a day-plus**. The direction
  that catches misses needs ~80+ exemptions, and writing that list _is_ the parity audit.
- **"The full integration run is ~5 minutes"** (`PARITY_PLAN.md:200-204`, and repeated verbally).
  Measured: **115.8s**, of which `cluster` is 64.1s.
- **"Integration coverage is 51 verified, 0 todo"** (commit `283cf45c2`). Actually **55**.
- **"Kafka Streams restore takes ~2 minutes"** — true on a cold first boot under contention, not
  representative of a normal run.

---

## Suggested order

**Same-day, mechanical:**

1. `tsconfig.json` exclude — stop shipping the harness (finding 2)
2. `.prettierignore` + restore goldens, then fix the 20 lint failures (finding 3, prerequisite)
3. Add the `tests-sdk-js` CI job (finding 3)
4. Fix the two false "JS SDK cannot create WfSpecs" claims and both broken README snippets (finding 4)
5. `LHW_` rename, no aliases (finding 5)
6. `git add sdk-js/FUTURE_WORK.md`; fix the stale numbers; cut the work-division section (finding 8)

**Then, in order of value:**

7. Rewrite the `workflow-constructs` header honestly and add the 8 real todos (finding 6)
8. Extend the `provenByGolden` + focused-assertion pattern from 8 entries to 65 (finding 7)
9. Build the Java API freshness check; expect it to surface more misses like
   `withOnCancellationException` (finding 1) — budget a day, and stop printing a bare "100%"
   until it exists
