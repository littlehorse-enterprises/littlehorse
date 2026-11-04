'use server'

import { ResumeWfRunRequest, WfRunId } from 'littlehorse-client/proto'
import { lhClient } from '../lhClient'

export async function resumeWfRun(tenantId: string, wfRunId: WfRunId) {
  const client = await lhClient({ tenantId })
  await client.resumeWfRun(ResumeWfRunRequest.create({ wfRunId }))
}
