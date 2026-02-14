import { VariableValue, Struct, StructField, InlineStruct } from '../proto/variable'
import { VarNameAndVal } from '../proto/task_run'
import { ScheduledTask } from '../proto/service'

/**
 * Extracts a JavaScript value from a VariableValue proto object.
 */
export function extractVariableValue(variable: VariableValue | undefined): unknown {
  if (!variable?.value) {
    return undefined
  }

  switch (variable.value.$case) {
    case 'str':
      return variable.value.value
    case 'int':
      return variable.value.value
    case 'double':
      return variable.value.value
    case 'bool':
      return variable.value.value
    case 'bytes':
      return variable.value.value
    case 'jsonObj':
      return JSON.parse(variable.value.value)
    case 'jsonArr':
      return JSON.parse(variable.value.value)
    case 'struct':
      return extractStruct(variable.value.value)
    default:
      return undefined
  }
}

/**
 * Extracts the input arguments from a ScheduledTask as an array of JS values.
 */
export function extractTaskArgs(task: ScheduledTask): unknown[] {
  return task.variables.map((v: VarNameAndVal) => extractVariableValue(v.value))
}

/**
 * Converts a JavaScript value to a VariableValue proto object.
 */
export function toVariableValue(value: unknown): VariableValue {
  if (value === null || value === undefined) {
    return { value: undefined }
  }

  if (typeof value === 'string') {
    return { value: { $case: 'str', value } }
  }

  if (typeof value === 'number') {
    if (Number.isInteger(value)) {
      return { value: { $case: 'int', value } }
    }
    return { value: { $case: 'double', value } }
  }

  if (typeof value === 'boolean') {
    return { value: { $case: 'bool', value } }
  }

  if (Buffer.isBuffer(value) || value instanceof Uint8Array) {
    return { value: { $case: 'bytes', value: Buffer.from(value) } }
  }

  if (Array.isArray(value)) {
    return { value: { $case: 'jsonArr', value: JSON.stringify(value) } }
  }

  if (typeof value === 'object') {
    return { value: { $case: 'jsonObj', value: JSON.stringify(value) } }
  }

  return { value: { $case: 'str', value: String(value) } }
}

/**
 * Recursively extracts a Struct proto value into a plain JavaScript object.
 */
function extractStruct(struct: Struct): Record<string, unknown> {
  const result: Record<string, unknown> = {}
  if (struct.struct?.fields) {
    for (const [key, field] of Object.entries(struct.struct.fields)) {
      result[key] = extractVariableValue((field as StructField).value)
    }
  }
  return result
}
