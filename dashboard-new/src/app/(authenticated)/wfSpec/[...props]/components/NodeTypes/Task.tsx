import { Cog6ToothIcon } from '@heroicons/react/16/solid'
import { Node as NodeProto } from 'littlehorse-client/dist/proto/wf_spec'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '.'
import { Fade } from './Fade'

const Node: FC<NodeProps<NodeProto>> = ({ selected, data }) => {
  const { fade } = data
  return (
    <Fade fade={fade}>
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
    </Fade>
  )
}

export const Task = memo(Node)
