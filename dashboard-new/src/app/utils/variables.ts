import { VariableAssignment } from "littlehorse-client/dist/proto/common_wfspec"
import { VariableValue } from "littlehorse-client/dist/proto/variable"

export const getVariable = (variable?: VariableAssignment) => {
  if (!variable) return
  if (variable.formatString) return getValueFromFormatString(variable)
  if (variable.variableName) {
    return getValueFromVariableName(variable)
  }
  if (variable.literalValue) return getValueFromLiteralValue(variable)
}

const getValueFromVariableName = ({
  variableName,
  jsonPath,
}: Pick<VariableAssignment, 'variableName' | 'jsonPath'>) => {
  if (jsonPath) return `${jsonPath} within ${variableName}`
  return variableName
}

const getValueFromLiteralValue = ({ literalValue }: Pick<VariableAssignment, 'literalValue'>) => {
  if (!literalValue) return
  const key = Object.keys(literalValue)[0] as keyof VariableValue
  return literalValue[key]
}

const getValueFromFormatString = ({ formatString }: Pick<VariableAssignment, 'formatString'>) => {
  if (!formatString) return
  return `${formatString.format}(${formatString.args})`
}
