import { VariableValue, Struct, StructField } from '../proto/type_definition'
import { VarNameAndVal } from '../proto/task_run'
import { ScheduledTask } from '../proto/service'

/**
 * Extracts a JavaScript value from a VariableValue proto object.
 */
export function extractVariableValue(variable: VariableValue | undefined): unknown {
  if (!variable?.value) {
    return undefined
  }

  const value = variable.value
  switch (value.oneofKind) {
    case 'str':
      return value.str
    case 'int':
      return Number(value.int)
    case 'double':
      return value.double
    case 'bool':
      return value.bool
    case 'bytes':
      return value.bytes
    case 'jsonObj':
      return JSON.parse(value.jsonObj)
    case 'jsonArr':
      return JSON.parse(value.jsonArr)
    case 'struct':
      return extractStruct(value.struct)
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
    return { value: { oneofKind: undefined } }
  }

  if (typeof value === 'string') {
    return { value: { oneofKind: 'str', str: value } }
  }

  if (typeof value === 'number') {
    if (Number.isInteger(value)) {
      return { value: { oneofKind: 'int', int: String(value) } }
    }
    return { value: { oneofKind: 'double', double: value } }
  }

  if (typeof value === 'boolean') {
    return { value: { oneofKind: 'bool', bool: value } }
  }

  if (Buffer.isBuffer(value) || value instanceof Uint8Array) {
    return { value: { oneofKind: 'bytes', bytes: Buffer.from(value) } }
  }

  if (Array.isArray(value)) {
    return { value: { oneofKind: 'jsonArr', jsonArr: JSON.stringify(value) } }
  }

  if (typeof value === 'object') {
    return { value: { oneofKind: 'jsonObj', jsonObj: JSON.stringify(value) } }
  }

  return { value: { oneofKind: 'str', str: String(value) } }
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
