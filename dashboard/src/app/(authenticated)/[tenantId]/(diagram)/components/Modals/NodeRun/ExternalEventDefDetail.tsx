import { getVariableValue, utcToLocalDateTime } from '@/app/utils'
import { cn } from '@/components/utils'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { useQuery } from '@tanstack/react-query'
import { ClipboardIcon, RefreshCwIcon } from 'lucide-react'
import { FC } from 'react'
import { getExternalEvent } from '../../NodeTypes/ExternalEvent/actions'
import { AccordionNode } from './AccordionContent'

export const ExternalEventDefDetail: FC<AccordionNode<'externalEvent'>> = ({ nodeRun }) => {
  const { externalEventId } = nodeRun.nodeType!.value
  const { tenantId } = useWhoAmI()

  const { data, isLoading } = useQuery({
    queryKey: ['externalEvent', externalEventId],
    queryFn: async () => {
      if (!externalEventId) return
      const externalEventRun = await getExternalEvent({ tenantId, ...externalEventId })
      return externalEventRun
    },
  })

  if (isLoading) {
    return (
      <div className="flex min-h-[60px] items-center justify-center text-center">
        <RefreshCwIcon className="h-8 w-8 animate-spin text-blue-500" />
      </div>
    )
  }

  if (!data || !externalEventId) return

  return (
    <>
      <div className="mb-2 items-center gap-2">
        <div className="mb-2 mt-1  flex ">
          <span className="font-bold ">Task Guid :</span> <span>{externalEventId.guid}</span>
          <span className="ml-2 mt-1">
            <ClipboardIcon
              className="h-4 w-4 cursor-pointer fill-transparent stroke-blue-500"
              onClick={() => {
                navigator.clipboard.writeText(externalEventId!.guid ?? '')
              }}
            />
          </span>
        </div>
        <div className="mb-2 mt-1 ">
          <span className="font-bold">Triggered: </span>
          {data.createdAt && <span className="">{utcToLocalDateTime(data.createdAt)}</span>}
        </div>
      </div>
      {data.content && (
        <div className={cn('flex w-full flex-col overflow-auto rounded p-1', 'bg-zinc-500 text-white')}>
          <h3 className="font-bold">Content</h3>
          <pre className="overflow-auto">{getVariableValue(data.content)}</pre>
        </div>
      )}
    </>
  )
}
