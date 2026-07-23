# Dashboard QA Process

How we keep the Dashboard from regressing. This exists because non-trivial changes (the
`ts-proto → @protobuf-ts` migration touched ~100 files) shipped bugs that unit tests
couldn't catch — empty labels on every diagram edge, status filters that silently didn't
filter, fields that rendered nothing. Those were only found by manually running the app.
This process makes that verification automatic.

## Testing layers

We use the standard testing pyramid — many cheap tests at the bottom, few expensive ones
at the top.

| Layer | Tool | What it covers | Speed |
|---|---|---|---|
| **Unit** | Jest + React Testing Library | Pure functions (`extractEdges`, `extractNodes`, `utils/*`) and single components with mock props | ms |
| **E2E** | Playwright + live LittleHorse server | Real pages rendering real server data: diagrams, statuses, timestamps, variables, filters, forms | seconds–min |
| **Invariants** | Playwright (cross-page) | "No `[object Object]` / `NaN` / console errors on any page" — catches whole classes of bug with no specific assertion | seconds |

**Rule of thumb for where a test goes:**

- Logic with no rendering (a formatter, a mapper, a filter) → **unit test**. Cheapest, do
  this first. The else-edge bug was a one-line pure-function mistake; a unit test would
  have caught it instantly.
- "Does this actually render right against real data?" → **E2E test**.
- "Could this silently render a proto object as text anywhere?" → covered for free by the
  **invariants** spec; just make sure your page is in its page list.

## Running tests

```bash
cd dashboard

pnpm test            # unit tests (Jest) — fast, no server needed
pnpm test:coverage   # unit tests with coverage

pnpm e2e:up          # start a LittleHorse server + seed fixtures (Docker + Java)
pnpm e2e             # run the E2E suite (Playwright)
pnpm e2e:ui          # ... or the interactive runner
pnpm e2e:down        # tear the stack down
```

See `e2e/README.md` for fixtures and details.

## CI gates (what blocks a PR)

Every PR that touches `dashboard/**` or `sdk-js/**` must pass:

1. **Build** — `pnpm build` (also catches type errors via `tsc`).
2. **Unit tests** — `pnpm test`.
3. **E2E** — the `Dashboard E2E` workflow (`.github/workflows/dashboard-e2e.yml`): boots a
   standalone server, seeds fixtures, runs the Playwright suite. On failure it uploads the
   Playwright HTML report (with traces/video) as a build artifact.

Add `[skip test]` to a commit message only for docs-only changes.

## PR author checklist

- [ ] New logic that can be unit-tested has a unit test.
- [ ] New/changed pages or render paths have (or are covered by an existing) E2E assertion.
- [ ] New render surfaces are added to `e2e/tests/invariants.spec.ts`'s page list.
- [ ] If new data was needed, it was added to the fixtures app with a **fixed** wfRun id
      and a **unique** task name (task defs are immutable).
- [ ] Assertions target user-visible text or `data-testid`, not CSS classes.
- [ ] `pnpm test` and `pnpm e2e` pass locally.

## Reviewer checklist

- [ ] Behavior changes are covered by a test, not just described in the PR.
- [ ] E2E assertions check **rendered output**, not just that a page returned 200.
- [ ] No new `data-testid` churn where a text/role selector would do.
- [ ] Fixtures changes are deterministic (no timing/ordering assumptions).

## When manual QA is still required

Automated tests cover the deterministic paths. Do a manual pass for:

- **Auth / OAuth (Keycloak) flows** — not yet in the E2E suite.
- **Visual / layout / responsive** polish — until visual snapshots land (see below).
- **Large / real-world data** — performance and virtualization with big WfSpecs and
  high-volume WfRun lists.
- **A brand-new page or interaction** the first time, before writing its E2E test.

## Flake policy

A test suite people don't trust is worse than none.

- E2E runs with `retries: 1` in CI; a test that only passes on retry is **flaky** and must
  be fixed or quarantined, not ignored.
- Quarantine a flaky test with `test.fixme()` (or a `@quarantine` tag) **and** open an
  issue. Quarantined tests are triaged weekly; they don't get to rot.
- Fix flakiness at the source: wait for a specific element/text, never a fixed `sleep`;
  use fixed fixture ids; don't assert on ordering.

## Roadmap (not yet built)

- **Visual regression** — Playwright `toHaveScreenshot()` on the diagram + a couple of
  wfRun pages, run in CI's Docker image for deterministic rendering. This is the automatic
  "something looks different that nobody asserted on" net (e.g. pills on every edge).
- **More fixtures** — error/exception runs, external events, structs, maps, child
  workflows; a variable-of-every-type spec.
- **Prod-mode target** — run E2E against the `output: standalone` server (what ships)
  instead of `next dev`.
- **Browser/viewport matrix** — Firefox/WebKit/mobile on a nightly schedule, plus a nightly
  canary run against `lh-standalone:master` to catch server drift.
- **MSW** for page/server-action tests that need hard-to-produce responses (e.g.
  `RESOURCE_EXHAUSTED`) without a live server.
