'use server'
import { lhClient } from '@/app/lhClient'
import { WithTenant } from '@/types'
import { SearchUserTaskRunRequest, UserTaskRunIdList } from 'littlehorse-client/dist/proto/service'

export type UserTaskRunSearchProps = SearchUserTaskRunRequest & WithTenant
export const searchUserTaskRun = async ({ tenantId, ...req }: UserTaskRunSearchProps): Promise<UserTaskRunIdList> => {
  const client = await lhClient({ tenantId })
  return client.searchUserTaskRun(req)
}
