import {
  Array as LHArray,
  Struct,
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

export const getVariableCaseFromTypeDef = (typeDef: TypeDefinition): NonNullable<VariableValue['value']>['$case'] => {
  switch (typeDef.definedType?.$case) {
    case 'primitiveType':
      return getVariableCaseFromType(typeDef.definedType.value)
    case 'structDefId':
      return 'struct'
    default:
      throw new Error('Unknown variable type.')
  }
}

export const formatTypeDefinition = (typeDef?: TypeDefinition | TypeDefinition['definedType']): string => {
  const definedType = !typeDef ? undefined : '$case' in typeDef ? typeDef : typeDef.definedType
  if (!definedType) return 'void'

  switch (definedType.$case) {
    case 'primitiveType': {
      const variableCase = getVariableCaseFromType(definedType.value)
      return VARIABLE_CASE_LABELS[variableCase]
    }
    case 'structDefId':
      return `Struct<${definedType.value.name},${definedType.value.version}>`
    case 'inlineArrayDef': {
      const nested = definedType.value.arrayType
      return `Array<${formatTypeDefinition(nested)}>`
    }
    default:
      throw new Error('Unknown variable type.')
  }
}

/**
 * Maps VariableValue cases to their human-readable display names.
 * This is used for UI components that need to show friendly type names.
 */
export const VARIABLE_CASE_LABELS: Record<NonNullable<VariableValue['value']>['$case'], string> = {
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
  if (!variable || !variable.source) return ''

  switch (variable.source.$case) {
    case 'expression':
      return formatVariableExpression(variable.source, depth)
    case 'formatString':
      return getValueFromFormatString(variable.source)
    case 'literalValue':
      return getVariableValue(variable.source.value)
    case 'nodeOutput':
      return variable.source.value.nodeName
    case 'variableName':
      return getValueFromVariableName(variable.source, variable.path)
    case 'sizeOf':
      return `${getVariable(variable.source.value.operand!, depth + 1)}.size()`
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
  if (!value || Object.keys(value).length === 0) return 'NULL'

  switch (value.$case) {
    case 'bytes':
      return '[bytes]'
    case 'wfRunId':
      return flattenWfRunId(value.value)
    case 'struct':
      return JSON.stringify(variableValueToJSON({ value }))
    case 'array':
      return JSON.stringify(variableValueToJSON({ value }))
    default:
      return value.value.toString()
  }
}

/**
 * Formats a JSON string or returns the original value if parsing fails.
 * This is useful for displaying JSON in a readable format in the UI.
 *
 * @param value - The string value to format as JSON.
 * @returns A formatted JSON string or the original value if parsing fails.
 */
export const formatJsonOrReturnOriginalValue = (value: string) => {
  try {
    const json = JSON.parse(value)
    return JSON.stringify(json, null, 2)
  } catch {
    return value
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
      structObject[entry[0]] = variableValueToJSON(entry[1].value)
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

export const variableValueToJSON = (variableValue: VariableValue): unknown => {
  const value = variableValue.value
  if (!value) return null

  switch (value.$case) {
    case 'bytes':
      return '[bytes]'
    case 'wfRunId':
      return flattenWfRunId(value.value)
    case 'struct':
      return structToJSONObject(value.value)
    case 'array':
      return arrayToJSONObject(value.value)
    case 'int':
      return toNumberIfPossible(value.value)
    case 'double':
      return toNumberIfPossible(value.value)
    case 'bool':
    case 'str':
      return value.value
    case 'jsonObj':
    case 'jsonArr':
      return parseJsonStringOrReturn(value.value)
    case 'utcTimestamp':
      return value.value.toString()
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
export const getTypedVariableValue = (
  type: NonNullable<VariableValue['value']>['$case'],
  value: string
): VariableValue => {
  switch (type) {
    case 'jsonObj':
      return VariableValue.fromJSON({ jsonObj: JSON.stringify(JSON.parse(value)) })
    case 'jsonArr':
      return VariableValue.fromJSON({ jsonArr: JSON.stringify(JSON.parse(value)) })
    case 'double':
      return VariableValue.fromJSON({ double: parseFloat(value) })
    case 'bool':
      return VariableValue.fromJSON({ bool: value.toLowerCase() === 'true' })
    case 'str':
      return VariableValue.fromJSON({ str: value })
    case 'int':
      return VariableValue.fromJSON({ int: parseInt(value, 10) })
    case 'bytes':
      return VariableValue.fromJSON({ bytes: Buffer.from(value) })
    case 'wfRunId':
      return VariableValue.fromJSON({ wfRunId: wfRunIdFromFlattenedId(value) })
    case 'struct':
      return VariableValue.fromJSON({ struct: Struct.fromJSON(value) })
    case 'utcTimestamp':
      return VariableValue.fromJSON({ utcTimestamp: normalizeUtcTimestampString(value) })
    case 'array': {
      return VariableValue.fromJSON({ array: LHArray.fromJSON(value) })
    }
    default:
      throw new Error(`Unknown variable value type: ${type}`)
  }
}

/**
 * After 0.13.2, the `VariableDef.type` and `VariableDef.maskedValue` fields are deprecated.
 * These fields are replaced with `VariableDef.typeDef`.
 *
 * Old server versions may keep around both old and new Variables, so this function
 * determines which typing strategy a Variable uses.
 */
export const getVariableDefType = (varDef: VariableDef): NonNullable<VariableValue['value']>['$case'] => {
  if (varDef.typeDef && varDef.typeDef.definedType) {
    const { $case, value } = varDef.typeDef.definedType
    switch ($case) {
      case 'primitiveType':
        return getVariableCaseFromType(value)
      case 'structDefId':
        return 'struct'
      case 'inlineArrayDef':
        return 'array'
      default:
        throw new Error('Unknown variable type.')
    }
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
export const getVariableCaseFromType = (type: VariableType): NonNullable<VariableValue['value']>['$case'] => {
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
  { value }: Extract<VariableAssignment['source'], { $case: 'variableName' }>,
  path?: Extract<VariableAssignment['path'], {}>
): string => {
  if (!value) return ''

  if (path?.$case == 'jsonPath') return `{${path.value.replace('$', value)}}`
  if (path?.$case == 'lhPath') return `{${lhPathToString(path.value).replace('$', value)}}`
  return `{${value}}`
}

const getValueFromFormatString = ({
  value,
}: Extract<VariableAssignment['source'], { $case: 'formatString' }>): string => {
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
  { value }: Extract<VariableAssignment['source'], { $case: 'expression' }>,
  depth = 0
): string => {
  const { lhs, rhs, operation } = value
  if (!operation) return ''

  let symbol: string
  let useDotNotation = false

  if (operation.$case === 'mutationType') {
    const mt = operation.value
    symbol = getExpressionSymbol(mt)
    useDotNotation =
      mt === VariableMutationType.REMOVE_IF_PRESENT ||
      mt === VariableMutationType.REMOVE_INDEX ||
      mt === VariableMutationType.REMOVE_KEY ||
      mt === VariableMutationType.EXTEND
  } else {
    symbol = getComparatorSymbol(operation.value)
  }

  const result = useDotNotation
    ? `${getVariable(lhs!, depth + 1)}.${symbol}(${getVariable(rhs!, depth + 1)})`
    : `${getVariable(lhs!, depth + 1)} ${symbol} ${getVariable(rhs!, depth + 1)}`
  return depth > 0 ? `(${result})` : result
}
