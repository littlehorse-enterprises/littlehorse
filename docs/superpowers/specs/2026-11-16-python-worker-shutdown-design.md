# Python worker graceful shutdown (Fixes #2650)

## Intent and scope

Keep the existing `await littlehorse.start(worker)` integration and SDK-managed
SIGTERM/SIGINT handlers. Returning successfully must mean every accepted task has
finished execution and its result has been acknowledged. Only Python worker
lifecycle, tests, SDK documentation, and a Python example change.

## Approaches

Use the existing synchronous `worker.stop()` as the idempotent request and the
existing `start()` coroutine as the completion boundary. Adding `wait_closed()`
would duplicate that boundary; making `stop()` async would break callers.
The user confirmed that existing SDK-managed signals should remain the default.

## Design

- Add keyword-only `shutdown_timeout: float = 30.0` to `LHTaskWorker`.
  The first stop request starts a fixed deadline; subsequent requests do not
  extend it.
- Keep automatic signal installation by default. Add keyword-only
  `handle_signals: bool = True` to `littlehorse.start()` for application ownership.
- Own each connection's lifetime until its polling and accepted work finish,
  including connections removed or replaced during heartbeat reconciliation.
- Register each accepted task before waiting for execution capacity. One owned
  coroutine spans execution and reporting; release execution capacity before
  reporting. Remove finished tasks and retrieve failures immediately.
- Admission occurs when the polling consumer receives a result, before any
  following suspension. Stop cancels only polling/heartbeat, not accepted work.
  Responses not delivered to that consumer before stop are outside the boundary.
- Preserve report retries and task failure serialization. Reporting exhaustion
  raises `TaskResultReportingException`, retained until shutdown if it occurred
  earlier. Drain all other accepted work before raising.
- Deadline expiry cancels and joins owned work and raises `TimeoutError`.
  External cancellation cancels and joins owned work and propagates
  `CancelledError`. Cleanup requires cancellation-cooperative task code; neither
  cancellation nor a missed deadline is graceful success.
- Do not close shared config channels during reconciliation or draining.

## Guarantees and limits

Keep application resources available through SDK completion. SIGKILL, process
failure, permanent network failure, and cancellation-resistant/blocking user code
cannot be solved by graceful shutdown. There is no exactly-once side-effect
guarantee. Kubernetes budgeting is drain deadline + application cleanup + margin.

## Verification

Use the real worker/connection lifecycle with fake RPC boundaries and controlled
events for admission, execution, acknowledgement, retry, reconciliation, and
cancellation. Prove regressions fail on the original implementation. Run the
repository's unittest, Black, Ruff, and mypy commands; include a small POSIX signal
subprocess smoke test with no external server.
