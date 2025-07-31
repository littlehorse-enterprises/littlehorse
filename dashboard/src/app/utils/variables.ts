import {
  VariableAssignment,
  VariableDef,
  VariableMutationType,
  VariableType,
  VariableValue,
} from 'littlehorse-client/proto'
import { flattenWfRunId, wfRunIdFromFlattenedId } from './wfRun'

export const getVariable = (variable: VariableAssignment, depth = 0): string => {
  if (!variable || !variable.source) return ''

  switch (variable.source.$case) {
    case 'expression':
      return formatVariableExpression(variable.source, depth)
    case 'formatString':
      return getValueFromFormatString(variable.source)
    case 'literalValue':
      if (Object.keys(variable.source.value).length === 0) return 'null'
      return getVariableValue(variable.source.value)
    case 'nodeOutput':
      return variable.source.value.nodeName
    case 'variableName':
      return getValueFromVariableName(variable.source, variable.jsonPath)
    default:
      return ''
  }
}

export const getVariableValue = ({ value }: VariableValue): string => {
  if (!value) return ''

  switch (value.$case) {
    case 'bytes':
      return '[bytes]'
    case 'wfRunId':
      return flattenWfRunId(value.value)
    default:
      return value.value.toString()
  }
}

const getValueFromVariableName = (
  { value }: Extract<VariableAssignment['source'], { $case: 'variableName' }>,
  jsonPath?: string
): string => {
  if (!value) return ''
  if (jsonPath) return `{${jsonPath.replace('$', value)}}`
  return `{${value}}`
}

const getValueFromFormatString = ({
  value,
}: Extract<VariableAssignment['source'], { $case: 'formatString' }>): string => {
  const template = getVariable(value.format!)
  const args = value.args.map(getVariable)

  return `${template}`.replace(/{(\d+)}/g, (_, index) => `${args[index]}`)
}

export const formatJsonOrReturnOriginalValue = (value: string) => {
  try {
    const json = JSON.parse(value)
    return JSON.stringify(json, null, 2)
  } catch {
    return value
  }
}

export const getTypedVariableValue = (
  type: NonNullable<VariableValue['value']>['$case'],
  value: string
): VariableValue => {
  const variable =
    type === 'jsonObj'
      ? { jsonObj: JSON.parse(value) }
      : type === 'jsonArr'
        ? { jsonArr: JSON.parse(value) }
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
                    : undefined
  return VariableValue.fromJSON(variable)
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

/**
 * After 0.13.2, the `VariableDef.type` and `VariableDef.maskedValue` fields are deprecated.
 * These fields are replaced with `VariableDef.typeDef`.
 *
 * Old server versions may keep around both old and new Variables, so this function
 * determines which typing strategy a Variable uses.
 */
export const getVariableDefType = (varDef: VariableDef): NonNullable<VariableValue['value']>['$case'] => {
  if (varDef.typeDef) {
    return getVariableCaseFromType(varDef.typeDef.type)
  } else if (varDef.type) {
    return getVariableCaseFromType(varDef.type)
  }
  throw new Error('Variable must have type or typeDef.')
}

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
    default:
      throw new Error(`Unknown variable type: ${type}`)
  }
}
