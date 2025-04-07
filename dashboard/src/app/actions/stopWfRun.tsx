'use server'

import { WfRunId } from 'littlehorse-client/proto'
import { lhClient } from '../lhClient'

export async function stopWfRun(tenantId: string, wfRunId: WfRunId) {
  const client = await lhClient({ tenantId })
  await client.stopWfRun({ wfRunId })
}
