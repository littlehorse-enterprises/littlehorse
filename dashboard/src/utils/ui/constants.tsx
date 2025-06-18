import { CheckCircle, XCircle, Loader2, Clock } from 'lucide-react'
import { FilterOption } from '@/components/ui/dropdown-filter'

/* --------------------------------- Search --------------------------------- */
export const SEARCH_LIMITS = [10, 20, 30, 60, 100] as const

export const SEARCH_LIMIT_DEFAULT: (typeof SEARCH_LIMITS)[number] = 10
export const SEARCH_ENTITIES = ['WfSpec', 'TaskDef', 'UserTaskDef', 'ExternalEventDef', 'WorkflowEventDef'] as const

/* ------------------------------- Workflow Runs ------------------------------- */
export const TIME_RANGES = [
  { value: 'all', label: 'All time', minutes: null },
  { value: '1h', label: 'Last hour', minutes: 60 },
  { value: '24h', label: 'Last 24 hours', minutes: 1440 },
  { value: '7d', label: 'Last 7 days', minutes: 10080 },
  { value: '30d', label: 'Last 30 days', minutes: 43200 },
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
    value: 'COMPLETED',
    label: 'Completed',
    icon: <CheckCircle className="h-3 w-3 text-green-500" />,
  },
  {
    value: 'FAILED',
    label: 'Failed',
    icon: <XCircle className="h-3 w-3 text-red-500" />,
  },
  {
    value: 'RUNNING',
    label: 'Running',
    icon: <Loader2 className="h-3 w-3 text-blue-500" />,
  },
  {
    value: 'PENDING',
    label: 'Pending',
    icon: <Clock className="h-3 w-3 text-[#656565]" />,
  },
]
