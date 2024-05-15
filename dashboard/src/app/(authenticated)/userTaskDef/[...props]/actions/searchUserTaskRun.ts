'use server'
import { lhClient } from '@/app/lhClient'
import { WithTenant } from '@/types'
import { SearchUserTaskRunRequest, UserTaskRunIdList } from 'littlehorse-client/dist/proto/service'
import { UserTaskRun } from 'littlehorse-client/dist/proto/user_tasks'

export interface PaginatedUserTaskRunList extends UserTaskRunIdList {
  resultsWithDetails: UserTaskRun[]
}

export type UserTaskRunSearchProps = SearchUserTaskRunRequest & WithTenant
export const searchUserTaskRun = async ({
  tenantId,
  ...req
}: UserTaskRunSearchProps): Promise<PaginatedUserTaskRunList> => {
  const client = await lhClient({ tenantId })
  const userTaskRunIdList: UserTaskRunIdList = await client.searchUserTaskRun(req)

  const userTaskRunsPromises: Promise<UserTaskRun>[] = userTaskRunIdList.results.map(async userTaskRunId => {
    return await client.getUserTaskRun({ wfRunId: userTaskRunId.wfRunId, userTaskGuid: userTaskRunId.userTaskGuid })
  })

  const allUserTasksWithDetails: Awaited<UserTaskRun>[] = await Promise.all(userTaskRunsPromises)

  return { ...userTaskRunIdList, resultsWithDetails: allUserTasksWithDetails }
}
