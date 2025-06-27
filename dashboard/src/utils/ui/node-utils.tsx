import { LHStatus } from 'littlehorse-client/proto'
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
  Minus,
  PlayIcon,
  Timer,
  User,
} from 'lucide-react'

export type NodeType =
  | 'ENTRYPOINT'
  | 'EXIT'
  | 'NOP'
  | 'TASK'
  | 'EXTERNAL_EVENT'
  | 'SLEEP'
  | 'START_THREAD'
  | 'START_MULTIPLE_THREADS'
  | 'THROW_EVENT'
  | 'USER_TASK'
  | 'WAIT_FOR_CONDITION'
  | 'WAIT_FOR_THREADS'

export function getNodeType(nodeName: string): NodeType | undefined {
  if (nodeName.includes('ENTRYPOINT')) return 'ENTRYPOINT'
  if (nodeName.includes('EXIT')) return 'EXIT'
  if (nodeName.includes('EXTERNAL_EVENT')) return 'EXTERNAL_EVENT'
  if (nodeName.includes('NOP')) return 'NOP'
  if (nodeName.includes('TASK')) return 'TASK'
  if (nodeName.includes('SLEEP')) return 'SLEEP'
  if (nodeName.includes('START_THREAD')) return 'START_THREAD'
  if (nodeName.includes('START_MULTIPLE_THREADS')) return 'START_MULTIPLE_THREADS'
  if (nodeName.includes('THROW_EVENT')) return 'THROW_EVENT'
  if (nodeName.includes('USER_TASK')) return 'USER_TASK'
  if (nodeName.includes('WAIT_FOR_CONDITION')) return 'WAIT_FOR_CONDITION'
  if (nodeName.includes('WAIT_FOR_THREADS')) return 'WAIT_FOR_THREADS'
}

export function getIconColor(nodeType: NodeType): string {
  switch (nodeType) {
    case 'ENTRYPOINT':
      return 'text-green-600'
    case 'EXIT':
      return 'text-red-600'
    case 'EXTERNAL_EVENT':
      return 'text-purple-600'
    case 'NOP':
      return 'text-orange-600'
    case 'SLEEP':
      return 'text-indigo-600'
    case 'START_THREAD':
      return 'text-cyan-600'
    case 'START_MULTIPLE_THREADS':
      return 'text-cyan-600'
    case 'THROW_EVENT':
      return 'text-pink-600'
    case 'USER_TASK':
      return 'text-emerald-600'
    case 'WAIT_FOR_CONDITION':
      return 'text-amber-600'
    case 'WAIT_FOR_THREADS':
      return 'text-violet-600'
    default:
      return 'text-blue-600'
  }
}

export function getBorderColor(nodeType: NodeType): string {
  switch (nodeType) {
    case 'ENTRYPOINT':
      return 'border-green-200'
    case 'EXIT':
      return 'border-red-200'
    case 'EXTERNAL_EVENT':
      return 'border-purple-200'
    case 'NOP':
      return 'border-orange-200'
    case 'SLEEP':
      return 'border-indigo-200'
    case 'START_THREAD':
      return 'border-cyan-200'
    case 'START_MULTIPLE_THREADS':
      return 'border-cyan-200'
    case 'THROW_EVENT':
      return 'border-pink-200'
    case 'USER_TASK':
      return 'border-emerald-200'
    case 'WAIT_FOR_CONDITION':
      return 'border-amber-200'
    case 'WAIT_FOR_THREADS':
      return 'border-violet-200'
    default:
      return 'border-blue-200'
  }
}

export function getBackgroundColor(nodeType: NodeType): string {
  switch (nodeType) {
    case 'ENTRYPOINT':
      return 'bg-green-100'
    case 'EXIT':
      return 'bg-red-100'
    case 'EXTERNAL_EVENT':
      return 'bg-purple-100'
    case 'NOP':
      return 'bg-orange-100'
    case 'SLEEP':
      return 'bg-indigo-100'
    case 'START_THREAD':
      return 'bg-cyan-100'
    case 'START_MULTIPLE_THREADS':
      return 'bg-cyan-100'
    case 'THROW_EVENT':
      return 'bg-pink-100'
    case 'USER_TASK':
      return 'bg-emerald-100'
    case 'WAIT_FOR_CONDITION':
      return 'bg-amber-100'
    case 'WAIT_FOR_THREADS':
      return 'bg-violet-100'
    default:
      return 'bg-blue-100'
  }
}

export function getTextColor(nodeType: NodeType): string {
  switch (nodeType) {
    case 'ENTRYPOINT':
      return 'text-green-700'
    case 'EXIT':
      return 'text-red-700'
    case 'EXTERNAL_EVENT':
      return 'text-purple-700'
    case 'NOP':
      return 'text-orange-700'
    case 'SLEEP':
      return 'text-indigo-700'
    case 'START_THREAD':
      return 'text-cyan-700'
    case 'START_MULTIPLE_THREADS':
      return 'text-cyan-700'
    case 'THROW_EVENT':
      return 'text-pink-700'
    case 'USER_TASK':
      return 'text-emerald-700'
    case 'WAIT_FOR_CONDITION':
      return 'text-amber-700'
    case 'WAIT_FOR_THREADS':
      return 'text-violet-700'
    default:
      return 'text-blue-700'
  }
}

export function getNodeIcon(nodeType: NodeType) {
  const iconClass = `${getIconColor(nodeType)}`

  switch (nodeType) {
    case 'EXTERNAL_EVENT':
      return <MailIcon className={iconClass} />
    case 'ENTRYPOINT':
      return <PlayIcon className={iconClass} />
    case 'EXIT':
      return <CircleSlashIcon className={iconClass} />
    case 'TASK':
      return <Box className={iconClass} />
    case 'NOP':
      return <Minus className={iconClass} />
    case 'SLEEP':
      return <Timer className={iconClass} />
    case 'START_THREAD':
      return <GitBranch className={iconClass} />
    case 'START_MULTIPLE_THREADS':
      return <GitBranch className={iconClass} />
    case 'THROW_EVENT':
      return <Bell className={iconClass} />
    case 'USER_TASK':
      return <User className={iconClass} />
    case 'WAIT_FOR_CONDITION':
      return <Clock className={iconClass} />
    case 'WAIT_FOR_THREADS':
      return <GitBranch className={iconClass} />
    default:
      return null
  }
}

export function getStatusBgColor(status?: LHStatus): string {
  switch (status) {
    case LHStatus.COMPLETED:
      return 'bg-green-100'
    case LHStatus.ERROR:
    case LHStatus.EXCEPTION:
      return 'bg-red-100'
    case LHStatus.RUNNING:
    case LHStatus.STARTING:
    case LHStatus.HALTING:
      return 'bg-blue-100'
    case LHStatus.HALTED:
      return 'bg-gray-100'
    default:
      return 'bg-gray-100'
  }
}

export function getStatusIcon(status?: LHStatus) {
  switch (status) {
    case LHStatus.COMPLETED:
      return <CheckCircle className="h-4 w-4 text-green-500" />
    case LHStatus.ERROR:
    case LHStatus.EXCEPTION:
      return <AlertCircle className="h-4 w-4 text-red-500" />
    case LHStatus.RUNNING:
    case LHStatus.STARTING:
    case LHStatus.HALTING:
      return <Loader2 className="h-4 w-4 animate-spin text-blue-500" />
    case LHStatus.HALTED:
      return <Clock className="h-4 w-4 text-gray-500" />
    default:
      return <Clock className="h-4 w-4 text-gray-500" />
  }
}
