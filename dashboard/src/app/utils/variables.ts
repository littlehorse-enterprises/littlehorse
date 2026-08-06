import {
  Array$ as LHArray,
  Map as LHMap,
  Struct,
  Timestamp,
  TypeDefinition,
  VariableAssignment,
  VariableDef,
  VariableMutationType,
  VariableType,
  VariableValue,
} from 'littlehorse-client/proto'
import { getComparatorSymbol } from './comparatorUtils'
import { normalizeUtcTimestampString } from './timestamp'
import { lhPathToString } from './lhPath'
import { flattenWfRunId, wfRunIdFromFlattenedId } from './wfRun'

/**
 * The set of discriminator values for the `VariableValue.value` oneof.
 * With `@protobuf-ts`, oneofs are discriminated unions keyed on `oneofKind`.
 */
export type VariableValueCase = Exclude<VariableValue['value']['oneofKind'], undefined>

export const getVariableCaseFromTypeDef = (typeDef: TypeDefinition): VariableValueCase => {
  switch (typeDef.definedType?.oneofKind) {
    case 'primitiveType':
      return getVariableCaseFromType(typeDef.definedType.primitiveType)
    case 'structDefId':
      return 'struct'
    case 'inlineArrayDef':
      return 'array'
    case 'inlineMapDef':
      return 'map'
    default:
      throw new Error('Unknown variable type.')
  }
}

export const formatTypeDefinition = (typeDef?: TypeDefinition | TypeDefinition['definedType']): string => {
  const definedType = !typeDef ? undefined : 'oneofKind' in typeDef ? typeDef : typeDef.definedType
  if (!definedType) return 'void'

  switch (definedType.oneofKind) {
    case 'primitiveType': {
      const variableCase = getVariableCaseFromType(definedType.primitiveType)
      return VARIABLE_CASE_LABELS[variableCase]
    }
    case 'structDefId':
      return `Struct<${definedType.structDefId.name},${definedType.structDefId.version}>`
    case 'inlineArrayDef': {
      const nested = definedType.inlineArrayDef.arrayType
      return `Array<${formatTypeDefinition(nested)}>`
    }
    case 'inlineMapDef': {
      const { keyType, valueType } = definedType.inlineMapDef
      return `Map<${formatTypeDefinition(keyType)},${formatTypeDefinition(valueType)}>`
    }
    default:
      throw new Error('Unknown variable type.')
  }
}

/**
 * Maps VariableValue cases to their human-readable display names.
 * This is used for UI components that need to show friendly type names.
 */
export const VARIABLE_CASE_LABELS: Record<VariableValueCase, string> = {
  str: 'String',
  int: 'Integer',
  double: 'Double',
  bool: 'Boolean',
  jsonObj: 'JSON Object',
  jsonArr: 'JSON Array',
  bytes: 'Bytes',
  wfRunId: 'WfRunId',
  struct: 'Struct',
  utcTimestamp: 'UTC Timestamp',
  array: 'Array',
  map: 'Map',
}

/**
 * Retrieves the value of a variable based on its assignment and source.
 * Handles different types of variable sources including expressions, format strings, literals, node outputs, and variable names.
 *
 * @param variable - The variable assignment to retrieve the value from.
 * @param depth - The current depth in nested expressions (default is 0).
 * @returns The formatted string representation of the variable value.
 */
export const getVariable = (variable: VariableAssignment, depth = 0): string => {
  if (!variable || variable.source?.oneofKind === undefined) return ''

  switch (variable.source.oneofKind) {
    case 'expression':
      return formatVariableExpression(variable.source, depth)
    case 'formatString':
      return getValueFromFormatString(variable.source)
    case 'literalValue':
      return getVariableValue(variable.source.literalValue)
    case 'nodeOutput':
      return variable.source.nodeOutput.nodeName
    case 'variableName':
      return getValueFromVariableName(variable.source, variable.path)
    case 'sizeOf':
      return `${getVariable(variable.source.sizeOf.operand!, depth + 1)}.size()`
    default:
      return ''
  }
}

