import { formatTime, getVariable } from '@/app/utils'
import { NodeRun, SleepNode } from 'littlehorse-client/proto'
import { FC } from 'react'
import { NodeRunsList } from '../../NodeRunsList'
import { DiagramDataGroup } from '../DataGroupComponents/DiagramDataGroup'
import { NodeDetails } from '../NodeDetails'
export const SleepDetails: FC<{ sleepLength?: SleepNode['sleepLength']; nodeRunsList: [NodeRun] }> = ({
  sleepLength,
  nodeRunsList,
}) => {
  if (!sleepLength) return

  const { $case, value } = sleepLength

  return (
    <NodeDetails nodeRunList={nodeRunsList}>
      <DiagramDataGroup label="Sleep">
        <div className="mb-2">
          <div className="flex flex-col gap-1 text-nowrap">
            <div className="flex ">
              {$case === 'rawSeconds' && <div>Time: {formatTime(parseInt(getVariable(value)))}</div>}
              {$case === 'isoDate' || ($case === 'timestamp' && <div>{getVariable(value)}</div>)}
            </div>

            <NodeRunsList nodeRuns={nodeRunsList} />
          </div>
        </div>
      </DiagramDataGroup>
    </NodeDetails>
  )
}
