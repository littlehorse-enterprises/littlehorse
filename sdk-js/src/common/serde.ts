import { Timestamp } from '../proto/google/protobuf/timestamp'
import { Struct, StructField, VariableValue } from '../proto/type_definition'
import { VariableType } from '../proto/common_enums'
import { TaskRunId, WfRunId } from '../proto/object_id'
import { LHSerdeError } from './errors'
import type { LHTypeAdapterRegistry } from './typeAdapters'

/**
 * The single JS <-> VariableValue conversion layer (Java: common/LHLibUtil).
 *
 * Everything above this — the wfsdk compiling literals, the worker mapping
 * task inputs and outputs — goes through here. Encoding is verified against
 * `golden/serde.json`, emitted by the Java SDK, because two SDKs can each
 * "work" while disagreeing on bytes, and that disagreement silently corrupts
 * data written by one and read by the other.
 */

/**
 * Serializes JSON the way the Java SDK does.
 *
 * Java omits null *object fields* (verified against the fixture:
 * `{present: 'yes', absent: null}` encodes as `{"present":"yes"}`), including
 * in nested objects — but keeps nulls inside arrays (`[1,null,2]`). Plain
 * JSON.stringify keeps both, so it cannot be used directly.
 */
export function lhJsonStringify(value: unknown): string {
  return JSON.stringify(stripNullFields(value))
}

function stripNullFields(value: unknown): unknown {
  if (Array.isArray(value)) {
    // Array elements are preserved as-is, nulls included.
    return value.map(item => (item === null ? null : stripNullFields(item)))
  }
  if (value !== null && typeof value === 'object' && !(value instanceof Date) && !(value instanceof Uint8Array)) {
    const out: Record<string, unknown> = {}
    for (const [key, item] of Object.entries(value as Record<string, unknown>)) {
      if (item === null || item === undefined) continue
      out[key] = stripNullFields(item)
    }
    return out
  }
  return value
}

/**
 * Converts a JS value to a VariableValue (Java: LHLibUtil#objToVarVal).
 *
 * JS has one numeric type, so an integer-valued number becomes INT and
 * anything else DOUBLE; pass a bigint to force INT.
 */
export function objToVarVal(value: unknown, registry?: LHTypeAdapterRegistry): VariableValue {
  if (value === null || value === undefined) {
    return VariableValue.create()
  }

  // A registered adapter wins over the built-in mapping, so user types can
  // control their own encoding (Java: LHTypeAdapterRegistry).
  const adapter = registry?.findForValue(value)
  if (adapter !== undefined) {
    return adapter.serialize(value)
  }

  // An already-encoded VariableValue passes through. Generated protobuf-ts
  // messages are interfaces, not classes, so this is a structural check.
  if (isVariableValue(value)) {
    return value
  }
  if (typeof value === 'number') {
    return Number.isInteger(value)
      ? VariableValue.create({ value: { oneofKind: 'int', int: String(value) } })
      : VariableValue.create({ value: { oneofKind: 'double', double: value } })
  }
  if (typeof value === 'bigint') {
    return VariableValue.create({ value: { oneofKind: 'int', int: String(value) } })
  }
  if (typeof value === 'string') {
    return VariableValue.create({ value: { oneofKind: 'str', str: value } })
  }
  if (typeof value === 'boolean') {
    return VariableValue.create({ value: { oneofKind: 'bool', bool: value } })
  }
  if (value instanceof Uint8Array) {
    return VariableValue.create({ value: { oneofKind: 'bytes', bytes: value } })
  }
  if (value instanceof Date) {
    return VariableValue.create({ value: { oneofKind: 'utcTimestamp', utcTimestamp: Timestamp.fromDate(value) } })
  }
  // NOTE: no structural detection of WfRunId here, deliberately. Java can do
  // `o instanceof WfRunId` safely; JS cannot, and sniffing for `{id}` would
  // silently encode the very common plain object `{id: 'abc'}` as a
  // WF_RUN_ID — dropping every other field with it. To store a WF_RUN_ID,
  // pass a built VariableValue, which the pass-through above accepts.
  if (Array.isArray(value)) {
    return VariableValue.create({ value: { oneofKind: 'jsonArr', jsonArr: lhJsonStringify(value) } })
  }
  if (typeof value === 'object') {
    return VariableValue.create({ value: { oneofKind: 'jsonObj', jsonObj: lhJsonStringify(value) } })
  }
  throw new LHSerdeError(`Cannot convert a value of type ${typeof value} to a VariableValue`)
}