/**
 * Retrieves the value from a VariableValue.
 * Handles different types of values including bytes, wfRunIds, and other primitive types.
 *
 * @param value - The VariableValue to retrieve the value from.
 * @returns A string representation of the value.
 */
export const getVariableValue = ({ value }: VariableValue): string => {
  if (!value || value.oneofKind === undefined) return 'NULL'

  switch (value.oneofKind) {
    case 'bytes':
      return '[bytes]'
    case 'wfRunId':
      return flattenWfRunId(value.wfRunId)
    case 'struct':
      return JSON.stringify(variableValueToJSON({ value }))
    case 'array':
      return JSON.stringify(variableValueToJSON({ value }))
    case 'map':
      return JSON.stringify(variableValueToJSON({ value }))
    case 'utcTimestamp':
      return Timestamp.toDate(value.utcTimestamp).toISOString()
    case 'str':
      return value.str.toString()
    case 'int':
      return value.int.toString()
    case 'double':
      return value.double.toString()
    case 'bool':
      return value.bool.toString()
    case 'jsonObj':
      return value.jsonObj.toString()
    case 'jsonArr':
      return value.jsonArr.toString()
    default:
      return 'NULL'
  }
}

const toNumberIfPossible = (value: unknown): number | unknown => {
  if (typeof value === 'number') return value
  if (typeof value === 'bigint') {
    const parsed = Number(value)
    return Number.isSafeInteger(parsed) ? parsed : value.toString()
  }
  if (typeof value === 'string') {
    const parsed = Number(value)
    return Number.isNaN(parsed) ? value : parsed
  }
  return value
}

const parseJsonStringOrReturn = (value: string): unknown => {
  try {
    return JSON.parse(value)
  } catch {
    return value
  }
}

const structToJSONObject = (struct: Struct): Record<string, unknown> => {
  const structObject: Record<string, unknown> = {}
  if (struct.struct == null) return structObject

  for (const entry of Object.entries(struct.struct.fields)) {
    if (entry[1].value) {
      structObject[entry[0]] = variableValueToJSON(entry[1].value!)
    }
  }

  return structObject
}

const arrayToJSONObject = (array: LHArray): unknown[] => {
  const arrayObject: unknown[] = []
  if (array == null) return arrayObject

  for (const entry of array.items) {
    arrayObject.push(variableValueToJSON(entry))
  }

  return arrayObject
}

const mapToJSONObject = (map: LHMap): Record<string, unknown> => {
  const mapObject: Record<string, unknown> = {}
  if (map == null) return mapObject

  for (const entry of map.entries) {
    if (!entry.key) continue
    const key = variableValueToJSON(entry.key)
    const keyString = typeof key === 'string' ? key : JSON.stringify(key)
    mapObject[keyString] = entry.value ? variableValueToJSON(entry.value) : null
  }

  return mapObject
}

const variableValueToJSON = (variableValue: VariableValue): unknown => {
  const value = variableValue.value
  if (!value || value.oneofKind === undefined) return null

  switch (value.oneofKind) {
    case 'bytes':
      return '[bytes]'
    case 'wfRunId':
      return flattenWfRunId(value.wfRunId)
    case 'struct':
      return structToJSONObject(value.struct)
    case 'array':
      return arrayToJSONObject(value.array)
    case 'map':
      return mapToJSONObject(value.map)
    case 'int':
      return toNumberIfPossible(value.int)
    case 'double':
      return toNumberIfPossible(value.double)
    case 'bool':
      return value.bool
    case 'str':
      return value.str
    case 'jsonObj':
      return parseJsonStringOrReturn(value.jsonObj)
    case 'jsonArr':
      return parseJsonStringOrReturn(value.jsonArr)
    case 'utcTimestamp':
      return Timestamp.toDate(value.utcTimestamp).toISOString()
    default:
      return null
  }
}

