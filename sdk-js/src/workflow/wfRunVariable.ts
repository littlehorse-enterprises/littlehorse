import type { LHExpression } from './lhExpression'
import type { WorkflowRhs } from './workflowRhs'

export interface WfRunVariable extends LHExpression {
  readonly name: string
  jsonPath(path: string): WfRunVariable
  get(field: string): WfRunVariable
  assign(rhs: WorkflowRhs): void
}
