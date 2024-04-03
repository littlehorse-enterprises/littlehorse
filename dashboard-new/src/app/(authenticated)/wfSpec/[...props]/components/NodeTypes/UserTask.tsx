import { UserIcon } from '@heroicons/react/16/solid'
import { Node as NodeProto } from 'littlehorse-client/dist/proto/wf_spec'
import { FC, memo } from 'react'
import { Handle, NodeProps, Position } from 'reactflow'

const Node: FC<NodeProps<NodeProto>> = ({ data, selected }) => {
  return (
    <div
      className={
        'flex cursor-pointer flex-col items-center rounded-md border-[1px] border-blue-500 bg-blue-200 px-2 pt-1 text-xs ' +
        (selected ? 'bg-blue-300' : '')
      }
    >
      <UserIcon className="h-4 w-4 fill-blue-500" />
      {data.userTask?.userTaskDefName}
      <Handle type="source" position={Position.Right} className="bg-transparent" />
      <Handle
        type="target"
        position={Position.Left}
        className="border-b-4 border-l-4 border-t-4 border-b-transparent border-l-gray-500 border-t-transparent bg-transparent"
      />
    </div>
  )
}

export const UserTask = memo(Node)
