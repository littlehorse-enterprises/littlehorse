import { UserTaskNodeRun as UserTaskNodeRunProto, UserTaskRun } from 'littlehorse-client/proto'
import { FC } from 'react'
import { NodeVariable } from '../../Components/NodeVariable'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import useSWR from 'swr'
import { getUserTaskRun } from '../../../NodeTypes/UserTask/getUserTaskRun'
import { Events } from './Events'
import { Results } from '../../Components/Results'

export const UserTaskNodeRun: FC<{ node: UserTaskNodeRunProto }> = ({ node }) => {
  const { userTaskRunId } = node
  const { tenantId } = useWhoAmI()

  const key = userTaskRunId ? ['userTaskRun', tenantId, userTaskRunId.userTaskGuid] : null
  const { data: nodeTask } = useSWR<UserTaskRun | undefined>(key, async () => {
    if (!userTaskRunId) return undefined
    return getUserTaskRun({ tenantId, ...userTaskRunId })
  })

  const resultsArray = Object.entries(nodeTask?.results || {})
  return (
    <div>
      <NodeVariable label="Node Type:" text="User task" />
      <NodeVariable label="wfRunId:" text={node.userTaskRunId?.wfRunId?.id} />
      <NodeVariable label="userTaskGuid:" text={node.userTaskRunId?.userTaskGuid} />
      <NodeVariable label="user_task_def_id:" text={nodeTask?.userTaskDefId?.name} />
      <NodeVariable label="user_group:" text={nodeTask?.userGroup} />
      {nodeTask?.userId && <NodeVariable label="user_id:" text={nodeTask?.userId} />}
      {nodeTask?.notes && <NodeVariable label="notes:" text={nodeTask?.notes} />}
      <NodeVariable label="scheduled_time:" text={nodeTask?.scheduledTime} type="date" />
      <NodeVariable label="position:" text={`${nodeTask?.nodeRunId?.position}`} />
      <NodeVariable label="threadRunNumber:" text={`${nodeTask?.nodeRunId?.threadRunNumber}`} />
      <NodeVariable label="epoch:" text={`${nodeTask?.epoch}`} />
      {resultsArray.length > 0 && <Results variables={resultsArray} classTitle="font-bold" />}
      {nodeTask?.events && <Events events={nodeTask.events} />}
    </div>
  )
}