/**
 * Converts a string value to a typed VariableValue based on the provided type.
 * Handles various types including JSON objects, arrays, doubles, booleans, strings, integers, bytes, and wfRunIds.
 *
 * @param type - The type of the variable value.
 * @param value - The string value to convert.
 * @returns A VariableValue object with the appropriate type and value.
 */
export const getTypedVariableValue = (type: VariableValueCase, value: string): VariableValue => {
  switch (type) {
    case 'jsonObj':
      return VariableValue.create({ value: { oneofKind: 'jsonObj', jsonObj: JSON.stringify(JSON.parse(value)) } })
    case 'jsonArr':
      return VariableValue.create({ value: { oneofKind: 'jsonArr', jsonArr: JSON.stringify(JSON.parse(value)) } })
    case 'double':
      return VariableValue.create({ value: { oneofKind: 'double', double: parseFloat(value) } })
    case 'bool':
      return VariableValue.create({ value: { oneofKind: 'bool', bool: value.toLowerCase() === 'true' } })
    case 'str':
      return VariableValue.create({ value: { oneofKind: 'str', str: value } })
    case 'int':
      return VariableValue.create({ value: { oneofKind: 'int', int: parseInt(value, 10).toString() } })
    case 'bytes':
      return VariableValue.create({ value: { oneofKind: 'bytes', bytes: new Uint8Array(Buffer.from(value)) } })
    case 'wfRunId':
      return VariableValue.create({ value: { oneofKind: 'wfRunId', wfRunId: wfRunIdFromFlattenedId(value) } })
    case 'struct':
      return VariableValue.create({ value: { oneofKind: 'struct', struct: Struct.fromJsonString(value) } })
    case 'utcTimestamp':
      return VariableValue.create({
        value: {
          oneofKind: 'utcTimestamp',
          utcTimestamp: Timestamp.fromDate(new Date(normalizeUtcTimestampString(value))),
        },
      })
    case 'array':
      return VariableValue.create({ value: { oneofKind: 'array', array: LHArray.fromJsonString(value) } })
    case 'map':
      return VariableValue.create({ value: { oneofKind: 'map', map: LHMap.fromJsonString(value) } })
    default:
      throw new Error(`Unknown variable value type: ${type}`)
  }
}

/**
 * Renders a primitive JS value (already JSON-parsed) into the string shape that
 * `getTypedVariableValue` consumes for that primitive type.
 */
const primitiveJsonToString = (type: VariableType, value: unknown): string => {
  if (type === VariableType.JSON_OBJ || type === VariableType.JSON_ARR) {
    return typeof value === 'string' ? value : JSON.stringify(value)
  }
  return typeof value === 'string' ? value : String(value)
}

/**
 * Converts an already-JSON-parsed value into a VariableValue, using a TypeDefinition to
 * type container keys/elements. This accepts the same human-friendly shape the dashboard
 * *displays* (via `variableValueToJSON`): a Map is `{"one":1}` and an Array is `[1,2]`,
 * NOT proto-JSON (`{"entries":[...]}` / `{"items":[...]}`). It is the input-side inverse of
 * `variableValueToJSON` for the container types.
 */
