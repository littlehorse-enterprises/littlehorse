'use server'

import { RescueThreadRunRequest, WfRunId } from 'littlehorse-client/proto'
import { lhClient } from '../lhClient'

export async function rescueWfRun(tenantId: string, wfRunId: WfRunId) {
  const client = await lhClient({ tenantId })
  await client.rescueThreadRun(RescueThreadRunRequest.create({ wfRunId }))
}
