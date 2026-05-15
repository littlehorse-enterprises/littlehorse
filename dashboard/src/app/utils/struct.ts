import { Array, Struct, TaskAttempt, VariableValue } from 'littlehorse-client/proto'
import { getVariableValue, variableValueToJSON } from './variables'

export const structToJSONString = (struct: Struct): string => {
  return JSON.stringify(variableValueToJSON(VariableValue.fromJSON({ struct })))
}

export const structFromJSONString = (jsonStr: string): Struct => {
  return Struct.fromJSON(jsonStr)
}

export const arrayToJSONString = (array: Array): string => {
  return JSON.stringify(variableValueToJSON(VariableValue.fromJSON({ array })))
}

export const arrayFromJSONString = (jsonStr: string): Array => {
  return Array.fromJSON(jsonStr)
}

export const getAttemptOutput = (output: VariableValue | undefined): string => {
  if (!output?.value?.value) return 'No Output'
  return getVariableValue(output)
}

export const getAttemptResult = (result: Partial<TaskAttempt>['result']): string => {
  if (!result) return 'No Output'

  if (result.$case === 'output') {
    return getAttemptOutput(result.value)
  }

  const val = result.value
  return (val && (val.message ?? val.toString())) || JSON.stringify(val) || 'No Output'
}
