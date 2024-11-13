'use server'
import { lhClient } from '@/app/lhClient'
import { WithTenant } from '@/types'
import { WfRun, RunWfRequest, VariableValue } from 'littlehorse-client/proto'

type RunWf = {
 variables?: { [key: string]: VariableValue };
} & Omit<RunWfRequest, 'variables'> &
  WithTenant
export const runWfSpec = async ({
  wfSpecName,
  tenantId,
  majorVersion,
  revision,
  parentWfRunId,
  id,
}: RunWf): Promise<WfRun> => {
  const client = await lhClient({ tenantId })
  return client.runWf({ wfSpecName, majorVersion, revision, parentWfRunId, id })
}
