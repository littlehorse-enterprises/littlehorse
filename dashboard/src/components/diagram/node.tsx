import { NodeType, getBorderColor, getNodeIcon, getStatusBgColor, getStatusIcon } from '@/utils/ui/node-utils'
import { Handle, Position } from '@xyflow/react'
import { LHStatus } from 'littlehorse-client/proto'
import { memo } from 'react'

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

function Node({ data, selected }: NodeProps) {
  return (
    <div className="flex flex-col items-center">
      <div className="mb-1 text-[10px] font-medium text-gray-400 uppercase">{data.type}</div>

      <div className="relative">
        {selected && (
          <div className="absolute inset-0 -m-[5.25px] rounded-md border-2 border-double border-blue-500"></div>
        )}

        <div
          className={`flex items-center justify-center border bg-white ${getBorderColor(data.type)} h-12 w-12 rounded-md shadow-sm ${
            selected ? 'shadow-md' : ''
          }`}
        >
          {data.status && (
            <div className="absolute top-0 right-0 -mt-1 -mr-1">
              <div
                className={`flex items-center justify-center rounded-full p-0.5 ${getStatusBgColor(data.status)} border border-white shadow-sm`}
              >
                {getStatusIcon(data.status)}
              </div>
            </div>
          )}

          <div>{getNodeIcon(data.type)}</div>
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
