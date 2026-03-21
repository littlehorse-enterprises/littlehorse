import type { LHExpression } from './lhExpression'
import type { WorkflowRhs } from './workflowRhs'

export interface NodeOutput extends LHExpression {
  jsonPath(path: string): NodeOutput
  get(field: string): NodeOutput
}
