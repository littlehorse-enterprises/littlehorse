import { Node as NodeProto } from 'littlehorse-client/proto'
import { CircleArrowOutUpRightIcon, ExternalLinkIcon } from 'lucide-react'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '..'

import LinkWithTenant from '@/app/(authenticated)/[tenantId]/components/LinkWithTenant'
import { Fade } from '../Fade'
import { NodeDetails } from '../NodeDetails'

import { DiagramDataGroup } from '../DataGroupComponents/DiagramDataGroup'

const Node: FC<NodeProps<NodeProto>> = ({ data }) => {
  if (!data.throwEvent) return null

  const { fade, throwEvent: throwEventNode, nodeNeedsToBeHighlighted, nodeRun } = data
  return (
    <>
      <NodeDetails nodeRunList={data.nodeRunsList}>
        <DiagramDataGroup label={nodeRun ? "WorkflowEvent" : "WorkflowEventDef"}>
          <div>
            <div>
              <div className="flex items-center gap-1 text-nowrap">
                <LinkWithTenant
                  className="flex items-center justify-center gap-1 text-blue-500 hover:underline"
                  target="_blank"
                  href={`/workflowEventDef/${throwEventNode.eventDefId?.name}`}
                >
                  {throwEventNode.eventDefId?.name} <ExternalLinkIcon className="h-4 w-4" />
                </LinkWithTenant>
              </div>
            </div>
          </div>
        </DiagramDataGroup>
      </NodeDetails>
      <Fade fade={fade} status={data.nodeRun?.status}>
        <div className="relative cursor-pointer items-center justify-center text-xs">
          <div
            className={
              'items-center-justify-center flex rounded-full border-[1px] border-purple-500 bg-purple-200 p-[1px] text-xs' +
              (nodeNeedsToBeHighlighted ? ' shadow-lg shadow-purple-500' : '')
            }
          >
            <div className="items-center-justify-center flex rounded-full border-[1px] border-purple-500 bg-purple-200 p-2 text-xs">
              <CircleArrowOutUpRightIcon className="h-4 w-4 fill-transparent stroke-purple-500" />
            </div>
          </div>
          <Handle type="source" position={Position.Right} className="h-2 w-2 bg-transparent" />
          <Handle type="target" position={Position.Left} className="bg-transparent" />
          <div className="absolute flex w-full items-center justify-center whitespace-nowrap text-center">
            <div className="block">{data.throwEvent?.eventDefId?.name}</div>
          </div>
        </div>
      </Fade>
    </>
  )
}

export const ThrowEvent = memo(Node)
