# TypeScript WfSpec builder (minimal API)

## Motivation

[`sdk-js`](../sdk-js) already ships a gRPC client, generated protos, and a task worker. It does **not** yet offer a workflow-spec DSL: today you either hand-assemble `PutWfSpecRequest` / `ThreadSpec` from protos, generate JSON for `lhctl deploy`, or define the `WfSpec` in another language. Teams that standardize on Node for workers naturally want to **author and register** workflows in TypeScript with the same mental model as Java / Python / Go / .NET.

This proposal sketches a **minimal first slice** (variables, task nodes, mutations) so the API can be reviewed before implementation; broader parity (conditionals, events, user tasks, etc.) comes later.

## Proposed Protocol Buffer changes

**None.** The builder compiles to the existing public API (`PutWfSpecRequest` and related messages in [`schemas/`](../schemas/)). No new fields or RPCs.

## Proposed SDK API changes

Add a **workflow builder** to `littlehorse-client` (`sdk-js`): types and implementation under `sdk-js/src/` (exact module path TBD) that mirror the graph-building model used by other SDKs and produce the same protobuf objects users could build manually today.

Initial surface area: `Workflow.create`, `WorkflowThread` (`declare*`, `execute`, `mutate`, `assign`, expression helpers, `complete`), compiling to `PutWfSpecRequest`. Optional follow-up: `registerWfSpec`-style helper orchestrating `putWfSpec` (and any side-effect RPCs) like other SDKs.

