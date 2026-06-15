import type { WorkflowRhs } from './workflowRhs'

export interface LHStructBuilder {
  put(fieldName: string, value: WorkflowRhs | import('./inlineLHStructBuilder').InlineLHStructBuilder): LHStructBuilder
}
