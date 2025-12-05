import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { Checkpoint, TaskNodeRun as TaskNodeRunProto, TaskRun } from 'littlehorse-client/proto'
import { FC, useState } from 'react'
import useSWR from 'swr'
import { getCheckpoints } from '../../NodeTypes/Task/getCheckpoints'
import { getTaskRun } from '../../NodeTypes/Task/getTaskRun'
import { InputVariables } from '../Components'
import { Attempts } from '../Components/Attempts'
import { Checkpoints } from '../Components/Checkpoints'
import { NodeStatus } from '../Components/NodeStatus'
import { NodeVariable } from '../Components/NodeVariable'

export const TaskNodeRun: FC<{ node: TaskNodeRunProto }> = ({ node }) => {
  const taskRunId = node.taskRunId
  const { tenantId } = useWhoAmI()
  const [attemptIndex, setAttemptIndex] = useState(0)

  const key = taskRunId ? ['taskRun', tenantId, taskRunId.taskGuid] : null
  const { data: nodeTask } = useSWR<TaskRun | undefined>(key, async () => {
    if (!taskRunId) return undefined
    return getTaskRun({ tenantId, ...taskRunId })
  })

  const checkpointsKey =
    taskRunId && nodeTask?.totalCheckpoints
      ? ['checkpoints', tenantId, taskRunId.taskGuid, nodeTask.totalCheckpoints]
      : null
  const { data: checkpoints } = useSWR<Checkpoint[]>(checkpointsKey, async () => {
    if (!taskRunId || !nodeTask?.totalCheckpoints) return []
    return getCheckpoints({ tenantId, taskRunId, totalCheckpoints: nodeTask.totalCheckpoints })
  })

  return (
    <div className="ml-1 flex max-w-full flex-1 flex-col">
      {nodeTask?.status && <NodeStatus status={nodeTask.status} type="task" />}
      <NodeVariable label="Node Type:" text="Task" />

      <NodeVariable label="taskGuid:" text={node.taskRunId?.taskGuid} />
      <NodeVariable label="TaskDefId:" text={nodeTask?.taskDefId?.name} />
      <NodeVariable label="position:" text={`${nodeTask?.source?.taskRunSource?.value.nodeRunId?.position}`} />
      <NodeVariable
        label="threadRunNumber:"
        text={`${nodeTask?.source?.taskRunSource?.value.nodeRunId?.threadRunNumber}`}
      />
      <NodeVariable label="wfRunId:" text={`${nodeTask?.source?.taskRunSource?.value.nodeRunId?.wfRunId?.id}`} />
      <NodeVariable label="scheduledAt:" text={nodeTask?.scheduledAt} type="date" />
      <NodeVariable label="timeoutSeconds:" text={`${nodeTask?.timeoutSeconds}`} />
      <NodeVariable label="totalCheckpoints:" text={`${nodeTask?.totalCheckpoints}`} />
      {checkpoints && checkpoints.length > 0 && <Checkpoints checkpoints={checkpoints} />}
      {nodeTask?.exponentialBackoff && (
        <NodeVariable label="baseIntervalMs:" text={`${nodeTask?.exponentialBackoff.baseIntervalMs}`} />
      )}
      {nodeTask?.exponentialBackoff && (
        <NodeVariable label="maxDelayMs:" text={`${nodeTask?.exponentialBackoff.maxDelayMs}`} />
      )}
      {nodeTask?.exponentialBackoff && (
        <NodeVariable label="multiplier:" text={`${nodeTask?.exponentialBackoff.multiplier}`} />
      )}
      {nodeTask?.attempts && (
        <Attempts attempts={nodeTask?.attempts} attemptIndex={attemptIndex} setAttemptIndex={setAttemptIndex} />
      )}
      {nodeTask && (
        <div className="ml-1">
          <InputVariables variables={nodeTask.inputVariables} />
        </div>
      )}
    </div>
  )
}
