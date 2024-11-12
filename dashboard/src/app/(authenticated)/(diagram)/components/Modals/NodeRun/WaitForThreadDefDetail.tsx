import { FC } from 'react'

import { utcToLocalDateTime } from '@/app/utils'
import { NodeRun } from 'littlehorse-client/proto'

type Props =  {
  currentNode: NodeRun
}
export const WaitForThreadDefDetail: FC<Props> = ({ currentNode }) => {
  return (
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
  )
}
