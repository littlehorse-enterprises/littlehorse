'use server'
import { lhClient } from '@/app/lhClient'
import { WithTenant } from '@/types'
import { UserTaskRunId } from 'littlehorse-client/dist/proto/object_id'

export type UserTaskRunRequestProps = UserTaskRunId & WithTenant
export const getUserTaskRun = async ({ tenantId, ...req }: UserTaskRunRequestProps) => {
  const client = await lhClient({ tenantId })
  return client.getUserTaskRun(req)
}
