import { formatTime, getVariable } from '@/app/utils'
import { NodeRun, SleepNode } from 'littlehorse-client/proto'
import { FC } from 'react'
import { NodeRunsList } from '../../NodeRunsList'
import { NodeDetails } from '../NodeDetails'
import { DiagramDataGroup } from '../DataGroupComponents/DiagramDataGroup'
export const SleepDetails: FC<{ sleepNode?: SleepNode; nodeRunsList: [NodeRun] }> = ({ sleepNode, nodeRunsList }) => {
  if (!sleepNode) return

  const timeValue = getVariable(sleepNode?.rawSeconds)
  return (
    <NodeDetails nodeRunList={nodeRunsList}>
      <DiagramDataGroup label="Sleep">
        <div className="mb-2">
          <div className="flex flex-col gap-1 text-nowrap">
            <div className="flex ">
              {sleepNode.rawSeconds && (
                <div>Time: {typeof timeValue === 'number' ? formatTime(timeValue) : String(timeValue)}</div>
              )}
              {sleepNode.timestamp && <div>{String(getVariable(sleepNode.timestamp))}</div>}
              {sleepNode.isoDate && <div>{String(getVariable(sleepNode.isoDate))}</div>}
            </div>

            <NodeRunsList nodeRuns={nodeRunsList} />
          </div>
        </div>
      </DiagramDataGroup>
    </NodeDetails>
  )
}
