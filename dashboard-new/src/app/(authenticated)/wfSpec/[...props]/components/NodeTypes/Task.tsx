import { Cog6ToothIcon } from '@heroicons/react/16/solid'
import { Node as NodeProto } from 'littlehorse-client/dist/proto/wf_spec'
import { FC, memo } from 'react'
import { Handle, NodeProps, Position } from 'reactflow'

const Node: FC<NodeProps<NodeProto>> = ({ selected, data }) => {
  return (
    <div
      className={
        'flex cursor-pointer flex-col items-center rounded-md border-[1px] border-orange-500 bg-orange-200 px-2 pt-1 text-xs' +
        (selected ? ' bg-orange-300 ' : '')
      }
    >
      <Cog6ToothIcon className="h-4 w-4 fill-orange-500" />
      {data.task?.taskDefId?.name}
      <Handle type="source" position={Position.Right} className="bg-transparent" />
      <Handle
        type="target"
        position={Position.Left}
        className="border-b-4 border-l-4 border-t-4 border-b-transparent border-l-gray-500 border-t-transparent bg-transparent"
      />
    </div>
  )
}

export const Task = memo(Node)