const jsonValueToVariableValue = (typeDef: TypeDefinition | undefined, value: unknown): VariableValue => {
  const definedType = typeDef?.definedType
  switch (definedType?.oneofKind) {
    case 'inlineArrayDef': {
      if (!Array.isArray(value)) throw new Error('Expected a JSON array')
      const elementType = definedType.inlineArrayDef.arrayType
      return VariableValue.create({
        value: { oneofKind: 'array', array: { items: value.map(el => jsonValueToVariableValue(elementType, el)) } },
      })
    }
    case 'inlineMapDef': {
      if (value === null || typeof value !== 'object' || Array.isArray(value)) {
        throw new Error('Expected a JSON object')
      }
      const { keyType, valueType } = definedType.inlineMapDef
      // Object.entries always yields string keys; the keyType drives their real parsing.
      const entries = Object.entries(value as Record<string, unknown>).map(([k, v]) => ({
        key: jsonValueToVariableValue(keyType, k),
        value: jsonValueToVariableValue(valueType, v),
      }))
      return VariableValue.create({ value: { oneofKind: 'map', map: { entries } } })
    }
    case 'primitiveType':
      return getTypedVariableValue(
        getVariableCaseFromType(definedType.primitiveType),
        primitiveJsonToString(definedType.primitiveType, value)
      )
    case 'structDefId':
      return VariableValue.create({
        value: { oneofKind: 'struct', struct: Struct.fromJsonString(JSON.stringify(value)) },
      })
    default:
      throw new Error('Unsupported type for value conversion.')
  }
}

/**
 * Like `getTypedVariableValue`, but resolves container element/key types from a
 * TypeDefinition, so Map and Array inputs can be provided in the same human-friendly JSON
 * the dashboard displays (e.g. `{"one":1}`) rather than raw proto-JSON. Primitives and
 * structs fall back to the existing string-based path.
 */
export const getTypedVariableValueFromTypeDef = (typeDef: TypeDefinition | undefined, value: string): VariableValue => {
  const oneofKind = typeDef?.definedType?.oneofKind
  if (oneofKind === 'inlineArrayDef' || oneofKind === 'inlineMapDef') {
    return jsonValueToVariableValue(typeDef, JSON.parse(value))
  }
  if (!typeDef?.definedType) throw new Error('Variable type is unknown.')
  return getTypedVariableValue(getVariableCaseFromTypeDef(typeDef), value)
}

/**
 * Builds the VariableValue for a WfRun variable-search filter, handling both the new
 * `typeDef` (including Map/Array via friendly JSON) and legacy `.type`-only VariableDefs.
 * Throws on invalid input — callers (dialog validation, filter builder) must catch.
 */
export const getVariableFilterValue = (varDef: VariableDef, value: string): VariableValue =>
  varDef.typeDef?.definedType
    ? getTypedVariableValueFromTypeDef(varDef.typeDef, value)
    : getTypedVariableValue(getVariableDefType(varDef), value)

/**
 * Converts a VariableValue (from a `VariableDef.defaultValue`) into the
 * representation used by primitive form fields in the Execute WfRun form.
 *
 * Returns `undefined` for non-primitive cases (struct, array, bytes, wfRunId)
 * since those need bespoke rendering.
 */
export const getPrimitiveFormDefaultValue = (defaultValue?: VariableValue): unknown => {
  const union = defaultValue?.value
  if (!union || union.oneofKind === undefined) return undefined

  switch (union.oneofKind) {
    case 'bool':
      return union.bool ? 'true' : 'false'
    case 'int':
      return union.int
    case 'double':
      return union.double
    case 'str':
      return union.str
    case 'jsonObj':
      return union.jsonObj
    case 'jsonArr':
      return union.jsonArr
    case 'utcTimestamp':
      return Timestamp.toDate(union.utcTimestamp).toISOString()
    default:
      return undefined
  }
}

/**
 * After 0.13.2, the `VariableDef.type` and `VariableDef.maskedValue` fields are deprecated.
 * These fields are replaced with `VariableDef.typeDef`.
 *
 * Old server versions may keep around both old and new Variables, so this function
 * determines which typing strategy a Variable uses.
 */
export const getVariableDefType = (varDef: VariableDef): VariableValueCase => {
  if (varDef.typeDef && varDef.typeDef.definedType) {
    // Delegate to the single source of truth so the two type-dispatch switches
    // cannot drift out of sync (they previously did: map support was added here
    // but not to getVariableCaseFromTypeDef, crashing the ExternalEventDef page).
    return getVariableCaseFromTypeDef(varDef.typeDef)
  } else if (varDef.type) {
    return getVariableCaseFromType(varDef.type)
  }
  throw new Error('Variable must have type or typeDef.')
}

