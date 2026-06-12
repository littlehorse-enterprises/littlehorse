import type { LHExpression } from './lhExpression'

export interface NodeOutput extends LHExpression {
  jsonPath(path: string): NodeOutput
  get(field: string): NodeOutput
}
