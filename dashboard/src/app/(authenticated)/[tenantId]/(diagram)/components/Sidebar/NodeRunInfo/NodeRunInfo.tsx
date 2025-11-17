import { FC } from 'react'
import { useDiagram } from '../../../hooks/useDiagram'
import { NodeStatus } from './NodeStatus'
import { NodeVariable } from './NodeVariable'

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
      <NodeVariable label="Node Run ID:" text={`${nodeRun.id?.position}`} />
      <NodeVariable label="Workflow Spec ID:" text={nodeRun.id?.wfRunId?.id} />
      <NodeVariable label="Workflow Spec :" text={nodeRun.wfSpecId?.name ?? 'N/A'} />
      <NodeVariable label="Threat name :" text={nodeRun.threadSpecName} />
      <NodeVariable label="Started :" text={nodeRun.arrivalTime} type={'date'} />
      {nodeRun.status !== 'RUNNING' && <NodeVariable label="Finished :" text={nodeRun.endTime} type={'date'} />}
    </div>
  )
}
