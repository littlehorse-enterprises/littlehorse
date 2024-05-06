import { VariableAssignment } from 'littlehorse-client/dist/proto/common_wfspec'
import { VariableValue } from 'littlehorse-client/dist/proto/variable'

export const getVariable = (variable?: VariableAssignment) => {
  if (!variable) return
  if (variable.formatString) return getValueFromFormatString(variable)
  if (variable.variableName) {
    return getValueFromVariableName(variable)
  }
  if (variable.literalValue) return getVariableValue(variable.literalValue)
}

export const getVariableValue = (variable?: VariableValue) => {
  if (!variable) return
  const key = Object.keys(variable)[0] as keyof VariableValue
  return variable[key]
}

const getValueFromVariableName = ({
  variableName,
  jsonPath,
}: Pick<VariableAssignment, 'variableName' | 'jsonPath'>) => {
  if (!variableName) return
  if (jsonPath) return `{${jsonPath.replace('$', variableName)}}`
  return `{${variableName}}`
}

const getValueFromFormatString = ({ formatString }: Pick<VariableAssignment, 'formatString'>): string | undefined => {
  if (!formatString) return
  const template = getVariable(formatString.format)
  const args = formatString.args.map(getVariable)

  return `${template}`.replace(/{(\d+)}/g, (_, index) => `${args[index]}`)
}
