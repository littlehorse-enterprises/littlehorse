'use server'

import { WfRunId } from 'littlehorse-client/proto'
import { lhClient } from '../lhClient'

export async function getUserTaskRun(tenantId: string, wfRunId: string, userTaskGuid: string) {
  const client = await lhClient({ tenantId })
  return await client.getUserTaskRun({ wfRunId: { id: wfRunId }, userTaskGuid })
}
