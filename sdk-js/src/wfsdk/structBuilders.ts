import { InlineStructBuilder, InlineStructFieldValue, StructBuilder } from '../proto/common_wfspec'
import { toVariableAssignment } from './builder'

type FieldValue = unknown | InlineLHStructBuilder

/**
 * Builds a schemaless (inline) struct value from named fields. Fields may be
 * any LH value or a nested inline builder.
 */
export class InlineLHStructBuilder {
  // Insertion order is preserved (Java uses a LinkedHashMap) so the compiled
  // proto is stable across runs.
  private readonly fields = new Map<string, FieldValue>()

  put(fieldName: string, value: FieldValue): this {
    this.fields.set(fieldName, value)
    return this
  }

  toInlineProto(): InlineStructBuilder {
    const out = InlineStructBuilder.create()
    for (const [name, value] of this.fields) {
      out.fields[name] = buildFieldValue(value)
    }
    return out
  }
}

/** Builds a value conforming to a registered StructDef. */
export class LHStructBuilder {
  private readonly inline = new InlineLHStructBuilder()

  constructor(
    private readonly structDefName: string,
    private readonly version?: number
  ) {}

  put(fieldName: string, value: FieldValue): this {
    this.inline.put(fieldName, value)
    return this
  }

  toProto(): StructBuilder {
    return StructBuilder.create({
      structDefId: { name: this.structDefName, version: this.version ?? -1 },
      value: this.inline.toInlineProto(),
    })
  }
}

function buildFieldValue(value: FieldValue): InlineStructFieldValue {
  if (value instanceof InlineLHStructBuilder) {
    return InlineStructFieldValue.create({
      structValue: { oneofKind: 'subStructure', subStructure: value.toInlineProto() },
    })
  }
  return InlineStructFieldValue.create({
    structValue: { oneofKind: 'simpleValue', simpleValue: toVariableAssignment(value) },
  })
}