/**
 * Maps a VariableType to its corresponding VariableValue case.
 * This is used to determine the type of value stored in a VariableValue.
 *
 * @param type - The VariableType to map.
 * @returns The corresponding VariableValue case.
 */
export const getVariableCaseFromType = (type: VariableType): VariableValueCase => {
  switch (type) {
    case VariableType.BOOL:
      return 'bool'
    case VariableType.DOUBLE:
      return 'double'
    case VariableType.INT:
      return 'int'
    case VariableType.STR:
      return 'str'
    case VariableType.JSON_OBJ:
      return 'jsonObj'
    case VariableType.JSON_ARR:
      return 'jsonArr'
    case VariableType.WF_RUN_ID:
      return 'wfRunId'
    case VariableType.BYTES:
      return 'bytes'
    case VariableType.TIMESTAMP:
      return 'utcTimestamp'
    default:
      throw new Error(`Unknown variable type: ${type}`)
  }
}

const getValueFromVariableName = (
  source: Extract<VariableAssignment['source'], { oneofKind: 'variableName' }>,
  path?: VariableAssignment['path']
): string => {
  const value = source.variableName
  if (!value) return ''

  if (path?.oneofKind == 'jsonPath') return `{${path.jsonPath.replace('$', value)}}`
  if (path?.oneofKind == 'lhPath') return `{${lhPathToString(path.lhPath).replace('$', value)}}`
  return `{${value}}`
}

const getValueFromFormatString = (
  source: Extract<VariableAssignment['source'], { oneofKind: 'formatString' }>
): string => {
  const value = source.formatString
  const template = getVariable(value.format!)
  const args = value.args.map(getVariable)

  return `${template}`.replace(/{(\d+)}/g, (_, index) => `${args[index]}`)
}

const getExpressionSymbol = (expression: VariableMutationType): string => {
  switch (expression) {
    case VariableMutationType.ASSIGN:
      return '='
    case VariableMutationType.ADD:
      return '+'
    case VariableMutationType.SUBTRACT:
      return '-'
    case VariableMutationType.DIVIDE:
      return '/'
    case VariableMutationType.POW:
      return '**'
    case VariableMutationType.MULTIPLY:
      return '*'
    case VariableMutationType.EXTEND:
      return 'extends'
    case VariableMutationType.REMOVE_IF_PRESENT:
      return 'removeIfPresent'
    case VariableMutationType.REMOVE_INDEX:
      return 'removeIndex'
    case VariableMutationType.REMOVE_KEY:
      return 'removeKey'
    default:
      return ''
  }
}

const formatVariableExpression = (
  source: Extract<VariableAssignment['source'], { oneofKind: 'expression' }>,
  depth = 0
): string => {
  const { lhs, rhs, operation } = source.expression
  if (!operation || operation.oneofKind === undefined) return ''

  let symbol: string
  let useDotNotation = false

  if (operation.oneofKind === 'mutationType') {
    const mt = operation.mutationType
    symbol = getExpressionSymbol(mt)
    useDotNotation =
      mt === VariableMutationType.REMOVE_IF_PRESENT ||
      mt === VariableMutationType.REMOVE_INDEX ||
      mt === VariableMutationType.REMOVE_KEY ||
      mt === VariableMutationType.EXTEND
  } else {
    symbol = getComparatorSymbol(operation.comparator)
  }

  const result = useDotNotation
    ? `${getVariable(lhs!, depth + 1)}.${symbol}(${getVariable(rhs!, depth + 1)})`
    : `${getVariable(lhs!, depth + 1)} ${symbol} ${getVariable(rhs!, depth + 1)}`
  return depth > 0 ? `(${result})` : result
}
