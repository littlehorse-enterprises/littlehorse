import { formatTime, getVariable, getVariableValue } from '@/app/utils'
import { Node as NodeProto } from 'littlehorse-client/proto'
import { ExternalLinkIcon, MailOpenIcon } from 'lucide-react'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '..'

import { Fade } from '../Fade'
import { NodeDetails } from '../NodeDetails'

import LinkWithTenant from '@/app/(authenticated)/[tenantId]/components/LinkWithTenant'
import { DiagramDataGroup } from '../DataGroupComponents/DiagramDataGroup'
import { Entry } from '../DataGroupComponents/Entry'
import PostEvent from './PostEvent'

const Node: FC<NodeProps<NodeProto>> = ({ data }) => {
  if (!data.externalEvent) return null
  const { fade, externalEvent: externalEventNode, nodeNeedsToBeHighlighted, nodeRun } = data

  return (
    <>
      <NodeDetails nodeRunList={data.nodeRunsList}>
        <DiagramDataGroup label={nodeRun ? 'ExternalEvent' : 'ExternalEventDef'}>
          <div>
            <div className="flex gap-1 text-nowrap">
              <LinkWithTenant
                className="flex items-center justify-center gap-1 text-blue-500 hover:underline"
                target="_blank"
                href={`/externalEventDef/${externalEventNode.externalEventDefId?.name}`}
              >
                {externalEventNode.externalEventDefId?.name} <ExternalLinkIcon className="h-4 w-4" />
              </LinkWithTenant>
            </div>
            <Entry label="Timeout">
              {externalEventNode.timeoutSeconds
                ? formatTime(getVariableValue(externalEventNode.timeoutSeconds.literalValue) as number)
                : 'N/A'}
            </Entry>
            <Entry label="Correlation Key">
              {nodeRun ? nodeRun?.externalEvent?.correlationKey : getVariable(externalEventNode.correlationKey)}
            </Entry>
            {nodeRun && !nodeRun.externalEvent?.eventTime && <PostEvent nodeRun={nodeRun} />}
          </div>
        </DiagramDataGroup>
      </NodeDetails>
      <Fade fade={fade} status={data.nodeRunsList[data.nodeRunsList.length - 1]?.status}>
        <div className="relative cursor-pointer items-center justify-center text-xs">
          <div
            className={
              'items-center-justify-center flex rounded-full border-[1px] border-blue-500 bg-blue-200 p-[1px] text-xs' +
              (nodeNeedsToBeHighlighted ? ' shadow-lg shadow-blue-500' : '')
            }
          >
            <div className="items-center-justify-center flex rounded-full border-[1px] border-blue-500 bg-blue-200 p-2 text-xs">
              <MailOpenIcon className="h-4 w-4 fill-transparent stroke-blue-500" />
            </div>
          </div>
          <Handle type="source" position={Position.Right} className="h-2 w-2 bg-transparent" />
          <Handle type="target" position={Position.Left} className="bg-transparent" />
          <div className="absolute flex w-full items-center justify-center whitespace-nowrap text-center">
            <div className="block">{externalEventNode.externalEventDefId?.name}</div>
          </div>
        </div>
      </Fade>
    </>
  )
}

export const ExternalEvent = memo(Node)
