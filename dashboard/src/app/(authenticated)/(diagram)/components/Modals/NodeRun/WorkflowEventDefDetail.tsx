import { FC } from 'react'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { RefreshCwIcon, ClipboardIcon } from 'lucide-react'
import { NodeRun } from 'littlehorse-client/proto'
import { useQuery } from '@tanstack/react-query'

import { cn } from '@/components/utils'
import { getVariableValue, utcToLocalDateTime } from '@/app/utils'
import { getWorkflowEvent } from '@/app/(authenticated)/(diagram)/components/NodeTypes/ThrowEvent/getWorkflowEvent'

type Props = {
  currentNode: NodeRun
}
export const WorkflowEventDefDetail: FC<Props> = ({ currentNode }) => {
  const { tenantId } = useWhoAmI()
  const wfRunId = currentNode?.throwEvent?.workflowEventId?.wfRunId?.id
  const workflowEventDefId = currentNode?.throwEvent?.workflowEventId
  const { data, isLoading } = useQuery({
    queryKey: ['taskRun', wfRunId, workflowEventDefId],
    queryFn: async () => {
      if (!wfRunId) return
      if (!workflowEventDefId) return
      const taskRun = await getWorkflowEvent({ tenantId, ...workflowEventDefId })

      return taskRun
    },
  })

  if (isLoading) {
    return (
      <div className="flex min-h-[60px] items-center justify-center text-center">
        <RefreshCwIcon className="h-8 w-8 animate-spin text-blue-500" />
      </div>
    )
  }

  if (!data) return

  return (
    <div className="mb-2 items-center gap-2">
      <div className="mb-2 mt-1 flex ">
        <span className="font-bold">workflow Event Id :</span>
        <span> {data?.id?.workflowEventDefId?.name}</span>
        <span className="ml-2 mt-1">
          <ClipboardIcon
            className="h-4 w-4 cursor-pointer fill-transparent stroke-blue-500"
            onClick={() => {
              navigator.clipboard.writeText(data?.id?.workflowEventDefId?.name ?? '')
            }}
          />
        </span>
      </div>
      <div>
        <div className="flex items-center gap-2">
          <div className="font-bold">Time Thrown:</div>
          <div className="">{data.createdAt && utcToLocalDateTime(data.createdAt)}</div>
        </div>
        <div className="flex items-center gap-2">
          <div className="font-bold">Sequence Number:</div>
          <div className="">{data.id?.number}</div>
        </div>
      </div>

      <div className={cn('mt-2 flex w-full flex-col overflow-auto rounded p-1', 'bg-zinc-500 text-white')}>
        <h3 className="font-bold">Content</h3>
        <pre className="overflow-auto">{getVariableValue(data.content)}</pre>
      </div>
    </div>
  )
}
