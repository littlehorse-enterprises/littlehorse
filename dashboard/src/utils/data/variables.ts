import { VariableValue } from "littlehorse-client/proto"

export function getVariableValue(variable?: VariableValue) {
    if (!variable) return
  
    const key = Object.keys(variable)[0] as keyof VariableValue
  
    if (variable.bytes) {
      return '[bytes]'
    } else {
      return variable[key]
    }
}