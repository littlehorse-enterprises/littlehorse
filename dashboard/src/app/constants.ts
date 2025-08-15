import { LHStatus, VariableValue } from 'littlehorse-client/proto'

export const SEARCH_LIMITS = [10, 20, 30, 60, 100] as const
export type SearchLimit = (typeof SEARCH_LIMITS)[number]
export const SEARCH_DEFAULT_LIMIT: SearchLimit = 10

export const VARIABLE_TYPES: { [key in NonNullable<VariableValue['value']>['$case']]: string } = {
  jsonObj: 'JSON Object',
  jsonArr: 'JSON Array',
  double: 'Double',
  bool: 'Boolean',
  str: 'String',
  int: 'Integer',
  bytes: 'Bytes',
  wfRunId: 'WfRunId',
}

export const SEARCH_ENTITIES = ['WfSpec', 'TaskDef', 'UserTaskDef', 'ExternalEventDef', 'WorkflowEventDef'] as const
export const WF_RUN_STATUSES = Object.values(LHStatus).filter(status => status !== 'UNRECOGNIZED')
export type SearchType = (typeof SEARCH_ENTITIES)[number]

export const TIME_RANGES = [-1, 5, 15, 30, 60, 180, 360, 720, 1440, 4320] as const
export type TimeRange = (typeof TIME_RANGES)[number]

export const TIME_RANGES_NAMES: { [key in TimeRange]: string } = {
  [-1]: 'All time',
  5: '5 minutes',
  15: '15 minutes',
  30: '30 minutes',
  60: '1 hour',
  180: '3 hours',
  360: '6 hours',
  720: '12 hours',
  1440: '1 day',
  4320: '3 days',
}

export const FUTURE_TIME_RANGES = [
  { label: 'All time', value: -1 },
  { label: 'Next 5 minutes', value: 5 },
  { label: 'Next 15 minutes', value: 15 },
  { label: 'Next 30 minutes', value: 30 },
  { label: 'Next 1 hour', value: 60 },
  { label: 'Next 3 hours', value: 180 },
  { label: 'Next 6 hours', value: 360 },
  { label: 'Next 12 hours', value: 720 },
  { label: 'Next 24 hours', value: 1440 },
  { label: 'Next 3 days', value: 4320 },
  { label: 'Next 7 days', value: 10080 },
] as const satisfies { label: string; value: number }[]
