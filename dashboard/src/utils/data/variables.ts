import { VariableAssignment, VariableDef, VariableType, VariableValue } from 'littlehorse-client/proto'

export function getVariable(variable?: VariableAssignment) {
  if (!variable) return
  if (variable.source?.$case === 'formatString') return getValueFromFormatString(variable.source)
  if (variable.source?.$case === 'variableName') {
    if (variable.jsonPath) {
      return `{${variable.jsonPath.replace('$', variable.source.variableName)}}`
    }
    return `{${variable.source.variableName}}`
  }
  if (variable.source?.$case === 'literalValue') return getVariableValue(variable.source.literalValue)
}

export function getVariableValue(variable?: VariableValue) {
  if (!variable) return

  if (variable.value?.$case === 'bytes') {
    return '[bytes]'
  } else if (variable.value) {
    const value = variable.value
    switch (value.$case) {
      case 'jsonObj':
        return value.jsonObj
      case 'jsonArr':
        return value.jsonArr
      case 'double':
        return value.double
      case 'bool':
        return value.bool
      case 'str':
        return value.str
      case 'int':
        return value.int
      default:
        return undefined
    }
  }
}

function getValueFromFormatString(
  source: VariableAssignment['source'] & { $case: 'formatString' }
): string | undefined {
  const template = getVariable(source.formatString.format)
  const args = source.formatString.args.map(getVariable)
  return `${template}`.replace(/{(\d+)}/g, (_, index) => `${args[index]}`)
}

export function getVariableTypeFromLiteralValue(literalValue: VariableValue) {
  const variableValueCase = literalValue.value?.$case
  if (variableValueCase === 'int') return 'int'
  if (variableValueCase === 'double') return 'double'
  if (variableValueCase === 'bool') return 'bool'
  if (variableValueCase === 'str') return 'str'
  if (variableValueCase === 'jsonObj') return 'jsonObj'
  if (variableValueCase === 'jsonArr') return 'jsonArr'
  if (variableValueCase === 'bytes') return 'bytes'
}

export function formatJsonOrReturnOriginalValue(value: string) {
  try {
    const json = JSON.parse(value)
    return JSON.stringify(json, null, 2)
  } catch {
    return value
  }
}

export function getTypedContent(contentType: string, contentValue: string) {
  switch (contentType) {
    case 'STR':
      return { str: contentValue }
    case 'INT':
      return { int: parseInt(contentValue) }
    case 'DOUBLE':
      return { double: parseFloat(contentValue) }
    case 'BOOL':
      return { bool: contentValue.toLowerCase() === 'true' }
    case 'JSON_OBJ':
      return { jsonObj: contentValue }
    case 'JSON_ARR':
      return { jsonArr: contentValue }
    case 'BYTES':
      return { bytes: Buffer.from(contentValue, 'utf8') }
    default:
      return { str: contentValue }
  }
}

/**
 * After 0.13.2, the `VariableDef.type` and `VariableDef.maskedValue` fields are deprecated.
 * These fields are replaced with `VariableDef.typeDef`.
 *
 * Old server versions may keep around both old and new Variables, so this function
 * determines which typing strategy a Variable uses.
 */
export const getVariableDefType = (varDef: VariableDef): VariableType => {
  if (varDef.typeDef) {
    return varDef.typeDef.type
  } else if (varDef.type) {
    return varDef.type
  }
  throw new Error('Variable must have type or typeDef.')
}

export const VARIABLE_TYPES: { [key in VariableType]: string } = {
  JSON_OBJ: 'JSON Object',
  JSON_ARR: 'JSON Array',
  DOUBLE: 'Double',
  BOOL: 'Boolean',
  STR: 'String',
  INT: 'Integer',
  BYTES: 'Bytes',
  WF_RUN_ID: 'Workflow Run ID',
  UNRECOGNIZED: 'Unrecognized',
}
