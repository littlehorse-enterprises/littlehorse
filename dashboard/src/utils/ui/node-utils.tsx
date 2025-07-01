import { OneOfCases } from '@/types/oneof'
import { Node as LHNode, LHStatus } from 'littlehorse-client/proto'
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

export type NodeType = OneOfCases<LHNode['node']>

export function getIconColor(nodeType: NodeType): string {
  switch (nodeType) {
    case 'entrypoint':
      return 'text-green-600'
    case 'exit':
      return 'text-red-600'
    case 'externalEvent':
      return 'text-purple-600'
    case 'nop':
      return 'text-orange-600'
    case 'sleep':
      return 'text-indigo-600'
    case 'startThread':
      return 'text-cyan-600'
    case 'startMultipleThreads':
      return 'text-cyan-600'
    case 'throwEvent':
      return 'text-pink-600'
    case 'userTask':
      return 'text-emerald-600'
    case 'waitForCondition':
      return 'text-amber-600'
    case 'waitForThreads':
      return 'text-violet-600'
    default:
      return 'text-blue-600'
  }
}

export function getBorderColor(nodeType: NodeType): string {
  switch (nodeType) {
    case 'entrypoint':
      return 'border-green-200'
    case 'exit':
      return 'border-red-200'
    case 'externalEvent':
      return 'border-purple-200'
    case 'nop':
      return 'border-orange-200'
    case 'sleep':
      return 'border-indigo-200'
    case 'startThread':
      return 'border-cyan-200'
    case 'startMultipleThreads':
      return 'border-cyan-200'
    case 'throwEvent':
      return 'border-pink-200'
    case 'userTask':
      return 'border-emerald-200'
    case 'waitForCondition':
      return 'border-amber-200'
    case 'waitForThreads':
      return 'border-violet-200'
    default:
      return 'border-blue-200'
  }
}

export function getBackgroundColor(nodeType: NodeType): string {
  switch (nodeType) {
    case 'entrypoint':
      return 'bg-green-100'
    case 'exit':
      return 'bg-red-100'
    case 'externalEvent':
      return 'bg-purple-100'
    case 'nop':
      return 'bg-orange-100'
    case 'sleep':
      return 'bg-indigo-100'
    case 'startThread':
      return 'bg-cyan-100'
    case 'startMultipleThreads':
      return 'bg-cyan-100'
    case 'throwEvent':
      return 'bg-pink-100'
    case 'userTask':
      return 'bg-emerald-100'
    case 'waitForCondition':
      return 'bg-amber-100'
    case 'waitForThreads':
      return 'bg-violet-100'
    default:
      return 'bg-blue-100'
  }
}

export function getTextColor(nodeType: NodeType): string {
  switch (nodeType) {
    case 'entrypoint':
      return 'text-green-700'
    case 'exit':
      return 'text-red-700'
    case 'externalEvent':
      return 'text-purple-700'
    case 'nop':
      return 'text-orange-700'
    case 'sleep':
      return 'text-indigo-700'
    case 'startThread':
      return 'text-cyan-700'
    case 'startMultipleThreads':
      return 'text-cyan-700'
    case 'throwEvent':
      return 'text-pink-700'
    case 'userTask':
      return 'text-emerald-700'
    case 'waitForCondition':
      return 'text-amber-700'
    case 'waitForThreads':
      return 'text-violet-700'
    default:
      return 'text-blue-700'
  }
}

export function getNodeIcon(nodeType: NodeType) {
  const iconClass = `${getIconColor(nodeType)}`

  switch (nodeType) {
    case 'externalEvent':
      return <MailIcon className={iconClass} />
    case 'entrypoint':
      return <PlayIcon className={iconClass} />
    case 'exit':
      return <CircleSlashIcon className={iconClass} />
    case 'task':
      return <Box className={iconClass} />
    case 'nop':
      return <Minus className={iconClass} />
    case 'sleep':
      return <Timer className={iconClass} />
    case 'startThread':
      return <GitBranch className={iconClass} />
    case 'startMultipleThreads':
      return <GitBranch className={iconClass} />
    case 'throwEvent':
      return <Bell className={iconClass} />
    case 'userTask':
      return <User className={iconClass} />
    case 'waitForCondition':
      return <Clock className={iconClass} />
    case 'waitForThreads':
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
