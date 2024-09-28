import { NodeRun, SleepNode } from 'littlehorse-client/proto'
import { FC } from 'react'
import { NodeDetails } from '../NodeDetails'
import { convertSecondsToReadableTime } from '../../../../../utils'

export const SleepTaskDetails: FC<{ sleepNode?: SleepNode; nodeRun?: NodeRun }> = ({ sleepNode, nodeRun }) => {
  if (!sleepNode?.rawSeconds?.literalValue?.int) return

  return (
    <NodeDetails>
      <div className="mb-2">
        <div className="flex items-center gap-1 whitespace-nowrap text-nowrap">
          <h3 className="font-bold">SleepTask</h3>
        </div>
        <div className="flex flex-col gap-2 text-nowrap">
          <div className="flex ">Time: {convertSecondsToReadableTime(sleepNode.rawSeconds.literalValue.int)}</div>
          {nodeRun && <div className="flex ">MaturationTime: {nodeRun?.sleep?.maturationTime}</div>}
        </div>
      </div>
    </NodeDetails>
  )
}
