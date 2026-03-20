/**
 * PROPOSAL — TypeScript WfSpec builder (minimal: variables, tasks, mutations).
 *
 * Lives under repo `proposals/` (not sdk-js). Not compiled. Intent: team review before implementation in sdk-js.
 *
 * Parity targets: sdk-java (Workflow / WorkflowThread), sdk-python (Workflow / WorkflowThread),
 * sdk-go (LHWorkflow / WorkflowThread), sdk-dotnet (Workflow / WorkflowThread).
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
   * `LHConfig#getClient()` from [`sdk-js/src/LHConfig.ts`](../sdk-js/src/LHConfig.ts).
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

// =============================================================================
// Example: “should work” once implemented (this block is illustrative only)
// =============================================================================

function exampleOrderFlow() {
  const wf = Workflow.create('example-order-flow', (t) => {
    const orderId = t.declareStr('order-id')
    const total = t.declareDouble('total', 0)

    const fetch = t.execute('fetch-order', orderId)
    // Prefer assign() for ASSIGN — matches Java/Python WfRunVariable API:
    total.assign(fetch.jsonPath('$.total'))

    const taxRate = t.declareDouble('tax-rate', 0.08)
    const amountDue = t.add(total, t.multiply(total, taxRate))
    const charged = t.execute('charge-card', amountDue, orderId).withRetries(3)

    t.complete(charged)
  })

  const _request: PutWfSpecRequest = wf.compile()
  return _request
}

// Silence unused in proposal file
void exampleOrderFlow
