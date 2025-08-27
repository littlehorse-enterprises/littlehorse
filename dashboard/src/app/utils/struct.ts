import { Struct } from 'littlehorse-client/proto'
import { getVariableValue } from './variables'

export const structToJSONString = (struct: Struct): string => {
  return JSON.stringify(structToJSONObject(struct))
}

const structToJSONObject = (struct: Struct): Object => {
  // TODO: Frontend team should refactor this as they see fit
  let structObject: { [key: string]: Object } = {}

  if (struct.struct == null) return '{}'

  for (const entry of Object.entries(struct.struct.fields)) {
    if (entry[1].value?.value?.$case == 'struct') {
      structObject[entry[0]] = structToJSONObject(entry[1].value?.value?.value)
    } else if (entry[1].value) {
      structObject[entry[0]] = getVariableValue(entry[1].value)
    }
  }
  return structObject
}

export const structFromJSONString = (jsonStr: string): Struct => {
  return Struct.fromJSON(jsonStr)
}
