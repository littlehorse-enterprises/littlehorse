import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { useQuery } from '@tanstack/react-query'

import { formatTime, getVariableValue } from '@/app/utils'
import { LHStatus, Node as NodeProto } from 'littlehorse-client/proto'
import { ExternalLinkIcon, EyeIcon, MailOpenIcon } from 'lucide-react'
import Link from 'next/link'
import { FC, memo, useCallback } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '..'
import { useModal } from '../../../hooks/useModal'
import { Fade } from '../Fade'
import { NodeDetails } from '../NodeDetails'
import { getExternalEvent } from './getExternalEvent'

const Node: FC<NodeProps<NodeProto>> = ({ data }) => {
  const { tenantId } = useWhoAmI()
  const { data: externalEvent } = useQuery({
    queryKey: ['externalEvent', data.nodeRun, tenantId],
    queryFn: async () => {
      if (data.nodeRun?.externalEvent?.externalEventId)
        return await getExternalEvent({ tenantId, ...data.nodeRun.externalEvent.externalEventId })
      return null
    },
  })

  const { setModal, setShowModal } = useModal()

  const onClick = useCallback(() => {
    if (!externalEvent) return

    setModal({ type: 'externalEvent', data: externalEvent })
    setShowModal(true)
  }, [externalEvent, setModal, setShowModal])

  if (!data.externalEvent) return null

  const { fade, externalEvent: externalEventNode, nodeNeedsToBeHighlighted, nodeRun } = data
  return (
    <>
      <NodeDetails>
        <div>
          <div>
            <div className="flex items-center gap-1 text-nowrap">
              <h3 className="font-bold">ExternalEventDef</h3>
              <Link
                className="flex items-center justify-center gap-1 text-blue-500 hover:underline"
                target="_blank"
                href={`/externalEventDef/${externalEventNode.externalEventDefId?.name}`}
              >
                {externalEventNode.externalEventDefId?.name} <ExternalLinkIcon className="h-4 w-4" />
              </Link>
            </div>
            {
              <div className="flex gap-2 text-nowrap">
                <div className="flex items-center justify-center">
                  Timeout:{' '}
                  {externalEventNode.timeoutSeconds
                    ? formatTime(getVariableValue(externalEventNode.timeoutSeconds.literalValue) as number)
                    : 'N/A'}
                </div>
              </div>
            }
          </div>
          {nodeRun &&
            (nodeRun.status == LHStatus.COMPLETED ||
              nodeRun.status == LHStatus.ERROR ||
              nodeRun.status == LHStatus.EXCEPTION ||
              nodeRun.status == LHStatus.HALTED) && (
              <div className="mt-2 flex justify-center">
                <button className="flex items-center gap-1 p-1 text-blue-500 hover:bg-gray-200" onClick={onClick}>
                  <EyeIcon className="h-4 w-4" />
                  Inspect ExternalEvent
                </button>
              </div>
            )}
        </div>
      </NodeDetails>
      <Fade fade={fade} status={data.nodeRun?.status}>
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
            <div className="block">{data.externalEvent?.externalEventDefId?.name}</div>
          </div>
        </div>
      </Fade>
    </>
  )
}

export const ExternalEvent = memo(Node)
