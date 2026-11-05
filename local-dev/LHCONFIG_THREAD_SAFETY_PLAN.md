# LHConfig thread safety

## Contract

A fully constructed LHConfig can be shared by worker and application threads.
Concurrent getters and stub creation must preserve one default value per key,
reuse one channel per host/port, and safely initialize OAuth credentials and
refresh their cached token. Builders remain confined to one thread; registered
interceptors and type adapters must support concurrent use themselves.

## Implementation

1. Retain atomic default initialization and its worker-ID regression.
2. Use a concurrent channel cache with atomic creation per host/port. Failed
   creation must remain retryable. Preserve channel options and interceptors.
3. Guard OAuth initialization as one operation. Synchronize token lookup and
   refresh inside the shared credentials provider, then invoke metadata callbacks
   outside that lock. Failed refresh must remain retryable.
4. Copy the builder's interceptor list into each built configuration and document
   the concurrency contract. Audit supporting objects for additional shared state.

## Validation

- Coordinate simultaneous calls with latches rather than sleeps.
- Check channel identity across blocking, asynchronous, and future stubs for the
  same endpoint, isolation between endpoints, and recovery after creation errors.
- Exercise concurrent OAuth initialization, initial token fetch, expiry, and
  recovery from failed fetches; assert successful calls receive the expected token.
- Check that builder reuse cannot change an existing configuration's interceptors.
- Run SDK formatting and unit tests, a shared-config E2E regression, and the full
  server E2E suite. Keep new work local for review.

## Boundaries

This change does not add channel lifecycle APIs, change builder concurrency, or
change retry or timeout policy. The original CI failure's precise trigger remains
unconfirmed; the original worker-ID regression remains part of validation.

## Result

All implementation steps are complete locally. OAuth initialization and credential
access use the config's monitor; token refresh uses the provider's separate monitor.
Metadata callbacks run after releasing the token lock. Channel initialization uses
atomic per-key cache creation, and built configs snapshot the interceptor list.

Validation completed on November 5, 2026:

- Before the fixes, four of the five initial targeted tests failed: channel reuse,
  interceptor snapshot isolation, and both token-sharing regressions.
- All 325 Java SDK tests pass, including the original worker-ID regression and
  seven new tests covering channel caching, OAuth metadata, token refresh and
  failures, callback lock boundaries, and builder snapshot isolation.
- All 461 server E2E tests pass, including eight simultaneous workflow clients
  sharing one config through blocking and future stubs.
- SDK/server formatting checks, SDK Javadoc generation, and `git diff --check` pass.
- The first E2E attempt stopped during bootstrap because Docker was off. Docker
  Desktop was started, and the subsequent complete run passed.

CI validation and review proceed in PR #2618.
