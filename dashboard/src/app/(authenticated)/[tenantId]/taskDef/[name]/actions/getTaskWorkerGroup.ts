'use server'

import { lhClient } from '@/app/lhClient'
import { WithTenant } from '@/types'
import { TaskDefId, TaskWorkerGroup } from 'littlehorse-client/proto'
import { ClientError, Status } from 'nice-grpc-common'

type Props = WithTenant & { taskDefName: string }

export const getTaskWorkerGroup = async ({ tenantId, taskDefName }: Props): Promise<TaskWorkerGroup | null> => {
  const client = await lhClient({ tenantId })
  const request: TaskDefId = { name: taskDefName }
  try {
    return await client.getTaskWorkerGroup(request)
  } catch (error) {
    if (error instanceof ClientError && error.code === Status.NOT_FOUND) return null
    throw error
  }
}
