import { EnvelopeOpenIcon } from '@heroicons/react/24/outline'
import { Node as NodeProto } from 'littlehorse-client/dist/proto/wf_spec'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '.'
import { Fade } from './Fade'

const Node: FC<NodeProps<NodeProto>> = ({ data }) => {
  const { fade } = data
  return (
    <Fade fade={fade} status={data.nodeRun?.status}>
      <div className="relative cursor-pointer items-center justify-center text-xs">
        <div
          className={`items-center-justify-center flex rounded-full border-[1px] border-blue-500 bg-blue-200 p-[1px] text-xs `}
        >
          <div className="items-center-justify-center flex rounded-full border-[1px] border-blue-500 bg-blue-200 p-2 text-xs">
            <EnvelopeOpenIcon className="h-4 w-4 fill-transparent stroke-blue-500" />
          </div>
        </div>
        <Handle type="source" position={Position.Right} className="h-2 w-2 bg-transparent" />
        <Handle type="target" position={Position.Left} className="bg-transparent" />
        <div className="absolute flex w-full items-center justify-center whitespace-nowrap text-center	">
          <div className="block">{data.externalEvent?.externalEventDefId?.name}</div>
        </div>
      </div>
    </Fade>
  )
}

export const ExternalEvent = memo(Node)
