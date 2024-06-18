import { Node as NodeProto } from 'littlehorse-client/proto'
import { ExternalLinkIcon, MailOpenIcon } from 'lucide-react'
import Link from 'next/link'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '.'
import { Fade } from './Fade'
import { NodeDetails } from './NodeDetails'

const Node: FC<NodeProps<NodeProto>> = ({ data }) => {
  if (!data.externalEvent) return null
  const { fade, externalEvent, nodeNeedsToBeHighlighted } = data

  return (
    <>
      <NodeDetails>
        <div className="">
          <div className="flex items-center gap-1 text-nowrap">
            <h3 className="font-bold">ExternalEventDef</h3>
            <Link
              className="flex items-center justify-center gap-1 text-blue-500 hover:underline"
              target="_blank"
              href={`/externalEventDef/${externalEvent.externalEventDefId?.name}`}
            >
              {externalEvent.externalEventDefId?.name} <ExternalLinkIcon className="h-4 w-4" />
            </Link>
          </div>
        </div>
      </NodeDetails>
      <Fade fade={fade} status={data.nodeRun?.status}>
        <div className="relative cursor-pointer items-center justify-center text-xs">
          <div
            className={
              'items-center-justify-center flex rounded-full border-[1px] border-blue-500 bg-blue-200 p-[1px] text-xs ' +
              (nodeNeedsToBeHighlighted ? ' shadow-lg shadow-blue-500' : '')
            }
          >
            <div className="items-center-justify-center flex rounded-full border-[1px] border-blue-500 bg-blue-200 p-2 text-xs">
              <MailOpenIcon className="h-4 w-4 fill-transparent stroke-blue-500" />
            </div>
          </div>
          <Handle type="source" position={Position.Right} className="h-2 w-2 bg-transparent" />
          <Handle type="target" position={Position.Left} className="bg-transparent" />
          <div className="absolute flex w-full items-center justify-center whitespace-nowrap text-center	">
            <div className="block">{data.externalEvent?.externalEventDefId?.name}</div>
          </div>
        </div>
      </Fade>
    </>
  )
}

export const ExternalEvent = memo(Node)