Concrete interfaces and an example appear in [Proposed TypeScript surface](#proposed-typescript-surface) below.

## Server architecture and operations

**No server changes.** This is client-only. Performance and cluster behavior are unchanged; the server continues to accept the same `putWfSpec` payloads.

## Backwards compatibility

**Additive.** Existing `littlehorse-client` imports and behavior stay as-is. New APIs are additional exports; no breaking changes to `LHConfig`, the gRPC client, or the worker unless explicitly called out in a future revision of this doc.

## GitHub issue

TBD — add a tracking issue when implementation work is scheduled.

---

## Scope (initial)

Per team discussion: **variables**, **task nodes**, and **variable mutations** first—then if/while, events, user tasks, etc.

## Cross-SDK parity

| Concept | Java | Python | Go | C# | Proposed TS (camelCase) |
|--------|------|--------|----|----|-------------------------|
| Define workflow | `Workflow.newWorkflow(name, ThreadFunc)` | `Workflow(name, entrypoint)` | `NewWorkflow(ThreadFunc, name)` ⚠️ arg order | `new Workflow(name, Action)` | `Workflow.create(name, threadFn)` |
| Compile | `compileWorkflow()` → `PutWfSpecRequest` | `compile()` | `Compile()` | `Compile()` | `compile()` → `PutWfSpecRequest` |
| Task node | `execute(task, args…)` | `execute(...)` | `Execute(...)` | `Execute(...)` | `execute(...)` |
| Mutate | `mutate(var, type, rhs)` | `mutate(...)` | `Mutate(...)` | `Mutate(...)` | `mutate(...)` |
| Assign sugar | `var.assign(rhs)` | `assign(rhs)` | _(use `Mutate` + ASSIGN)_ | _(use `Mutate` + ASSIGN)_ | `assign(rhs)` |
| Declare var | `declareStr(name)` etc. | `declare_str(name, default=…)` | `DeclareStr` / `AddVariableWithDefault` | `DeclareStr` | `declareStr(name, default?)` |
| Task options | `execute(...).withRetries(n)` etc. | kwargs on `execute` | overrides on node | chain on `TaskNodeOutput` | chain on `TaskNodeOutput` (Java-style) |

Go keeps `(threadFunc, name)`; TS should use the more common **name-first** order like Java/C#/Python.

## Implementation notes

- Reuse generated types under [`sdk-js/src/proto/`](../sdk-js/src/proto): `PutWfSpecRequest`, `ThreadSpec`, `VariableMutation`, `VariableMutationType`, `TaskNode`, etc.
- Mirror the **graph-building model** from other SDKs: one active `WorkflowThread`, implicit edges from the previous node, auto-append **exit** when the thread function returns (same as C# constructor pattern).
- **RHS** of `execute` / `mutate` / `assign`: literals, `WfRunVariable`, `NodeOutput`, and **`LHExpression`** from `add` / `multiply` / … (same layering as Java/Python/Go).
- **Registration:** `putWfSpec` already exists on the gRPC client from [`LHConfig#getClient()`](../sdk-js/src/LHConfig.ts); a future `registerWfSpec()` helper is mostly orchestration + external-event side effects (same as other SDKs).
- **Format strings:** Java/C# use `{0}`-style indices; Python’s `LHFormatString` uses `{}` — choose one for TS and document it so examples stay consistent.
- **Deferred parity:** JSON helpers (`extend`, `removeKey`, …), variable modifiers (`.searchable()`, `.masked()`, …), `doIf` / `spawnThread`, etc. follow after the minimal milestone.

The following is a **design sketch** only — not part of the `sdk-js` build until implemented under `sdk-js/src/`.

## Proposed TypeScript surface

```typescript
/**
 * Sketch for a minimal WfSpec builder (variables, tasks, mutations).
 * Imports are relative to repo root for illustration; implementation lives in sdk-js.
 *
 * Parity: sdk-java / sdk-python / sdk-go / sdk-dotnet workflow APIs.
 */

import type { PutWfSpecRequest } from '../sdk-js/src/proto/service'
import type { VariableType } from '../sdk-js/src/proto/common_enums'
import type { ExponentialBackoffRetryPolicy, VariableMutationType } from '../sdk-js/src/proto/common_wfspec'

// --- Values accepted wherever other SDKs take `Serializable` / `interface{}` / `object` ---

/** Literal, variable, task output, or inline expression (see thread.add / thread.multiply, etc.). */
export type WorkflowRhs = unknown

/**
 * Opaque handle for inline RHS expressions (math / JSON helpers).
 * Same role as Java `LHExpression`, Python `LHExpression`, Go `lhExpression`.
 */
export interface LHExpression {
  readonly _lhExpression: true
}

// --- Handles (opaque at compile time; implementation tracks node names, json paths, lh paths) ---

export interface WfRunVariable {
  readonly name: string
  /** Optional JSON path / struct field navigation — same semantics as other SDKs. */
  jsonPath(path: string): WfRunVariable
  /**
   * Sugar for `mutate(this, VariableMutationType.ASSIGN, rhs)` on the previous node’s edge.
   * Matches Java `WfRunVariable.assign` / Python `WfRunVariable.assign`.
   */
  assign(rhs: WorkflowRhs): void
}

export interface NodeOutput {
  jsonPath(path: string): NodeOutput
  /** Field access on JSON_OBJ / struct-shaped outputs */
  get(field: string): NodeOutput
}

/**
 * Return value of `execute`. Java chains `withRetries` / `timeout` / `withExponentialBackoff` here;
 * Python passes `retries` / `timeout_seconds` as kwargs on `execute`. TS can follow Java-style fluency.
 */
export interface TaskNodeOutput extends NodeOutput {
  withRetries(retries: number): TaskNodeOutput
  timeout(timeoutSeconds: number): TaskNodeOutput
  withExponentialBackoff(policy: ExponentialBackoffRetryPolicy): TaskNodeOutput
}

/** Dynamic task name from `thread.format(...)` — parity with Java/Python/C# `LHFormatString`. */
export interface LHFormatString {
  readonly _lhFormatString: true
}

// --- Thread builder (active only while the entrypoint / sub-thread callback runs) ---

export interface WorkflowThread {
  // -- Variable declarations (minimal set; extend with declareBool, declareJsonObj, …) --
  /** Optional default: Python `declare_*(..., default_value=…)` / Go `AddVariableWithDefault`. */
  declareStr(name: string, defaultValue?: WorkflowRhs): WfRunVariable
  declareInt(name: string, defaultValue?: WorkflowRhs): WfRunVariable
  declareDouble(name: string, defaultValue?: WorkflowRhs): WfRunVariable
  declareBool(name: string, defaultValue?: WorkflowRhs): WfRunVariable
  /** Struct: pass registered StructDef name (string). Struct registration stays manual like Go. */
  declareStruct(name: string, structDefName: string): WfRunVariable

  /**
   * Low-level escape hatch matching Python `add_variable` / Go `AddVariable`.
   * Exactly one of `type` or `structDefName` should be set.
   */
  addVariable(name: string, options: { type?: VariableType; structDefName?: string }): WfRunVariable

  // -- Task node --
  /**
   * TASK node. `taskName`: static string, dynamic (`WfRunVariable`), or `format(...)` — same as Java/Python/C#.
   * Args: literals, variables, node outputs — coerced to `VariableAssignment` like other SDKs.
   */
  execute(taskName: string | WfRunVariable | LHFormatString, ...args: WorkflowRhs[]): TaskNodeOutput

  // -- Mutations on the *previous* node’s outgoing edge (same as other SDKs) --
  mutate(lhs: WfRunVariable, operation: VariableMutationType, rhs: WorkflowRhs): void

  // -- Expression helpers for RHS (parity with Java/Python/Go thread helpers) --
  add(lhs: WorkflowRhs, rhs: WorkflowRhs): LHExpression
  multiply(lhs: WorkflowRhs, rhs: WorkflowRhs): LHExpression
  subtract(lhs: WorkflowRhs, rhs: WorkflowRhs): LHExpression
  divide(lhs: WorkflowRhs, rhs: WorkflowRhs): LHExpression

  /**
   * Interpolated task name or other dynamic strings. Java/C# use `{0}` positional placeholders;
   * Python’s `LHFormatString` uses `{}` — pick one convention for TS and document it.
   */
  format(template: string, ...args: WorkflowRhs[]): LHFormatString

  /** Terminal node; workflow thread ends after this (implementation adds EXIT if omitted, like C#). */
  complete(result?: WorkflowRhs): void
}

export type ThreadFunc = (thread: WorkflowThread) => void

// --- Workflow container ---

export interface Workflow {
  readonly name: string

  /** Compile to protobuf for `LittleHorseClient.putWfSpec`. */
  compile(): PutWfSpecRequest

  /**
   * Register dependent metadata then `putWfSpec` — same order as Python `create_workflow_spec` /
   * Java `registerWfSpec` / C# `RegisterWfSpec`. Implementation can take the client returned by
   * `LHConfig#getClient()` from sdk-js/src/LHConfig.ts.
   */
  // registerWfSpec(client: Awaited<ReturnType<LHConfig['getClient']>>): Promise<WfSpec>

  /** Child ThreadSpec’s (spawn, handlers, etc.) — later phases */
  addSubThread(name: string, fn: ThreadFunc): string
}

export declare const Workflow: {
  /**
   * Java: Workflow.newWorkflow
   * Python: Workflow(name, entrypoint)
   * Go: NewWorkflow(fn, name)  → TS uses name-first for ergonomics
   * C#: new Workflow(name, entryPoint)
   */
  create(name: string, entrypoint: ThreadFunc): Workflow
}

// --- Example (illustrative) ---

function exampleOrderFlow() {
  const wf = Workflow.create('example-order-flow', (t) => {
    const orderId = t.declareStr('order-id')
    const total = t.declareDouble('total', 0)

    const fetch = t.execute('fetch-order', orderId)
    total.assign(fetch.jsonPath('$.total'))

    const taxRate = t.declareDouble('tax-rate', 0.08)
    const amountDue = t.add(total, t.multiply(total, taxRate))
    const charged = t.execute('charge-card', amountDue, orderId).withRetries(3)

    t.complete(charged)
  })

  const _request: PutWfSpecRequest = wf.compile()
  return _request
}

void exampleOrderFlow
```
