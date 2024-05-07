'use server'
import { lhClient } from '@/app/lhClient'
import { WithTenant } from '@/types'
import { TaskRunId } from 'littlehorse-client/dist/proto/object_id'

export type TaskRunRequestProps = TaskRunId & WithTenant
export const getTaskRun = async ({ tenantId, ...req }: TaskRunRequestProps) => {
  const client = await lhClient({ tenantId })
  return client.getTaskRun(req)
}
