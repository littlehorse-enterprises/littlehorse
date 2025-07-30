import { getVariableValue, utcToLocalDateTime } from '@/app/utils'
import { cn } from '@/components/utils'
import { useQuery } from '@tanstack/react-query'
import { ClipboardIcon, RefreshCwIcon } from 'lucide-react'
import { useParams } from 'next/navigation'
import { FC } from 'react'
import { getWorkflowEvent } from '../../NodeTypes/ThrowEvent/getWorkflowEvent'
import { AccordionNode } from './AccordionContent'

export const WorkflowEventDefDetail: FC<AccordionNode<'throwEvent'>> = ({ nodeRun }) => {
  const tenantId = useParams().tenantId as string
  const { workflowEventId } = nodeRun.nodeType.value
  const { data, isLoading } = useQuery({
    queryKey: ['taskRun', workflowEventId, tenantId],
    queryFn: async () => {
      if (!workflowEventId) return
      const taskRun = await getWorkflowEvent({ tenantId, ...workflowEventId })

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

      {data.content && (
        <div className={cn('mt-2 flex w-full flex-col overflow-auto rounded p-1', 'bg-zinc-500 text-white')}>
          <h3 className="font-bold">Content</h3>
          <pre className="overflow-auto">{getVariableValue(data.content)}</pre>
        </div>
      )}
    </div>
  )
}
