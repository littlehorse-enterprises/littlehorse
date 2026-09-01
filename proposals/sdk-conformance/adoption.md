# Proposal: SDK Conformance, adoption and governance

- Status: **Implemented** (2026-09); kept as the governance record. One
  delta from the plan below: the suite's CI job currently runs report-only
  for everyone, including the two v1 SDKs; flipping them to gated is the
  remaining step.
- Parent: [README.md](./README.md) · Design: [design.md](./design.md)

A conformance suite is as much an institution as a program. This file
covers how SDKs join, who decides what, and the objections we expect, each
answered from the research rather than optimism. What future versions can
entail lives in [design.md](./design.md#future-versions).

## Contents

- [The adoption ladder](#the-adoption-ladder)
- [Governance](#governance)
- [Objections, pre-answered](#objections-pre-answered)

## The adoption ladder

Three rungs per SDK, each honest and visible in the matrix. No team is
ever forced up a rung; the matrix simply shows where everyone stands.

| Rung | What it means | Cost to the SDK team |
| --- | --- | --- |
| **not joined** | the SDK's column reads *not joined*: visible, zero shame | none |
| **report-only** | a testee exists and results publish on every run; the ledger holds the todos; nothing blocks that team's merges | a small testee + a ledger file |
| **gated** | the SDK's own CI fails on any drift from its ledger, which is full membership | flipping one CI flag |

The plan is for sdk-js and sdk-java to land gated: the suite ships green
by construction (canon is minted from the reference, and every gap is
ledgered), so gating costs nothing and locks the state in. The report-only
rung is designed to be genuinely useful on its own: an SDK team gets a
public, always-current inventory of exactly what their SDK does and does
not cover, before accepting any CI obligation.

The invitation model. The research's clearest social finding: suites
imposed on teams die (CloudEvents: two of ten SDKs ever joined), while
suites that make joining trivial and immediately useful spread. So the
sequence is show-then-ask: land v1 with two green columns, then invite the
next SDK with its testee shim mostly written. Arriving with a working
matrix beats arriving with a mandate.

## Governance

Small rules, written before they're needed:

- Canon changes (any fixture or manifest change) require a PR with the
  regenerated fixture diff as the review surface, and a second reviewer.
  The server team is the natural one, since the canon encodes their
  protocol's meaning. This also answers the solo-maintainer concern: no
  one person can change what "correct" means alone, however automated the
  pipeline.
- New cases land green everywhere by construction: the same PR adds the
  case, its fixtures, and a `todo` ledger entry for every SDK that doesn't
  pass it yet (the protobuf same-PR convention). Debt is visible from
  birth.
- Ledger changes for an SDK are reviewed by that SDK's owner;
  `not_applicable` entries additionally need the second reviewer, because
  they are permanent claims about the protocol, not about one SDK's
  schedule.
- Disagreements: in v1 there is nothing to adjudicate, since canon is
  frozen and disagreement means red. The genuine governance question
  arrives with runtime cases and a third SDK; the rule then is that
  server-visible behavior is adjudicated by the server's own records, and
  anything else is a reviewed canon change. If consensus can't be reached,
  the case moves to `recommended` rather than blocking anyone: visible
  disagreement beats stalled canon.
- Custody: the suite starts under its proposing maintainer's stewardship.
  The intended end state is server-team custody, since the natural owner
  of a cross-SDK canon is the team that owns the protocol. That transfer
  is a standing offer, not a precondition.

## Objections, pre-answered

| Objection | The evidence-backed answer |
| --- | --- |
| "Ledgers will rot like protobuf's 11 KB failure lists." | Their rot came from one-way tolerance. Ours ratchet both ways with exact equality: an excuse that stops being true fails CI until deleted. |
| "The suite will die like CloudEvents'." | CloudEvents was optional and external. This rides each participant's normal CI from day one, and joining costs a small shim, under the adoption-cost ceiling every surviving suite respects. |
| "The canon itself will have bugs." | It will; AWS once shipped 36 bad canonical cases. The valves are designed in: per-SDK ledger entries with reasons (local escape), reviewed regeneration from the reference (fix path), and later server-side validation of every fixture (the canon's own test). |
| "One polyglot suite will drag every toolchain through CI" (Temporal's pain, Avro's death). | No shared build environment: each testee builds in its own SDK's toolchain and CI job; the runner only shells binaries and compares JSON. |
| "Who reviews the reviewer?" | Canon and `not_applicable` changes require a second reviewer outside the authoring pipeline (governance above). The system's credibility rests on nobody grading their own homework, including its maintainer. |
