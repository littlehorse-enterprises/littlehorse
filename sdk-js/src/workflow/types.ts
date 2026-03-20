import type { VariableType } from '../proto/common_enums'
import type { ExponentialBackoffRetryPolicy, VariableMutationType } from '../proto/common_wfspec'
import type { PutWfSpecRequest } from '../proto/service'

/** Values allowed on the right-hand side of assignments, mutations, and expressions. */
export type WorkflowRhs = unknown

/** Composable RHS building — mirrors Java `LHExpression` (instance methods on values, not on the thread). */
export interface LHExpression {
  add(other: WorkflowRhs): LHExpression
  subtract(other: WorkflowRhs): LHExpression
  multiply(other: WorkflowRhs): LHExpression
  divide(other: WorkflowRhs): LHExpression
}

export interface WfRunVariable extends LHExpression {
  readonly name: string
  jsonPath(path: string): WfRunVariable
  get(field: string): WfRunVariable
  assign(rhs: WorkflowRhs): void
}

export interface NodeOutput extends LHExpression {
  jsonPath(path: string): NodeOutput
  get(field: string): NodeOutput
}

export interface TaskNodeOutput extends NodeOutput {
  withRetries(retries: number): TaskNodeOutput
  timeout(timeoutSeconds: number): TaskNodeOutput
  withExponentialBackoff(policy: ExponentialBackoffRetryPolicy): TaskNodeOutput
}

export interface LHFormatString {
  readonly _lhFormatString: true
}

export interface WorkflowThread {
  declareStr(name: string, defaultValue?: WorkflowRhs): WfRunVariable
  declareInt(name: string, defaultValue?: WorkflowRhs): WfRunVariable
  declareDouble(name: string, defaultValue?: WorkflowRhs): WfRunVariable
  declareBool(name: string, defaultValue?: WorkflowRhs): WfRunVariable
  declareStruct(name: string, structDefName: string): WfRunVariable
  addVariable(name: string, options: { type?: VariableType; structDefName?: string }): WfRunVariable

  execute(taskName: string | WfRunVariable | LHFormatString, ...args: WorkflowRhs[]): TaskNodeOutput

  mutate(lhs: WfRunVariable, operation: VariableMutationType, rhs: WorkflowRhs): void

  /**
   * Build a format string for task names or other template parameters.
   * Placeholders use Java/C# style `{0}`, `{1}`, … (not Python `{}`).
   */
  format(template: string, ...args: WorkflowRhs[]): LHFormatString

  complete(result?: WorkflowRhs): void
}

export type ThreadFunc = (thread: WorkflowThread) => void

/** Compiled workflow; instances will be produced by {@link Workflow.create} once implemented. */
export interface Workflow {
  readonly name: string
  compile(): PutWfSpecRequest
}
