# Python Worker Shutdown Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make successful Python worker shutdown include all accepted executions and acknowledged results.

**Architecture:** Preserve synchronous stop requests and make existing start coroutines own and join connection, execution, and reporting lifetimes. Bound draining from the first stop request, retain failures, and cancel only intake during normal shutdown.

**Tech Stack:** Python 3.10–3.13, asyncio, grpc.aio, unittest, Poetry, Black, Ruff, mypy.

**Spec:** `docs/superpowers/specs/2026-11-16-python-worker-shutdown-design.md`

## Global Constraints

- Only Python worker lifecycle, tests, SDK documentation, and a Python example change.
- Keep SDK-managed signals and existing `await littlehorse.start(worker)` calls.
- No dependencies, server/protobuf changes, global asyncio task discovery, or arbitrary drain sleeps.
- First stop request fixes the deadline; incomplete draining must raise.
- Execution capacity must not be held during reporting.

## Review Focus

- Stop before startup or during registration must not reopen admission.
- A response already received but waiting for capacity must survive intake cancellation.
- Retired/replaced connections must retain reporting ownership without accumulating forever.
- Cancellation during deserialization, execution, reporting, and cleanup must not become success.
- One worker's startup/report failure must not abandon another worker's accepted work.

---

### Task 1: Own accepted work through result acknowledgement

**Files:** `sdk-python/littlehorse/worker.py`, `sdk-python/littlehorse/exceptions.py`, new `sdk-python/tests/test_worker_shutdown.py`.

**Interfaces:** Preserve `stop() -> None`, `start() -> None`; add keyword-only `shutdown_timeout: float = 30.0` to worker construction and `handle_signals: bool = True` to module start. Reporting exhaustion raises `TaskResultReportingException`.

- [x] Write event-driven fake-RPC lifecycle tests covering the 13 requested regressions, execution capacity, serialization, cancellation, startup races, and sibling-worker failures.
- [x] Run against original implementation and save failures.
- [x] Implement explicit task ownership, connection retirement, prompt intake shutdown, bounded drain, and explicit failures.
- [x] Run `poetry run python -m unittest tests.test_worker tests.test_worker_shutdown -v`; expect all passing.

### Task 2: Document and verify the public lifecycle

**Files:** `sdk-python/README.md`, new `examples/python/basic/example_graceful_shutdown.py`, `examples/python/basic/README.md`, new minimal signal-smoke fixture under `sdk-python/tests/`.

**Interfaces:** Consume Task 1's unchanged default call pattern, stop/completion semantics, timeout, and signal opt-out.

- [x] Add a small subprocess SIGTERM test using fake RPCs; verify both default SDK handlers and application-owned handlers.
- [x] Update documentation and basic example using real APIs, cleanup ordering, explicit error outcomes, honest limits, and Kubernetes budgeting.
- [x] Run focused tests, full `poetry run python -m unittest discover -v`, `poetry run black --check .`, `poetry run ruff check .`, and `poetry run mypy .`.
- [x] Review all cancellation/admission/reporting/reconciliation paths; obtain fresh review, fix significant findings with regressions.
- [x] Commit with Conventional Commit title and `Fixes #2650`, push the feature branch, and open a draft PR.

## Progress

- Investigation complete: detached tasks and heartbeat-only start completion reproduce the ownership gap structurally.
- Existing isolated worktree is on `fix/python-sdk-graceful-shutdown-2650`; no new worktree required.
- User directed continuing with existing SDK-managed signals; no further design questions.
- Poetry is supplied via `uv tool run --from poetry poetry`; project dependencies/lockfiles remain unchanged.
- Verification: 306 unittest tests pass on Python 3.10 and 3.13; 61 focused worker tests pass; pytest also passes all 306 tests on 3.10. Black, Ruff, and mypy pass.
- Independent review found no Critical/Important issues. Three extra boundary checks passed during review; persisting them as dedicated regressions is deferred (unchanged repeated-stop deadline, pre-stop reporting exhaustion followed by retirement, startup failure with accepted sibling work).
- Limits: no live LittleHorse server or Windows execution; process-level signals use fake RPC boundaries. Cancellation requires cooperative task code.
- Draft PR: https://github.com/littlehorse-enterprises/littlehorse/pull/2651
