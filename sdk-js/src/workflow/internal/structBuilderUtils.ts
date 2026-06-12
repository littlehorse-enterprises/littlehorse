import type { InlineStructBuilder, InlineStructFieldValue } from '../../proto/common_wfspec'
import { InlineLHStructBuilderImpl } from './inlineLHStructBuilderImpl'
import type { WorkflowThreadImpl } from './workflowThreadImpl'

function buildFieldValue(value: unknown, thread: WorkflowThreadImpl): InlineStructFieldValue {
  if (value instanceof InlineLHStructBuilderImpl) {
    return { structValue: { $case: 'subStructure', value: value.toInlineProto() } }
  }
  return { structValue: { $case: 'simpleValue', value: thread.assignVariable(value) } }
}

export function buildInlineProto(
  fields: Record<string, unknown>,
  thread: WorkflowThreadImpl
): InlineStructBuilder {
  const result: InlineStructBuilder = { fields: {} }
  for (const [key, value] of Object.entries(fields)) {
    result.fields[key] = buildFieldValue(value, thread)
  }
  return result
}
