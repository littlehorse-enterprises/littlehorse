import { EnvelopeOpenIcon } from '@heroicons/react/24/outline'
import { Node as NodeProto } from 'littlehorse-client/dist/proto/wf_spec'
import { FC, memo } from 'react'
import { Handle, NodeProps, Position } from 'reactflow'

const Node: FC<NodeProps<NodeProto>> = ({ data }) => {
  return (
    <div className="relative cursor-pointer items-center justify-center text-xs">
      <div className="items-center-justify-center flex rounded-full border-[1px] border-blue-500 bg-blue-200 p-[1px] text-xs">
        <div className="items-center-justify-center flex rounded-full border-[1px] border-blue-500 bg-blue-200 p-2 text-xs">
          <EnvelopeOpenIcon className="h-4 w-4 fill-transparent stroke-blue-500" />
        </div>
      </div>
      <Handle type="source" position={Position.Right} className="h-2 w-2 bg-transparent" />
      <Handle
        type="target"
        position={Position.Left}
        className="border-b-4 border-l-4 border-t-4 border-b-transparent border-l-gray-500 border-t-transparent bg-transparent"
      />
      <div className="absolute flex w-full items-center justify-center whitespace-nowrap text-center	">
        <div className="block">{data.externalEvent?.externalEventDefId?.name}</div>
      </div>
    </div>
  )
}

export const ExternalEvent = memo(Node)
