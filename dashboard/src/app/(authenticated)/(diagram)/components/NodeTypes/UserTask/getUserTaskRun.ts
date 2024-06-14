'use server'
import { lhClient } from '@/app/lhClient'
import { WithTenant } from '@/types'
import { UserTaskRunId } from 'littlehorse-client'

export type UserTaskRunRequestProps = UserTaskRunId & WithTenant
export const getUserTaskRun = async ({ tenantId, ...req }: UserTaskRunRequestProps) => {
  const client = await lhClient({ tenantId })
  return client.getUserTaskRun(req)
}
