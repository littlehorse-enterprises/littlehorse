import { FC } from 'react'
import { LHStatus } from 'littlehorse-client/proto'
import { useDiagram } from '../../../hooks/useDiagram'
import { NodeStatus } from '../Components/NodeStatus'
import { NodeVariable } from '../Components/NodeVariable'
import { NodeTypeDocumentation } from '../Components/NodeTypeDocumentation'

export const NodeRunInfo: FC<{ nodeRunIndex: number }> = ({ nodeRunIndex }) => {
  const { selectedNode, threadRun, failedNodeId } = useDiagram()

  if (!selectedNode) {
    return null
  }

  if (!('nodeRunsList' in selectedNode.data)) {
    return null
  }

  const nodeRunsList = selectedNode.data.nodeRunsList
  const nodeRun = nodeRunsList[nodeRunIndex]
  const isFailedNodeWithNoRun = failedNodeId === selectedNode.id && (!nodeRunsList || nodeRunsList.length === 0)

  if (isFailedNodeWithNoRun && threadRun?.errorMessage) {
    return (
      <div className="ml-1 flex max-w-full flex-1 flex-col">
        <NodeStatus status={LHStatus.ERROR} errorMessage={threadRun.errorMessage} />
        <NodeTypeDocumentation
          nodeType={selectedNode.type}
          showNodeRun={true}
          className="ml-1 mt-1 text-sm font-bold"
        />
      </div>
    )
  }

  if (!nodeRun) {
    return null
  }

  const isFailedNode =
    threadRun?.errorMessage &&
    nodeRun.id?.position !== undefined &&
    nodeRun.id.position === threadRun.currentNodePosition

  return (
    <div className="ml-1 flex max-w-full flex-1 flex-col">
      {nodeRun.status && (
        <NodeStatus status={nodeRun.status} errorMessage={isFailedNode ? threadRun?.errorMessage : undefined} />
      )}
      <NodeTypeDocumentation nodeType={selectedNode.type} showNodeRun={true} className="ml-1 mt-1 text-sm font-bold" />
      <NodeVariable label="position:" text={`${nodeRun.id?.position}`} />
      <NodeVariable label="wfRunId:" text={nodeRun.id?.wfRunId?.id} />
      <NodeVariable label="wfSpecId:" text={nodeRun.wfSpecId?.name ?? 'N/A'} />
      <NodeVariable label="threadSpecName:" text={nodeRun.threadSpecName} />
      <NodeVariable label="arrivalTime:" text={nodeRun.arrivalTime} type={'date'} />
      {nodeRun.endTime !== 'RUNNING' && <NodeVariable label="endTime:" text={nodeRun.endTime} type={'date'} />}
    </div>
  )
}
