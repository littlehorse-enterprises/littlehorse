import { ExternalEventNode as ExternalEventProto } from 'littlehorse-client/proto'
import { MailOpenIcon } from 'lucide-react'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '..'

import { Fade } from '../Fade'
import { SelectedNode } from '../SelectedNode'

const Node: FC<NodeProps<'externalEvent', ExternalEventProto>> = ({ data }) => {
  const { fade, nodeRunsList, externalEventDefId } = data
  const nodeRun = nodeRunsList?.[0]
  return (
    <>
      <SelectedNode />
      <Fade fade={fade} status={nodeRun?.status}>
        <div className="relative cursor-pointer items-center justify-center text-xs">
          <div
            className={
              'items-center-justify-center flex rounded-full border-[1px] border-blue-500 bg-blue-200 p-[1px] text-xs'
            }
          >
            <div className="items-center-justify-center flex rounded-full border-[1px] border-blue-500 bg-blue-200 p-2 text-xs">
              <MailOpenIcon className="h-4 w-4 fill-transparent stroke-blue-500" />
            </div>
          </div>
          <Handle type="source" position={Position.Right} className="h-2 w-2 bg-transparent" />
          <Handle type="target" position={Position.Left} className="bg-transparent" />
          <div className="absolute flex w-full items-center justify-center whitespace-nowrap text-center">
            <div className="block">{externalEventDefId?.name}</div>
          </div>
        </div>
      </Fade>
    </>
  )
}

export const ExternalEvent = memo(Node)
