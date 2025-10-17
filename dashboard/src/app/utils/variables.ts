import {
  LHPath,
  LHPath_Selector,
  TypeDefinition,
  VariableAssignment,
  VariableDef,
  VariableMutationType,
  VariableType,
  VariableValue,
} from 'littlehorse-client/proto'
import { structFromJSONString, structToJSONString } from './struct'
import { flattenWfRunId, wfRunIdFromFlattenedId } from './wfRun'
import { lhPathToString } from './lhPath'

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
      return structToJSONString(value.value)
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
  const variable =
    type === 'jsonObj'
      ? { jsonObj: JSON.stringify(JSON.parse(value)) }
      : type === 'jsonArr'
        ? { jsonArr: JSON.stringify(JSON.parse(value)) }
        : type === 'double'
          ? { double: parseFloat(value) }
          : type === 'bool'
            ? { bool: value.toLowerCase() === 'true' }
            : type === 'str'
              ? { str: value }
              : type === 'int'
                ? { int: parseInt(value, 10) }
                : type === 'bytes'
                  ? { bytes: Buffer.from(value) }
                  : type === 'wfRunId'
                    ? { wfRunId: wfRunIdFromFlattenedId(value) }
                    : type === 'struct'
                      ? { struct: structFromJSONString(value) }
                      : type === 'utcTimestamp'
                        ? { utcTimestamp: new Date(value) }
                        : undefined
  return VariableValue.fromJSON(variable)
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

  switch (path?.$case) {
    case 'jsonPath':
      return path.value
    case 'lhPath':
      return lhPathToString(path.value)
    default:
      return `{${value}}`
  }
}

const getValueFromFormatString = ({
  value,
}: Extract<VariableAssignment['source'], { $case: 'formatString' }>): string => {
  const template = getVariable(value.format!)
  const args = value.args.map(getVariable)

  return `${template}`.replace(/{(\d+)}/g, (_, index) => `${args[index]}`)
}

const getExpressionSymbol = (expression: VariableMutationType): String => {
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
  const result =
    operation === VariableMutationType.REMOVE_IF_PRESENT ||
    operation === VariableMutationType.REMOVE_INDEX ||
    operation === VariableMutationType.REMOVE_KEY ||
    operation === VariableMutationType.EXTEND
      ? `${getVariable(lhs!, depth + 1)}.${getExpressionSymbol(operation)}(${getVariable(rhs!, depth + 1)})` // Dot notation for these operations
      : `${getVariable(lhs!, depth + 1)} ${getExpressionSymbol(operation)} ${getVariable(rhs!, depth + 1)}` // Arithmetic operations
  return depth > 0 ? `(${result})` : result
}
