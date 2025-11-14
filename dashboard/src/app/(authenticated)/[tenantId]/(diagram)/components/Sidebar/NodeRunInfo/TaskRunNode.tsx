import { TaskNodeRun, TaskRun } from 'littlehorse-client/proto'
import { FC, useLayoutEffect, useState } from 'react'
import { getTaskRun } from '../../NodeTypes/Task/getTaskRun'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { NodeVariable } from './NodeVariable'
import { InputVariables } from '../Components'
import { Attempts } from '../Components/Attempts'
import { NodeStatus } from './NodeStatus'

export const TaskRunNode: FC<{ node: TaskNodeRun }> = ({ node }) => {
  const taskRunId = node.taskRunId
  const { tenantId } = useWhoAmI()
  const [nodeTask, setNodeTask] = useState<TaskRun>()
  const [attemptIndex, setAttemptIndex] = useState(0)
  useLayoutEffect(() => {
    const fetchTaskRun = async () => {
      if (taskRunId) {
        try {
          const taskRunData = await getTaskRun({ tenantId, ...taskRunId })
          setNodeTask(taskRunData)
        } catch (error) {}
      }
    }

    fetchTaskRun()
  }, [tenantId, taskRunId])
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
      {nodeTask?.exponentialBackoff && (
        <NodeVariable label="baseIntervalMs:" text={`${nodeTask?.exponentialBackoff.baseIntervalMs}`} />
      )}
      {nodeTask?.exponentialBackoff && (
        <NodeVariable label="maxDelayMs:" text={`${nodeTask?.exponentialBackoff.maxDelayMs}`} />
      )}
      {nodeTask?.exponentialBackoff && (
        <NodeVariable label="multiplier:" text={`${nodeTask?.exponentialBackoff.multiplier}`} />
      )}
      {nodeTask && <InputVariables variables={nodeTask.inputVariables} />}
      {nodeTask?.attempts && (
        <Attempts attempts={nodeTask?.attempts} attemptIndex={attemptIndex} setAttemptIndex={setAttemptIndex} />
      )}
    </div>
  )
}