/** Converts a VariableValue back to a JS value (Java: LHLibUtil#varValToObj). */
export function varValToObj(value: VariableValue | undefined, registry?: LHTypeAdapterRegistry): unknown {
  if (!value?.value) return undefined

  const adapter = registry?.findForVariableValue(value)
  if (adapter !== undefined) {
    return adapter.deserialize(value)
  }

  const inner = value.value
  switch (inner.oneofKind) {
    case 'str':
      return inner.str
    case 'int':
      return Number(inner.int)
    case 'double':
      return inner.double
    case 'bool':
      return inner.bool
    case 'bytes':
      return inner.bytes
    case 'utcTimestamp':
      return Timestamp.toDate(inner.utcTimestamp)
    case 'wfRunId':
      return inner.wfRunId
    case 'jsonObj':
      return parseJson(inner.jsonObj)
    case 'jsonArr':
      return parseJson(inner.jsonArr)
    case 'struct':
      return structToObj(inner.struct)
    default:
      return undefined
  }
}

function parseJson(raw: string): unknown {
  try {
    return JSON.parse(raw)
  } catch (err) {
    throw new LHSerdeError(`Could not parse JSON from the server: ${(err as Error).message}`)
  }
}

/** Recursively converts a Struct proto into a plain object. */
export function structToObj(struct: Struct): Record<string, unknown> {
  const out: Record<string, unknown> = {}
  for (const [key, field] of Object.entries(struct.struct?.fields ?? {})) {
    out[key] = varValToObj((field as StructField).value)
  }
  return out
}

/** The VariableType a VariableValue represents (Java: LHLibUtil#fromValueCase). */
export function variableTypeOf(value: VariableValue): VariableType {
  switch (value.value.oneofKind) {
    case 'str':
      return VariableType.STR
    case 'int':
      return VariableType.INT
    case 'double':
      return VariableType.DOUBLE
    case 'bool':
      return VariableType.BOOL
    case 'bytes':
      return VariableType.BYTES
    case 'utcTimestamp':
      return VariableType.TIMESTAMP
    case 'jsonObj':
      return VariableType.JSON_OBJ
    case 'jsonArr':
      return VariableType.JSON_ARR
    case 'wfRunId':
      return VariableType.WF_RUN_ID
    default:
      throw new LHSerdeError(`Cannot infer a VariableType from ${value.value.oneofKind ?? 'an empty value'}`)
  }
}

function isVariableValue(value: unknown): value is VariableValue {
  return (
    typeof value === 'object' &&
    value !== null &&
    'value' in value &&
    typeof (value as VariableValue).value === 'object' &&
    (value as VariableValue).value !== null &&
    'oneofKind' in (value as VariableValue).value
  )
}

/**
 * Renders a WfRunId as a string. Child runs are joined to their parent with
 * `_`, so `parent-1_child-1` denotes a child of `parent-1`.
 */
export function wfRunIdToString(id: WfRunId): string {
  return id.parentWfRunId ? `${wfRunIdToString(id.parentWfRunId)}_${id.id}` : id.id
}

/** Parses the `wfRunIdToString` format back into a WfRunId. */
export function wfRunIdFromString(id: string): WfRunId {
  const separator = id.lastIndexOf('_')
  if (separator === -1) {
    return WfRunId.create({ id })
  }
  return WfRunId.create({
    id: id.substring(separator + 1),
    parentWfRunId: wfRunIdFromString(id.substring(0, separator)),
  })
}

/** Renders a TaskRunId as `<wfRunId>/<taskGuid>`. */
export function taskRunIdToString(id: TaskRunId): string {
  return `${id.wfRunId ? wfRunIdToString(id.wfRunId) : ''}/${id.taskGuid}`
}
