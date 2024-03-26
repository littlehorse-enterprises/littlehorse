import { LHStatus, VariableType } from 'littlehorse-client/dist/proto/common_enums'

export const SEARCH_LIMITS = [10, 20, 30, 60, 100] as const
export type SearchLimit = (typeof SEARCH_LIMITS)[number]
export const SEARCH_DEFAULT_LIMIT: SearchLimit = 10

export const VARIABLE_TYPES: { [key in VariableType]: string } = {
  JSON_OBJ: 'JSON Object',
  JSON_ARR: 'JSON Array',
  DOUBLE: 'Double',
  BOOL: 'Boolean',
  STR: 'String',
  INT: 'Integer',
  BYTES: 'Bytes',
  UNRECOGNIZED: 'Unrecognized',
}

export const SEARCH_ENTITIES = ['WfSpec', 'TaskDef', 'UserTaskDef', 'ExternalEventDef'] as const
export const WF_RUN_STATUSES = Object.values(LHStatus).filter(status => status !== 'UNRECOGNIZED')
export type SearchType = (typeof SEARCH_ENTITIES)[number]

const toTime = (minutes: number) => {
  return minutes * 60
}

export const TIME_RANGES = [5, 15, 30, 60, 180, 360, 720, 1440] as const
