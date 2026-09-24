# The builder rules

The cross-case recipe for implementing a workflow builder in any language.
Each rule states a fact every conforming builder must implement, and names
the frozen fixture that enforces it. **The fixtures are the authority**.
If this prose and a fixture ever disagree, the fixture wins and this file
has a bug. Rules exist so an implementer never has to reverse-engineer
another SDK's source, and scenarios stay short by pointing here.

Enforcement notes name fixtures as `<case>/<variant>` under
[cases/](./cases/).

## Node names (R1)

A node's name is `<index>-<human>-<TYPE>`, built from three parts.

- `<index>` is the number of nodes already in the thread when this node
  is added (the entrypoint is always index 0).
- `<human>` is a lowercase hint from the call that created the node.
  `entrypoint` and `exit` name the automatic nodes, `sleep` names sleep
  nodes, `nop` names no-op nodes, and nodes that reference a target use
  the target's name (the TaskDef name for task nodes, the event name for
  external-event nodes, `throw-<event name>` for throw-event nodes, and
  so on). The fixtures pin the hint for every kind, with one blind spot.
  The child-workflow fixtures use the spec name `child-wf`, so they
  cannot distinguish Java's derived `run-<spec name>` hint from a
  hard-coded `run-child-wf`. The derivation is the rule.
- `<TYPE>` is the node-kind suffix from this table.

| node kind | suffix | enforced by (one of several) |
| --- | --- | --- |
| entrypoint | `ENTRYPOINT` | `workflow-minimal/feature` |
| exit | `EXIT` | `workflow-minimal/feature` |
| sleep | `SLEEP` | `sleep-seconds/feature` |
| task | `TASK` | `execute-args/feature` |
| external event | `EXTERNAL_EVENT` | `wait-for-event/feature` |
| start thread | `START_THREAD` | `spawn-thread-input-vars/feature` |
| start multiple threads | `START_MULTIPLE_THREADS` | `spawn-thread-for-each/feature` |
| wait for threads | `WAIT_FOR_THREADS` | `wait-for-threads/feature` |
| no-op | `NOP` | `do-else/feature` |
| user task | `USER_TASK` | `assign-user-task/feature` |
| throw event | `THROW_EVENT` | `throw-event/feature` |
| wait for condition | `WAIT_FOR_CONDITION` | `expr-and/feature` |
| run child workflow | `RUN_CHILD_WF` | `run-wf-inputs/feature` |
| wait for child workflow | `WAIT_FOR_CHILD_WF` | `wait-for-child-wf/feature` |

## The automatic entrypoint (R2)

Every thread begins with node `0-entrypoint-ENTRYPOINT` containing an
empty `entrypoint` message, created before any user code runs.
*Enforced by `workflow-minimal/feature` (and every other fixture).*

## Edge wiring (R3)

When a node is added, the previous last node gains one outgoing edge
`{ sinkNodeName: <new node>, variableMutations: [] }`, unless the previous
node is an exit node, which never gains edges. Nodes are appended in call
order, and the "last node" pointer advances to each new node.
*Enforced by `sleep-seconds/feature` (the entrypoint, sleep, exit chain).*

## The automatic exit (R4)

When the thread function returns, if the last node is not an exit node,
the builder appends one, an `exit` message with no result, named per R1.
*Enforced by `workflow-minimal/feature` (`1-exit-EXIT`) and
`sleep-seconds/feature` (`2-exit-EXIT`, whose index shows it is added
last).*

## The entrypoint thread (R5)

The entrypoint thread function compiles into `threadSpecs["entrypoint"]`,
and `entrypointThreadName` is `"entrypoint"`.
*Enforced by every fixture.*

## Workflow-level defaults (R6)

`allowedUpdates` defaults to `ALL_UPDATES`. No retention policy, no parent
workflow reference, and no other top-level field is emitted unless a call
set it.
*Enforced by `workflow-minimal/feature`.*

## Thread-level structure (R7)

A compiled ThreadSpec always carries `nodes`, `variableDefs` (an empty
array when no variables were declared), and `interruptDefs` (an empty
array when no interrupts were registered).
*Enforced by `workflow-minimal/feature`.*

## Literal values (R8)

A literal passed to a builder method becomes a `VariableAssignment` with
`literalValue` holding the proto arm for the value's type. Strings are
`STR`, booleans are `BOOL`, and integers are `INT` (an int64, serialized
as a decimal string in JSON). In languages without an int/double
distinction, numbers with no fractional part are `INT` and fractional
numbers are `DOUBLE`. The reference maps by static type instead, and no
fixture pins a `DOUBLE` literal yet, so that arm awaits its own case.
*Enforced by `sleep-seconds/feature` (`"int": "30"`).*

## sleepSeconds (R9)

`sleepSeconds(n)` appends one sleep node whose `sleepLength` is
`rawSeconds`, the R8 assignment of `n`.
*Enforced by `sleep-seconds/feature`.*

## Case workflow names (R10)

The workflow in case `<id>` is named `probe-<id>` in every variant, so a
pair's diff never contains the name.
*Enforced by every fixture.*

## Testee serialization (R11)

Testees print proto3 JSON with default values emitted, int64 as strings,
and enums as names. Key order and whitespace are free, because comparison
is semantic (`runner/compare.mjs`, which also treats `null` as
field-absent per proto3 JSON).
*Enforced by the runner on every case.*

## Minting

This area's canon (`surface.json` and every fixture under `cases/`) is
minted by the reference testee's wfsdk module,
[`WfsdkAreaMint.java`](../../../sdk-java/conformance/src/main/java/io/littlehorse/sdk/conformance/WfsdkAreaMint.java),
which mints from the case definitions in `WfsdkArea.java`, the same ones
that answer the exam's `compile` verb, so the answers and the canon can
never drift. Regenerate only ever inside a PR, where the diff is the
review surface:

```
node sdk-conformance/runner/build.mjs
node sdk-conformance/runner/mint.mjs
```
