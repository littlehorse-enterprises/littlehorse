import type { WorkflowRhs } from './workflowRhs'

export interface LHExpression {
  add(other: WorkflowRhs): LHExpression
  subtract(other: WorkflowRhs): LHExpression
  multiply(other: WorkflowRhs): LHExpression
  divide(other: WorkflowRhs): LHExpression
}
