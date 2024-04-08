import { ExclamationTriangleIcon, PlusIcon } from '@heroicons/react/16/solid'
import { Node as NodeProto } from 'littlehorse-client/dist/proto/wf_spec'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '.'
import { Fade } from './Fade'

const Node: FC<NodeProps<NodeProto>> = ({ data }) => {
  const { fade } = data
  return (
    <Fade fade={fade}>
      <div className="relative cursor-pointer">
        <div className="ml-1 flex h-6 w-6 rotate-45 items-center justify-center border-[1px] border-gray-500 bg-gray-200">
          <PlusIcon className="h-4 w-4 rotate-45 fill-gray-500" />
        </div>
        {data.failureHandlers?.length > 0 && (
          <div className="absolute -right-2 -top-2 rounded-full bg-yellow-200 p-1">
            <ExclamationTriangleIcon className="h-2 w-2 fill-yellow-500" />
          </div>
        )}
        <Handle
          type="target"
          position={Position.Left}
          className="bg-transparent"
        />
        <Handle type="source" position={Position.Right} className="bg-transparent" />
      </div>
    </Fade>
  )
}

export const WaitForThreads = memo(Node)
