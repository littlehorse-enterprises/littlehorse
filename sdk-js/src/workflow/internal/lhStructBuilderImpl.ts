import type { StructBuilder } from '../../proto/common_wfspec'
import { InlineLHStructBuilderImpl } from './inlineLHStructBuilderImpl'
import type { InlineLHStructBuilder } from '../inlineLHStructBuilder'
import type { LHStructBuilder } from '../lhStructBuilder'
import { buildInlineProto } from './structBuilderUtils'
import type { WorkflowThreadImpl } from './workflowThreadImpl'

export class LHStructBuilderImpl implements LHStructBuilder {
  private readonly fields: Record<string, unknown> = {}

  constructor(
    private readonly thread: WorkflowThreadImpl,
    private readonly structDefName: string,
    private readonly version?: number
  ) {}

  put(fieldName: string, value: unknown): LHStructBuilder {
    this.fields[fieldName] = value
    return this
  }

  toProto(): StructBuilder {
    return {
      structDefId: { name: this.structDefName, version: this.version ?? -1 },
      value: buildInlineProto(this.fields, this.thread),
    }
  }
}
