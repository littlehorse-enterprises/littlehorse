import { NodeType } from '@/types'
import { Handle, Position } from '@xyflow/react'
import { LHStatus } from 'littlehorse-client/proto'
import { AlertCircle, CheckCircle, Clock, Loader2 } from 'lucide-react'
import { memo } from 'react'
import { NODE_STYLES } from '@/constants'

export type NodeData = {
  label: string
  type: NodeType
  status?: LHStatus
  nodeName?: string
}

interface NodeProps {
  data: NodeData
  selected?: boolean
}

// Status style configuration
const STATUS_STYLES: Record<LHStatus, {
  bgColor: string
  icon: React.ComponentType<{ className?: string }>
  iconColor: string
  animate?: string
}> = {
  [LHStatus.COMPLETED]: {
    bgColor: 'bg-green-100',
    icon: CheckCircle,
    iconColor: 'text-green-500'
  },
  [LHStatus.ERROR]: {
    bgColor: 'bg-red-100',
    icon: AlertCircle,
    iconColor: 'text-red-500'
  },
  [LHStatus.EXCEPTION]: {
    bgColor: 'bg-red-100',
    icon: AlertCircle,
    iconColor: 'text-red-500'
  },
  [LHStatus.RUNNING]: {
    bgColor: 'bg-blue-100',
    icon: Loader2,
    iconColor: 'text-blue-500',
    animate: 'animate-spin'
  },
  [LHStatus.STARTING]: {
    bgColor: 'bg-blue-100',
    icon: Loader2,
    iconColor: 'text-blue-500',
    animate: 'animate-spin'
  },
  [LHStatus.HALTING]: {
    bgColor: 'bg-blue-100',
    icon: Loader2,
    iconColor: 'text-blue-500',
    animate: 'animate-spin'
  },
  [LHStatus.HALTED]: {
    bgColor: 'bg-gray-100',
    icon: Clock,
    iconColor: 'text-gray-500'
  },
  [LHStatus.UNRECOGNIZED]: {
    bgColor: 'bg-gray-100',
    icon: Clock,
    iconColor: 'text-gray-500'
  }
}

function Node({ data, selected }: NodeProps) {
  // Extract styling variables
  const nodeStyle = NODE_STYLES[data.type] || NODE_STYLES.task
  const NodeIcon = nodeStyle.icon
  const borderColor = nodeStyle.borderColor

  const statusStyle = data.status ? STATUS_STYLES[data.status] : null
  const StatusIcon = statusStyle?.icon
  const statusIconClasses = statusStyle ? `h-4 w-4 ${statusStyle.iconColor} ${statusStyle.animate || ''}` : ''
  const statusIcon = StatusIcon ? <StatusIcon className={statusIconClasses} /> : null

  return (
    <div className="flex flex-col items-center">
      <div className="mb-1 text-[10px] font-medium text-gray-400 uppercase">{data.type}</div>

      <div className="relative">
        {selected && (
          <div className="absolute inset-0 -m-[5.25px] rounded-md border-2 border-double border-blue-500"></div>
        )}

        <div
          className={`flex items-center justify-center border bg-white ${borderColor} h-12 w-12 rounded-md shadow-sm ${selected ? 'shadow-md' : ''
            }`}
        >
          {data.status && (
            <div className="absolute top-0 right-0 -mt-1 -mr-1">
              <div
                className={`flex items-center justify-center rounded-full p-0.5 ${STATUS_STYLES[data.status].bgColor} border border-white shadow-sm`}
              >
                {statusIcon}
              </div>
            </div>
          )}

          <div>
            <NodeIcon className={nodeStyle.iconColor} />
          </div>
        </div>

        <Handle type="target" position={Position.Left} className="!bg-gray-400" />
        <Handle type="source" position={Position.Right} className="!bg-gray-400" />
      </div>

      {data.label && (
        <div className="absolute top-full left-1/2 mt-1 -translate-x-1/2 transform text-center text-xs font-medium whitespace-nowrap text-gray-700">
          {data.label.match(/^[^-]*-(.+)-[^-]*$/)?.[1] || data.label}
        </div>
      )}
    </div>
  )
}



export default memo(Node)
