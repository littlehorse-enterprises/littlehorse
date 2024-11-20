import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { useQuery } from '@tanstack/react-query'

import { LHStatus, Node as NodeProto, ThrowEventNode } from 'littlehorse-client/proto'
import { CircleArrowOutUpRightIcon, ExternalLinkIcon, EyeIcon } from 'lucide-react'
import Link from 'next/link'
import { FC, ReactNode, memo, useCallback } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '..'
import { useModal } from '../../../hooks/useModal'
import { Fade } from '../Fade'
import { NodeDetails } from '../NodeDetails'
import { getWorkflowEvent } from './getWorkflowEvent'
import LinkWithTenant from '@/app/[tenantId]/components/LinkWithTenant'

const Node: FC<NodeProps<NodeProto>> = ({ data }) => {
  const { tenantId } = useWhoAmI()
  const { data: workflowEvent } = useQuery({
    queryKey: ['workflowEvent', data.nodeRun, tenantId],
    queryFn: async () => {
      if (data.nodeRun?.throwEvent?.workflowEventId)
        return await getWorkflowEvent({ tenantId, ...data.nodeRun.throwEvent.workflowEventId })
      return null
    },
  })

  const { setModal, setShowModal } = useModal()

  const onClick = useCallback(() => {
    if (!workflowEvent) return

    setModal({ type: 'workflowEvent', data: workflowEvent })
    setShowModal(true)
  }, [workflowEvent, setModal, setShowModal])

  if (!data.throwEvent) return null

  const { fade, throwEvent: throwEventNode, nodeNeedsToBeHighlighted, nodeRun } = data
  return (
    <>
      <NodeDetails>
        <div>
          <div>
            <div className="flex items-center gap-1 text-nowrap">
              <h3 className="font-bold">WorkflowEventDef</h3>
              <LinkWithTenant
                className="flex items-center justify-center gap-1 text-blue-500 hover:underline"
                target="_blank"
                href={`/workflowEventDef/${throwEventNode.eventDefId?.name}`}
              >
                {throwEventNode.eventDefId?.name} <ExternalLinkIcon className="h-4 w-4" />
              </LinkWithTenant>
            </div>
          </div>
          {nodeRun &&
            (nodeRun.status == LHStatus.COMPLETED ||
              nodeRun.status == LHStatus.ERROR ||
              nodeRun.status == LHStatus.EXCEPTION ||
              nodeRun.status == LHStatus.HALTED) && (
              <div className="mt-2 flex justify-center">
                <button className="flex items-center gap-1 p-1 text-blue-500 hover:bg-gray-200" onClick={onClick}>
                  <EyeIcon className="h-4 w-4" />
                  Inspect WorkflowEvent
                </button>
              </div>
            )}
        </div>
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
