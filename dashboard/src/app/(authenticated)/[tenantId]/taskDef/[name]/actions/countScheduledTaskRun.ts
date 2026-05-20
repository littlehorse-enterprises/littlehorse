'use server'

import { lhClient } from '@/app/lhClient'
import { WithTenant } from '@/types'
import { ClientError, Status } from 'nice-grpc-common'

type Props = WithTenant & { taskDefName: string }

export type QueueDepthResult =
  | { status: 'ok'; count: number }
  | { status: 'unavailable'; message: string }

export const countScheduledTaskRun = async ({ tenantId, taskDefName }: Props): Promise<QueueDepthResult> => {
  const client = await lhClient({ tenantId })
  if (typeof client.countScheduledTaskRun !== 'function') {
    return { status: 'unavailable', message: 'Queue depth is not available on this server version.' }
  }

  const request = { taskDefName }
  try {
    const response = await client.countScheduledTaskRun(request)
    return { status: 'ok', count: Number(response.value) }
  } catch (error) {
    if (error instanceof ClientError && (error.code === Status.UNIMPLEMENTED || error.code === Status.NOT_FOUND)) {
      return { status: 'unavailable', message: 'Queue depth is not available on this server version.' }
    }
    throw error
  }
}
