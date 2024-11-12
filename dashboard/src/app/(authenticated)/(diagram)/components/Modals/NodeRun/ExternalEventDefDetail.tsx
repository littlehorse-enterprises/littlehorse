import { FC } from 'react'
import { RefreshCwIcon, ClipboardIcon } from 'lucide-react'
import { cn } from '@/components/utils'
import { utcToLocalDateTime, getVariableValue } from '@/app/utils'
import { useQuery } from '@tanstack/react-query'
import { getExternalEvent } from '../../NodeTypes/ExternalEvent/getExternalEvent'
import { NodeRun } from 'littlehorse-client/proto'

type Props = {
  currentNode: NodeRun
}

export const ExternalEventDefDetail: FC<Props> = ({ currentNode }) => {
  const externalEventDefId = currentNode?.externalEvent?.externalEventId?.externalEventDefId
  const guid = currentNode?.externalEvent?.externalEventId?.guid
  const wfRunId = currentNode?.externalEvent?.externalEventId?.wfRunId?.id
  const { data, isLoading } = useQuery({
    queryKey: ['taskRun', wfRunId, externalEventDefId],
    queryFn: async () => {
      if (!wfRunId) return
      if (!externalEventDefId) return
      if (!guid) return
      const externalEventRun = await getExternalEvent({
        wfRunId: {
          id: wfRunId,
          parentWfRunId: undefined,
        },
        externalEventDefId,
        guid,
      })
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

  if (!data) return

  return (
    <>
      <div className="mb-2 items-center gap-2">
        <div className="mb-2 mt-1  flex ">
          <span  className="font-bold ">Task Guid :</span> <span >{guid}</span>
          <span className="ml-2 mt-1">
            <ClipboardIcon
              className="h-4 w-4 cursor-pointer fill-transparent stroke-blue-500"
              onClick={() => {
                navigator.clipboard.writeText(guid ?? '')
              }}
            />
          </span>
        </div>
        <div className="mb-2 mt-1 ">
          <span className="font-bold">Triggered: </span>
          {data.createdAt && <span className="">{utcToLocalDateTime(data.createdAt)}</span>}
        </div>
      </div>

      <div className={cn('flex w-full flex-col overflow-auto rounded p-1', 'bg-zinc-500 text-white')}>
        <h3 className="font-bold">Content</h3>
        <pre className="overflow-auto">{getVariableValue(data.content)}</pre>
      </div>
    </>
  )
}
