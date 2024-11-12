import { FC, Fragment } from 'react'
import { cn } from '@/components/utils'
import { utcToLocalDateTime } from '@/app/utils'
import { NodeRun } from 'littlehorse-client/proto'

type Props = {
  currentNode: NodeRun
}

export const SleepDefDetail: FC<Props> = ({ currentNode }) => {
  return (
    <Fragment>
      <div className="mb-2 items-center gap-2">
        {currentNode?.arrivalTime && (
          <div className="mb-2 mt-1 text-sm font-bold">
            Arrival Time : <span className=" pb-2">{utcToLocalDateTime(currentNode?.arrivalTime)}</span>
          </div>
        )}
        {currentNode?.endTime && (
          <div className="mb-2 mt-1 text-sm font-bold">
            End Time : <span className="  pb-2">{utcToLocalDateTime(currentNode?.endTime)}</span>
          </div>
        )}
      </div>
      <div className="mb-2 items-center gap-2">
        <div className="mb-2 mt-1 text-sm font-bold">
          Matured : <span className="border-2 border-blue-500 p-1">{currentNode?.sleep?.matured?.toString()}</span>
        </div>
      </div>

      <div className={cn('flex w-full flex-col overflow-auto rounded p-1', 'bg-zinc-500 text-white')}>
        <h3 className="font-bold">Maturation Time</h3>
        {currentNode?.sleep?.maturationTime && (
          <pre className="overflow-auto">{utcToLocalDateTime(currentNode?.sleep?.maturationTime)}</pre>
        )}
      </div>
    </Fragment>
  )
}
