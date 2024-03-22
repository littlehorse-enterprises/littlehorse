import { VariableType } from 'littlehorse-client/dist/proto/common_enums'

export const SEARCH_LIMITS = [10, 20, 30, 60, 100] as const
export const SEARCH_DEFAULT_LIMIT: (typeof SEARCH_LIMITS)[number] = 10

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
export type SearchType = (typeof SEARCH_ENTITIES)[number]
