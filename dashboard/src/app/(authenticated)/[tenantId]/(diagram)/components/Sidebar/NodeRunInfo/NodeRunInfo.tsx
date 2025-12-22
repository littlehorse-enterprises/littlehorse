import { FC } from 'react'
import { useDiagram } from '../../../hooks/useDiagram'
import { NodeStatus } from '../Components/NodeStatus'
import { NodeVariable } from '../Components/NodeVariable'

export const NodeRunInfo: FC<{ nodeRunIndex: number }> = ({ nodeRunIndex }) => {
  const { selectedNode } = useDiagram()

  if (!selectedNode) {
    return null
  }

  if (!('nodeRunsList' in selectedNode.data)) {
    return null
  }

  const nodeRun = selectedNode.data.nodeRunsList[nodeRunIndex]
  return (
    <div className="ml-1 flex max-w-full flex-1 flex-col">
      {nodeRun.status && <NodeStatus status={nodeRun.status} />}
      <NodeVariable label="position:" text={`${nodeRun.id?.position}`} />
      <NodeVariable label="wfRunId:" text={nodeRun.id?.wfRunId?.id} />
      <NodeVariable label="wfSpecId:" text={nodeRun.wfSpecId?.name ?? 'N/A'} />
      <NodeVariable label="threadSpecName:" text={nodeRun.threadSpecName} />
      <NodeVariable label="arrivalTime:" text={nodeRun.arrivalTime} type={'date'} />
      {nodeRun.endTime !== 'RUNNING' && <NodeVariable label="endTime:" text={nodeRun.endTime} type={'date'} />}
    </div>
  )
}
