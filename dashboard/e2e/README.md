# Dashboard E2E tests

End-to-end tests that drive a **real browser** against a **real Dashboard** talking to a
**real LittleHorse server** seeded with deterministic fixtures. This is the layer that
catches regressions the unit tests can't — anything about how live server data actually
renders (statuses, timestamps, variable values, diagram labels).

## Layout

```
dashboard/e2e/
  playwright.config.ts   # config (baseURL, retries, webServer, reporters)
  global-setup.ts        # fails fast with a clear message if fixtures aren't seeded
  tests/
    diagram.spec.ts      # WfSpec diagram: condition labels, else edge, mutation icon, no empty pills
    wfrun.spec.ts        # WfRun detail: status enum, timestamps, typed variable values
    invariants.spec.ts   # every key page: no "[object Object]" / "NaN" / console errors
  scripts/
    up.sh / down.sh      # start/stop the local stack (LH server + seeded fixtures)
```

Fixtures are defined in `examples/java/dashboard-e2e-fixtures` (Java, because only the
Java SDK can author WfSpecs). They register a fixed set of WfSpecs and seed WfRuns with
**fixed ids** so tests can navigate straight to them:

| wfRun id | WfSpec | State | Exercises |
|---|---|---|---|
| `dashe2e-basic-completed` | `dashe2e-basic` | COMPLETED | task node, status, timestamps, str var |
| `dashe2e-cond-hi` / `dashe2e-cond-lo` | `dashe2e-conditionals` | COMPLETED | condition + else edges, mutation icon, int/double/bool/str vars |
| `dashe2e-usertask-running` | `dashe2e-usertask` | RUNNING | USER_TASK node, RUNNING status |

## Run it locally

```bash
# 1. bring up a LittleHorse server + seed fixtures (leave running)
cd dashboard
pnpm e2e:up

# 2. run the tests (Playwright starts the dashboard itself if one isn't already up)
pnpm e2e            # or: pnpm e2e:ui   for the interactive runner

# 3. tear the stack down when done
pnpm e2e:down
```

Requirements: Docker, Java (for the fixtures), Node/pnpm, and Playwright browsers
(`pnpm exec playwright install chromium`).

Env overrides: `E2E_BASE_URL` (default `http://localhost:3000`), `LHC_API_HOST` /
`LHC_API_PORT` (default `localhost:2023`), `LH_TAG` (standalone image tag, default `master`).

## Adding a test

1. If you need new data, add a WfSpec + a fixed-id run to the fixtures app and give it a
   unique task name (task defs are immutable — reusing a name across specs collides).
2. Navigate straight to the fixed id; never rely on search ordering or "the first row".
3. Prefer asserting on **user-visible text** over CSS classes. If you must target a
   structural element, add a `data-testid` to the component rather than matching classes.
