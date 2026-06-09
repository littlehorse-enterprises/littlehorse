'use server'

import { lhClient } from '@/app/lhClient'
import { WithTenant } from '@/types'
import { CountNodeRunRequest, WfSpecId } from 'littlehorse-client/proto'
import { ClientError, Status } from 'nice-grpc-common'

type Props = WithTenant & { wfSpecId: WfSpecId }

export type NodeRunCountResult = { status: 'ok'; count: number } | { status: 'unavailable'; message: string }

export const countNodeRun = async ({ tenantId, wfSpecId }: Props): Promise<NodeRunCountResult> => {
  const client = await lhClient({ tenantId })
  const request: CountNodeRunRequest = {
    filter: {
      $case: 'wfSpecFilter',
      value: {
        wfSpecName: wfSpecId.name,
        wfSpecMajorVersion: wfSpecId.majorVersion,
        wfSpecRevision: wfSpecId.revision,
      },
    },
  }
  try {
    const response = await client.countNodeRun(request)
    return { status: 'ok', count: Number(response.value) }
  } catch (error) {
    if (error instanceof ClientError && (error.code === Status.UNIMPLEMENTED || error.code === Status.NOT_FOUND)) {
      return { status: 'unavailable', message: 'Storage usage is not available on this server version.' }
    }
    throw error
  }
}
