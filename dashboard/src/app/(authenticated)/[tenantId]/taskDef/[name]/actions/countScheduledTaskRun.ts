'use server'

import { lhClient } from '@/app/lhClient'
import { WithTenant } from '@/types'
import { TaskStatus } from 'littlehorse-client/proto'
import { ClientError, Status } from 'nice-grpc-common'

type Props = WithTenant & { taskDefName: string }

export type QueueDepthResult = { status: 'ok'; count: number } | { status: 'unavailable'; message: string }

export const countScheduledTaskRun = async ({ tenantId, taskDefName }: Props): Promise<QueueDepthResult> => {
  const client = await lhClient({ tenantId })
  if (typeof client.countTaskRun !== 'function') {
    return { status: 'unavailable', message: 'Queue depth is not available on this server version.' }
  }

  const request = { taskDefName, status: TaskStatus.TASK_SCHEDULED }
  try {
    const response = await client.countTaskRun(request)
    return { status: 'ok', count: Number(response.value) }
  } catch (error) {
    if (error instanceof ClientError && (error.code === Status.UNIMPLEMENTED || error.code === Status.NOT_FOUND)) {
      return { status: 'unavailable', message: 'Queue depth is not available on this server version.' }
    }
    throw error
  }
}
