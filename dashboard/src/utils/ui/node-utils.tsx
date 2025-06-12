import { Box, CheckCircle, AlertCircle, Loader2, PlayIcon, MailIcon, CircleSlashIcon } from 'lucide-react'

export type NodeType = 'ENTRYPOINT' | 'EXIT' | 'TASK' | 'EXTERNAL_EVENT' | 'DECISION'

export function getNodeType(nodeName: string): NodeType {
  if (nodeName.startsWith('ENTRYPOINT')) return 'ENTRYPOINT'
  if (nodeName.startsWith('EXIT')) return 'EXIT'
  if (nodeName.includes('EXTERNAL_EVENT')) return 'EXTERNAL_EVENT'
  if (nodeName.includes('DECISION')) return 'DECISION'
  return 'TASK'
}

export function getIconColor(nodeType: NodeType): string {
  switch (nodeType) {
    case 'ENTRYPOINT':
      return 'text-green-600'
    case 'EXIT':
      return 'text-red-600'
    case 'EXTERNAL_EVENT':
      return 'text-purple-600'
    case 'DECISION':
      return 'text-orange-600'
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
    case 'DECISION':
      return 'border-orange-200'
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
    case 'DECISION':
      return 'bg-orange-100'
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
    case 'DECISION':
      return 'text-orange-700'
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
    default:
      return null
  }
}

export function getStatusBgColor(status?: string): string {
  switch (status) {
    case 'COMPLETED':
      return 'bg-green-100'
    case 'ERROR':
      return 'bg-red-100'
    case 'RUNNING':
      return 'bg-blue-100'
    default:
      return 'bg-gray-100'
  }
}

export function getStatusIcon(status?: string) {
  switch (status) {
    case 'COMPLETED':
      return <CheckCircle className="h-4 w-4 text-green-500" />
    case 'ERROR':
      return <AlertCircle className="h-4 w-4 text-red-500" />
    case 'RUNNING':
      return <Loader2 className="h-4 w-4 animate-spin text-blue-500" />
    default:
      return null
  }
}
