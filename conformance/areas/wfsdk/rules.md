# The Builder Rules

The cross-case recipe for implementing a workflow builder in any language.
Each rule states a fact every conforming builder must implement, and names
the frozen fixture that enforces it — **the fixtures are the authority**: if
this prose and a fixture ever disagree, the fixture wins and this file has a
bug. Rules exist so an implementer never has to reverse-engineer another
SDK's source; scenarios stay short by pointing here.

Enforcement notes name fixtures as `<case>/<variant>` under
[cases/](./cases/).

## R1 — node names

A node's name is `<index>-<human>-<TYPE>`:

- `<index>`: the number of nodes already in the thread when this node is
  added (entrypoint is always index 0).
- `<human>`: a lowercase hint from the call that created the node —
  `entrypoint` and `exit` for the automatic nodes, `sleep` for sleep nodes.
- `<TYPE>`: the node-kind suffix from this table. Only suffixes with a
  shipped case are enforced today; the rest are the forward recipe and
  become enforced as their cases land.

| node kind | suffix | enforced by |
| --- | --- | --- |
| entrypoint | `ENTRYPOINT` | `workflow-minimal/feature` |
| exit | `EXIT` | `workflow-minimal/feature` |
| sleep | `SLEEP` | `sleep-seconds/feature` |
| task | `TASK` | *(future case)* |
| external event | `EXTERNAL_EVENT` | *(future case)* |
| start thread | `START_THREAD` | *(future case)* |
| start multiple threads | `START_MULTIPLE_THREADS` | *(future case)* |
| wait for threads | `WAIT_FOR_THREADS` | *(future case)* |
| no-op | `NOP` | *(future case)* |
| user task | `USER_TASK` | *(future case)* |
| throw event | `THROW_EVENT` | *(future case)* |
| wait for condition | `WAIT_FOR_CONDITION` | *(future case)* |
| run child workflow | `RUN_CHILD_WF` | *(future case)* |
| wait for child workflow | `WAIT_FOR_CHILD_WF` | *(future case)* |

## R2 — the automatic entrypoint

Every thread begins with node `0-entrypoint-ENTRYPOINT` containing an empty
`entrypoint` message, created before any user code runs.
*Enforced by `workflow-minimal/feature` (and every other fixture).*

## R3 — edge wiring

When a node is added, the previous last node gains one outgoing edge
`{ sinkNodeName: <new node>, variableMutations: [] }` — unless the previous
node is an exit node, which never gains edges. Nodes are appended in call
order; the "last node" pointer advances to each new node.
*Enforced by `sleep-seconds/feature` (entrypoint→sleep→exit chain).*

## R4 — the automatic exit

When the thread function returns, if the last node is not an exit node, the
builder appends one: an `exit` message with no result, named per R1.
*Enforced by `workflow-minimal/feature` (`1-exit-EXIT`) and
`sleep-seconds/feature` (`2-exit-EXIT` — note the index shows it is added
last).*

## R5 — the entrypoint thread

The entrypoint thread function compiles into `threadSpecs["entrypoint"]`,
and `entrypointThreadName` is `"entrypoint"`.
*Enforced by every fixture.*

## R6 — workflow-level defaults

`allowedUpdates` defaults to `ALL_UPDATES`. No retention policy, no parent
workflow reference, and no other top-level field is emitted unless a call
set it.
*Enforced by `workflow-minimal/feature`.*

## R7 — thread-level structure

A compiled ThreadSpec always carries `nodes`, `variableDefs` (empty array
when no variables were declared), and `interruptDefs` (empty array when no
interrupts were registered).
*Enforced by `workflow-minimal/feature`.*

## R8 — literal values

A literal passed to a builder method becomes a `VariableAssignment` with
`literalValue` holding the proto arm for the value's type. Numbers with no
fractional part are `INT` (an int64, serialized as a **decimal string** in
JSON); fractional numbers are `DOUBLE`; strings are `STR`; booleans are
`BOOL`.
*Enforced by `sleep-seconds/feature` (`"int": "30"`).*

## R9 — sleepSeconds

`sleepSeconds(n)` appends one sleep node whose `sleepLength` is
`rawSeconds` = the R8 assignment of `n`.
*Enforced by `sleep-seconds/feature`.*

## R10 — case workflow names

The workflow in case `<id>` is named `probe-<id>` in every variant, so a
pair's diff never contains the name.
*Enforced by every fixture.*

## R11 — testee serialization

Testees print proto3 JSON: default values emitted, int64 as strings, enums
as names. Key order and whitespace are free — comparison is semantic
(`runner/compare.mjs`, which also treats `null` as field-absent per proto3
JSON).
*Enforced by the runner on every case.*

## Minting

This area's canon — `surface.json` and every fixture under `cases/` — is
minted by the reference testee's wfsdk module,
[`WfsdkAreaMint.java`](../../../sdk-java/conformance/src/main/java/io/littlehorse/sdk/conformance/WfsdkAreaMint.java),
which mints from the case definitions in `WfsdkArea.java` — the same
ones that answer the exam's `compile` verb — so the answers and the canon
can never drift. Regenerate —
only ever inside a PR, where the diff is the review surface — with:

```
./gradlew :sdk-java-conformance:installDist
sdk-java/conformance/build/install/sdk-java-conformance/bin/sdk-java-conformance mint conformance
```
