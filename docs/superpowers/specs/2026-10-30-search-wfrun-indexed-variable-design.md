# SearchWfRun indexed-variable optimization

**Draft status: not ready to merge.** At the user's direction, this PR exposes the
minimal experimental fast path and documents unresolved coverage as a merge
blocker. The deletion regression remains enabled and failing to demonstrate it.

## Objective

Reduce server-side work for `SearchWfRun` requests combining workflow name,
status, and exact equality on a searchable string variable. The implementation
must be generic: no application-specific names, identifiers, or UUID assumptions.

The primary performance objective is a significant reduction from the reported
production round trip of more than 1,000 ms. Client p95 below 100 ms is the desired
target, to be validated under representative load rather than assumed from code
inspection. Production latency has not yet been measured in this investigation.

## Selected approach

Add an indexed-variable-first execution path inside `SearchWfRun`, reusing
existing variable indexes and index-key encoding. Keep the public request and
response shapes unchanged. No new persisted index or data migration is proposed.

1. Check whether a string-equality predicate is searchable and unmasked in every
   retained workflow definition. This is an experimental eligibility check,
   not yet proof that all retained runs have index coverage (see the blocker below).
2. If eligible, use that predicate's variable index to obtain candidates.
3. Retain only entrypoint-thread variables (`threadRunNumber == 0`).
4. Apply existing variable/status predicates to candidates before counting results
   toward the page limit.
5. Return full WfRun IDs and a cursor that resumes the selected scan correctly.

Use a deterministic choice when more than one eligible predicate exists; a
cost-based optimizer is outside this change. Apply the remaining variable
predicates as post-filters, including predicates on unindexed variables.

### Fallback behavior

- No variable predicates: preserve the existing WfRun scan.
- No usable indexed predicate: preserve the existing scan and post-filtering.
- Mixed indexed and unindexed predicates: use a fully covering indexed predicate
  for candidates, then evaluate the remaining predicates.
- Incomplete or uncertain index coverage across workflow versions: use the
  existing scan. Do not combine indexed and unindexed version scans in this PR.
- Version, time-window, or parent predicates: retain the existing scan in this first PR,
  avoiding additional internal filter types while preserving these query shapes.
  Workflow migration can leave existing variables associated with their original
  WfSpec version, so a version-specific variable index is not a safe substitute
  for the current WfRun version index.

Checking only the latest WfSpec's searchable flag is insufficient to establish
coverage. The draft checks all retained definitions; a complete coverage solution
must also account for deleted definitions and migrated runs before merge.

### Merge blocker: deleted definitions and migrated variables

Deleting a WfSpec removes its definition but leaves runs and variables behind.
For example, create a run under an unindexed definition, register an indexed
revision, delete the unindexed definition, and search for the original value.
The retained-definition check selects the variable index and misses the old run.
`SearchWfRunIndexTest.shouldFindUnindexedRunsAfterTheirWorkflowDefinitionIsDeleted`
is an enabled regression test demonstrating this failure.

Migration makes a check of only current WfRun version tags insufficient too:
existing variables can retain their original definition and lack an index even
after the run moves to an indexed revision and the original definition is deleted.

Upstream review needs to establish the smallest reliable coverage mechanism:
additional runtime checks, retained lifecycle/provenance metadata, or an index
coverage/backfill guarantee. This draft does not claim complete results for these
lifecycle cases and must not be merged or deployed until that is resolved.

## Strategy metrics

Add a search-specific duration histogram with bounded labels:

- `method="SearchWfRun"`
- `strategy="wfrun_index"` or `strategy="variable_index"`
- `outcome="success"` or `outcome="error"`

The selected execution plan carries its strategy explicitly. Start timing before
planning and record one observation per public RPC/page, covering planning, local
scans, and internal RPC waits. Internal partition scans must not record additional
observations in this same timer. If planning fails before a strategy is selected,
record `strategy="unselected"` rather than falsely attributing the error to a plan.

Use the Micrometer timer name `lh.search.duration`, exported as
`lh_search_duration_seconds` with histogram buckets enabled. Its count provides
request volume by strategy. Retain the existing generic gRPC metrics. Do not label
these metrics with workflow names, variable names, or values. Fallback-reason
labels are outside the initial scope.

## Verification

Success requires equivalent complete paginated results, unchanged unindexed query
semantics and tenant isolation, and a significant measured latency reduction.
There is no new snapshot-consistency guarantee for concurrent workflow changes.

- `./gradlew server:spotlessApply server:spotlessCheck server:test --continue --console=plain`:
  821 tests, 820 passed, one enabled failure demonstrating the coverage blocker.
  Formatting and SpotBugs passed.
- Tests cover status, reruns, thread filtering, full child IDs, misses, mixed
  predicates, masked values, retained unindexed versions, pagination, and metrics.
- `./gradlew server:e2e --tests e2e.WfRunSearchTest --console=plain`: compilation
  passed; execution failed during initialization because Docker was unavailable.
  The new E2E case covers running reruns and mixed predicates across limit-one pages.

Production validation still needs reused-channel client p50/p95/p99, representative
concurrency, and multiple partitions/servers, including hits, misses, and the time
to consume all pages. The sub-100 ms target must be demonstrated rather than assumed.

### Local draft measurements

An isolated in-memory comparison over 1,002 running runs, with two matching runs,
used fresh request contexts, five warmups, and 20 measured requests per strategy.
The baseline used the existing WfRun scan with an inclusive time cutoff before
all fixture runs; the optimized request omitted that cutoff. Both returned the
same two full IDs without a bookmark.

| Strategy | p50 | p95 | Core object reads per request |
| --- | --- | --- | --- |
| WfRun index | 27.218 ms | 32.927 ms | 1,002 |
| Variable index | 0.300 ms | 0.535 ms | 4 |

These are planning-and-scan measurements using real in-memory stores, not
production, RocksDB, network, or multi-server round trips. Tests assert result
equivalence and reduced reads, not wall-clock thresholds. Production latency and
the sub-100 ms end-to-end target remain unverified.

## Open question: rolling upgrades and bookmark compatibility

How should pagination continue seamlessly when a cursor was issued by the old
WfRun-index strategy, or when an optimized request's next page reaches an older
server during a rolling upgrade or rollback?

Existing bookmarks contain partition positions and raw index keys but no explicit
scan-strategy identifier. A WfRun-index position and a Variable-index position are
not interchangeable. Adding a field that older servers ignore does not by itself
establish compatibility.

At the user's direction, the cross-version bookmark protocol and rollout solution
are deferred from this PR. Seamless rolling-upgrade pagination remains a desired
requirement, not a resolved guarantee of this change. Pagination correctness
within each supported scan strategy remains in scope.
