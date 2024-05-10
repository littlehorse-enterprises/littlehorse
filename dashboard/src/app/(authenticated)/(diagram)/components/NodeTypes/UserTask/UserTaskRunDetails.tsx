import { FC } from 'react'
import { NodeRun } from 'littlehorse-client/dist/proto/node_run'
import { UserTaskNode } from 'littlehorse-client/dist/proto/wf_spec'
import { useQuery } from '@tanstack/react-query'
import { getUserTaskRun } from '@/app/(authenticated)/(diagram)/components/NodeTypes/UserTask/getUserTaskRun'

export const UserTaskRunDetails: FC<{ userTask?: UserTaskNode; nodeRun?: NodeRun }> = ({ userTask, nodeRun }) => {
  const { data } = useQuery({
    queryKey: ['userTaskRun', nodeRun],
    queryFn: async () => {
      if (nodeRun?.userTask?.userTaskRunId) return await getUserTaskRun(nodeRun.userTask.userTaskRunId)
      return null
    },
  })

  return <></>
}
