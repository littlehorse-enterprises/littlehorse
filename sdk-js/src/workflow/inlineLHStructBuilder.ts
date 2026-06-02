import type { WorkflowRhs } from './workflowRhs'

export interface InlineLHStructBuilder {
  put(fieldName: string, value: WorkflowRhs | InlineLHStructBuilder): InlineLHStructBuilder
}
