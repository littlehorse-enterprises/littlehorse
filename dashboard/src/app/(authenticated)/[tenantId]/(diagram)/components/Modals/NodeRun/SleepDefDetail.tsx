import { utcToLocalDateTime } from '@/app/utils'
import { cn } from '@/components/utils'
import { FC, Fragment } from 'react'
import { AccordionNode } from './AccordionContent'

export const SleepDefDetail: FC<AccordionNode> = ({ nodeRun }) => {
  return (
    <Fragment>
      <div className="mb-2 items-center gap-2">
        {nodeRun.arrivalTime && (
          <div className="mb-2 mt-1 text-sm font-bold">
            Arrival Time : <span className=" pb-2">{utcToLocalDateTime(nodeRun.arrivalTime)}</span>
          </div>
        )}
        {nodeRun.endTime && (
          <div className="mb-2 mt-1 text-sm font-bold">
            End Time : <span className="  pb-2">{utcToLocalDateTime(nodeRun.endTime)}</span>
          </div>
        )}
      </div>
      <div className="mb-2 items-center gap-2">
        <div className="mb-2 mt-1 text-sm font-bold">
          Matured : <span className="border-2 border-blue-500 p-1">{nodeRun.sleep?.matured?.toString()}</span>
        </div>
      </div>

      <div className={cn('flex w-full flex-col overflow-auto rounded p-1', 'bg-zinc-500 text-white')}>
        <h3 className="font-bold">Maturation Time</h3>
        {nodeRun.sleep?.maturationTime && (
          <pre className="overflow-auto">{utcToLocalDateTime(nodeRun.sleep?.maturationTime)}</pre>
        )}
      </div>
    </Fragment>
  )
}
