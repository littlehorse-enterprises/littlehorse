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

## Manual release QA

Automated tests cover the deterministic paths. Before a release (or release candidate),
one person runs the checklist below by hand — it covers what automation can't judge yet:
visual quality, auth, performance under real data, and the shipped production image.
During normal development, a manual pass is also expected the first time a brand-new page
or interaction lands, before its E2E tests exist.

**Setup.** Run against the bits that will actually ship:

```bash
cd dashboard
pnpm e2e:up                    # LittleHorse server + seeded fixtures on :2023
docker build -t dash-rc .      # the production image (output: standalone)
docker run --rm -p 3000:3000 -e LHC_API_HOST=host.docker.internal -e LHC_API_PORT=2023 dash-rc
```

Using the production image matters: the E2E suite currently runs against `next dev`, so
the standalone build path is only exercised here.

**Checklist.** Mark each item; a failure blocks the release until triaged.

*Core flows (against the seeded fixtures):*
- [ ] Metadata search lists the fixture WfSpecs; search-as-you-type narrows; pagination works.
- [ ] `dashe2e-conditionals` WfSpec page: diagram lays out sanely, condition/else labels and
      the mutation icon look right (not just present — readable, positioned, styled).
- [ ] `dashe2e-basic-completed` WfRun: click through **every** sidebar tab (Overview / Node /
      NodeRun); switch between NodeRuns and confirm no stale data or crash.
- [ ] Execute WfRun modal: run `dashe2e-conditionals` filling **only required** fields, then
      again including **optional** fields — confirm optional values actually reach the run
      (regression area: optional variables were silently dropped in v1.2.0).
- [ ] User task flow on `dashe2e-usertask-running`: assign/claim/complete via the UI.
- [ ] Leave a WfRun page open >30s: polling refreshes data **without** clearing the selected
      node or scroll position.
- [ ] Copy buttons appear and work (requires localhost/HTTPS secure context).

*Judgment areas (no automation):*
- [ ] **Auth**: with Keycloak enabled (`LHD_OAUTH_ENABLED=true` + local Keycloak), login,
      tenant selection, and logout all work; deep links redirect through auth correctly.
- [ ] **Visual pass**: key pages at desktop and ~1280px width, light theme — no overflow,
      truncation, or misaligned layout. Zoom the diagram in/out.
- [ ] **Large data**: open a WfSpec with 50+ nodes and a WfRun with large variable values
      (oversized JSON) — diagram stays responsive, large values expand into the modal
      instead of breaking the sidebar.
- [ ] **Second browser**: repeat 2–3 core flows in one non-Chromium browser (Safari or
      Firefox) until the E2E browser matrix exists.

*Sign-off:* record date, commit SHA, server image tag, and who ran it in the release notes
or tracking issue.

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
