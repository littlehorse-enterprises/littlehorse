import { ExclamationTriangleIcon, PlusIcon } from '@heroicons/react/16/solid'
import { WaitForThreadsNode, Node as NodeProto } from 'littlehorse-client/dist/proto/wf_spec'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeData } from '../extractNodes'
import { ClockIcon } from '@heroicons/react/24/outline'

const Node: FC<NodeData<Node>> = ({ data }) => {
  return (
    <div className="relative cursor-pointer">
      <div className="ml-1 flex h-10 w-10 items-center justify-center rounded-full border-[1px] border-gray-500 bg-gray-200">
        <ClockIcon className="h-4 w-4 fill-none stroke-gray-500" />
      </div>

      <Handle
        type="target"
        position={Position.Left}
        className="border-b-4 border-l-4 border-t-4 border-b-transparent border-l-gray-500 border-t-transparent bg-transparent"
      />
      <Handle type="source" position={Position.Right} className="bg-transparent" />
    </div>
  )
}

export const Sleep = memo(Node)
