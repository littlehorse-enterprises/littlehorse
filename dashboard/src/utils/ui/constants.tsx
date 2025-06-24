import { FilterOption } from '@/components/ui/dropdown-filter'
import { LHStatus } from 'littlehorse-client/proto'
import { AlertCircle, CheckCircle, Clock, Loader2, XCircle } from 'lucide-react'

/* --------------------------------- Search --------------------------------- */
export const SEARCH_LIMITS = [10, 20, 30, 60, 100] as const

export const SEARCH_LIMIT_DEFAULT: (typeof SEARCH_LIMITS)[number] = 10
export const SEARCH_ENTITIES = [
  'TaskRun',
  'WfSpec',
  'TaskDef',
  'UserTaskDef',
  'ExternalEventDef',
  'WorkflowEventDef',
] as const
export const TIME_RANGES = [
  { value: '30d', label: 'Last 30 days', minutes: 43200 },
  { value: '7d', label: 'Last 7 days', minutes: 10080 },
  { value: '1d', label: 'Last 1 day', minutes: 1440 },
] as const

// Convert TIME_RANGES to FilterOption[] format
export const TIME_RANGE_OPTIONS: FilterOption[] = TIME_RANGES.map(({ value, label }) => ({
  value,
  label,
}))

// Map time range values to minutes
export const TIME_RANGE_MINUTES: Record<string, number | null> = TIME_RANGES.reduce(
  (acc, { value, minutes }) => {
    acc[value] = minutes
    return acc
  },
  {} as Record<string, number | null>
)

// Map minutes to time range values
export const MINUTES_TO_TIME_RANGE: Record<number | string, string> = Object.entries(TIME_RANGE_MINUTES).reduce(
  (acc, [key, value]) => {
    if (value !== null) {
      acc[value] = key
    } else {
      acc['null'] = key
    }
    return acc
  },
  {} as Record<string, string>
)

export const STATUS_OPTIONS: FilterOption[] = [
  {
    value: LHStatus.COMPLETED,
    label: 'Completed',
    icon: <CheckCircle className="h-3 w-3 text-green-500" />,
  },
  {
    value: LHStatus.ERROR,
    label: 'Error',
    icon: <XCircle className="h-3 w-3 text-red-500" />,
  },
  {
    value: LHStatus.EXCEPTION,
    label: 'Exception',
    icon: <AlertCircle className="h-3 w-3 text-red-500" />,
  },
  {
    value: LHStatus.HALTED,
    label: 'Halted',
    icon: <Clock className="h-3 w-3 text-[#656565]" />,
  },
  {
    value: LHStatus.HALTING,
    label: 'Halting',
    icon: <Loader2 className="h-3 w-3 animate-spin text-blue-500" />,
  },
  {
    value: LHStatus.RUNNING,
    label: 'Running',
    icon: <Loader2 className="h-3 w-3 animate-spin text-blue-500" />,
  },
  {
    value: LHStatus.STARTING,
    label: 'Starting',
    icon: <Loader2 className="h-3 w-3 text-blue-500" />,
  },
]
