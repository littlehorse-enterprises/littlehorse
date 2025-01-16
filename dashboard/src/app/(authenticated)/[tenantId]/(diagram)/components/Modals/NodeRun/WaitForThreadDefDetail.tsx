import { FC } from 'react'

import { utcToLocalDateTime } from '@/app/utils'
import { NodeRun } from 'littlehorse-client/proto'

export const WaitForThreadDefDetail: FC<{ nodeRun: NodeRun }> = ({ nodeRun }) => {
  return (
    <div className="mb-2 items-center gap-2">
      {nodeRun?.arrivalTime && (
        <div className="mb-2 mt-1 text-sm font-bold">
          Arrival Time : <span className=" pb-2">{utcToLocalDateTime(nodeRun?.arrivalTime)}</span>
        </div>
      )}
      {nodeRun?.endTime && (
        <div className="mb-2 mt-1 text-sm font-bold">
          End Time : <span className="  pb-2">{utcToLocalDateTime(nodeRun?.endTime)}</span>
        </div>
      )}
    </div>
  )
}
