import { FilterOption } from '@/components/ui/dropdown-filter'
import { Comparator, LHStatus, WfRunVariableAccessLevel } from 'littlehorse-client/proto'
import {
  AlertCircle,
  Bell,
  Box,
  CheckCircle,
  CircleSlashIcon,
  Clock,
  GitBranch,
  Loader2,
  MailIcon,
  MinusIcon,
  PlayIcon,
  Timer,
  User,
  XCircle,
} from 'lucide-react'

/* --------------------------------- Search --------------------------------- */
export const SEARCH_LIMITS = [10, 20, 30, 60, 100] as const

export const SEARCH_LIMIT_DEFAULT: (typeof SEARCH_LIMITS)[number] = 20
export const SEARCH_ENTITIES = ['WfSpec', 'TaskDef', 'UserTaskDef', 'ExternalEventDef', 'WorkflowEventDef'] as const
export const TIME_RANGES = [
  { value: '5m', label: 'Last 5 minutes', minutes: 5 },
  { value: '15m', label: 'Last 15 minutes', minutes: 15 },
  { value: '30m', label: 'Last 30 minutes', minutes: 30 },
  { value: '1h', label: 'Last 1 hour', minutes: 60 },
  { value: '2h', label: 'Last 2 hours', minutes: 120 },
  { value: '4h', label: 'Last 4 hours', minutes: 240 },
  { value: '6h', label: 'Last 6 hours', minutes: 360 },
  { value: '12h', label: 'Last 12 hours', minutes: 720 },
  { value: '1d', label: 'Last 1 day', minutes: 1440 },
  { value: '2d', label: 'Last 2 days', minutes: 2880 },
  { value: '3d', label: 'Last 3 days', minutes: 4320 },
  { value: '7d', label: 'Last 7 days', minutes: 10080 },
  { value: '14d', label: 'Last 14 days', minutes: 20160 },
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

/* --------------------------------- Node Styles --------------------------------- */
export const NODE_STYLES = {
  entrypoint: {
    iconColor: 'text-green-600',
    borderColor: 'border-green-200',
    icon: PlayIcon,
  },
  exit: {
    iconColor: 'text-red-600',
    borderColor: 'border-red-200',
    icon: CircleSlashIcon,
  },
  externalEvent: {
    iconColor: 'text-purple-600',
    borderColor: 'border-purple-200',
    icon: MailIcon,
  },
  nop: {
    iconColor: 'text-orange-600',
    borderColor: 'border-orange-200',
    icon: MinusIcon,
  },
  sleep: {
    iconColor: 'text-indigo-600',
    borderColor: 'border-indigo-200',
    icon: Timer,
  },
  startThread: {
    iconColor: 'text-cyan-600',
    borderColor: 'border-cyan-200',
    icon: GitBranch,
  },
  startMultipleThreads: {
    iconColor: 'text-cyan-600',
    borderColor: 'border-cyan-200',
    icon: GitBranch,
  },
  throwEvent: {
    iconColor: 'text-pink-600',
    borderColor: 'border-pink-200',
    icon: Bell,
  },
  userTask: {
    iconColor: 'text-emerald-600',
    borderColor: 'border-emerald-200',
    icon: User,
  },
  waitForCondition: {
    iconColor: 'text-amber-600',
    borderColor: 'border-amber-200',
    icon: Clock,
  },
  waitForThreads: {
    iconColor: 'text-violet-600',
    borderColor: 'border-violet-200',
    icon: GitBranch,
  },
  task: {
    iconColor: 'text-blue-600',
    borderColor: 'border-blue-200',
    icon: Box,
  },
} as const

export const accessLevelLabels: { [key in WfRunVariableAccessLevel]: string } = {
  PUBLIC_VAR: 'Public',
  INHERITED_VAR: 'Inherited',
  PRIVATE_VAR: 'Private',
  UNRECOGNIZED: '',
}

export const Conditions: Record<Comparator, string> = {
  [Comparator.LESS_THAN]: '<',
  [Comparator.GREATER_THAN]: '>',
  [Comparator.LESS_THAN_EQ]: '<=',
  [Comparator.GREATER_THAN_EQ]: '>=',
  [Comparator.EQUALS]: '==',
  [Comparator.NOT_EQUALS]: '!=',
  [Comparator.IN]: 'IN',
  [Comparator.NOT_IN]: 'NOT IN',
  [Comparator.UNRECOGNIZED]: '',
}
