import { FC } from 'react'
import { NodeRun } from 'littlehorse-client/dist/proto/node_run'
import { UserTaskNode } from 'littlehorse-client/dist/proto/wf_spec'
import { useQuery } from '@tanstack/react-query'
import { getUserTaskRun } from '@/app/(authenticated)/(diagram)/components/NodeTypes/UserTask/getUserTaskRun'
import { getVariable } from '@/app/utils'

export const UserTaskRunDetails: FC<{ userTask?: UserTaskNode; nodeRun?: NodeRun }> = ({ userTask, nodeRun }) => {
  const { data } = useQuery({
    queryKey: ['userTaskRun', nodeRun],
    queryFn: async () => {
      if (nodeRun?.userTask?.userTaskRunId) return await getUserTaskRun(nodeRun.userTask.userTaskRunId)
      return null
    },
  })

  return (
    data && (
      <div className="mb-2 flex gap-2 text-nowrap">
        {data.userGroup && <div className="flex items-center justify-center">Group: {data.userGroup}</div>}
        {data.userId && <div className="flex items-center justify-center">User: {data.userId}</div>}
      </div>
    )
  )
}
