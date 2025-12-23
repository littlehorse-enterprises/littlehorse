'use server'
import { lhClient } from '@/app/lhClient'
import { WithTenant } from '@/types'
import { RunWfRequest, WfRun } from 'littlehorse-client/proto'

export const runWfSpec = async ({
  wfSpecName,
  tenantId,
  majorVersion,
  revision,
  parentWfRunId,
  id,
  variables,
}: RunWfRequest & WithTenant): Promise<WfRun> => {
  const client = await lhClient({ tenantId })
  return client.runWf({ wfSpecName, majorVersion, revision, parentWfRunId, id, variables })
}
