import { FC } from 'react'
import { useDiagram } from '../../../hooks/useDiagram'
import { NodeStatus } from './NodeStatus'
import { NodeVariable } from './NodeVariable'

export const NodeRunInfo: FC<{ nodeRunIndex: number }> = ({ nodeRunIndex }) => {
  const { selectedNode } = useDiagram()
  if (!selectedNode) {
    return null
  }
  const nodeRunsList = selectedNode.data.nodeRunsList

  const nodeRun = nodeRunsList[nodeRunIndex]
  console.log('nodeRun', nodeRun)
  // const { type, id, data } = selectedNode
  // const { failureHandlers } = data
  return (
    <div className="flex max-w-full flex-1 flex-col">
      <NodeStatus status={nodeRun.status} />
      <NodeVariable label="Node Run ID:" text={nodeRun.id.position} />
      <NodeVariable label="Workflow Spec ID:" text={nodeRun.id.wfRunId.id} />
      <NodeVariable label="Workflow Spec :" text={nodeRun.id.nodeName} />
      <NodeVariable label="Threat name :" text={nodeRun.threadSpecName} />
      <NodeVariable label="Started :" text={nodeRun.arrivalTime} />
      <NodeVariable label="Finished :" text={nodeRun.endTime} />
    </div>
  )
}
