import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { useQuery } from '@tanstack/react-query'
import { Node as NodeProto } from 'littlehorse-client/proto'
import { ExternalLinkIcon, EyeIcon, MailOpenIcon } from 'lucide-react'
import Link from 'next/link'
import { FC, memo, useCallback } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '..'
import { useModal } from '../../../hooks/useModal'
import { Fade } from '../Fade'
import { NodeDetails } from '../NodeDetails'
import { getExternalEventRun } from './getExternalEventRun'

const Node: FC<NodeProps<NodeProto>> = ({ data }) => {
  const { tenantId } = useWhoAmI()
  const { data: externalEventRun } = useQuery({
    queryKey: ['externalEventRun', data.nodeRun, tenantId],
    queryFn: async () => {
      if (data.nodeRun?.externalEvent?.externalEventId)
        return await getExternalEventRun({ tenantId, ...data.nodeRun.externalEvent.externalEventId })
      return null
    },
  })

  const { setModal, setShowModal } = useModal()

  const onClick = useCallback(() => {
    if (externalEventRun) {
      setModal({ type: 'externalEventRun', data: externalEventRun })
      setShowModal(true)
    }
  }, [externalEventRun, setModal, setShowModal])

  if (!data.externalEvent) return null
  const { fade, externalEvent, nodeNeedsToBeHighlighted, nodeRun } = data
  console.log('externalEvent', externalEvent)
  console.log('externalEventRun', externalEventRun)

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
          {nodeRun && (
            <div className="mt-2 flex justify-center">
              <button className="flex items-center gap-1 p-1 text-blue-500 hover:bg-gray-200" onClick={onClick}>
                <EyeIcon className="h-4 w-4" />
                Inspect TaskRun
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
