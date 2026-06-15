import type { InlineStructBuilder } from '../../proto/common_wfspec'
import type { InlineLHStructBuilder } from '../inlineLHStructBuilder'
import { buildInlineProto } from './structBuilderUtils'
import type { WorkflowThreadImpl } from './workflowThreadImpl'

export class InlineLHStructBuilderImpl implements InlineLHStructBuilder {
  private readonly fields: Record<string, unknown> = {}

  constructor(private readonly thread: WorkflowThreadImpl) {}

  put(fieldName: string, value: unknown): InlineLHStructBuilder {
    this.fields[fieldName] = value
    return this
  }

  toInlineProto(): InlineStructBuilder {
    return buildInlineProto(this.fields, this.thread)
  }
}
