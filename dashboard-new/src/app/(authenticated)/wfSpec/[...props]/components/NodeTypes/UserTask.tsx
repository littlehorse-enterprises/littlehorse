import { UserIcon } from '@heroicons/react/16/solid'
import { Node as NodeProto } from 'littlehorse-client/dist/proto/wf_spec'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '.'
import { Fade } from './Fade'

const Node: FC<NodeProps<NodeProto>> = ({ data, selected }) => {
  const { fade } = data
  return (
    <Fade fade={fade}>
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
          className="bg-transparent"
        />
      </div>
    </Fade>
  )
}

export const UserTask = memo(Node)
