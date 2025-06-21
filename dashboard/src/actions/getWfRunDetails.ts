'use server'

import { lhClient } from '@/utils/client/lhClient'
import { Variable, WfRun, WfRunId } from 'littlehorse-client/proto'

type Props = {
  wfRunId: WfRunId
  tenantId: string
}

export type WfRunDetails = {
  wfRun: WfRun
  variables: Variable[]
}

export const getWfRunDetails = async ({ wfRunId, tenantId }: Props): Promise<WfRunDetails> => {
  const client = await lhClient(tenantId)

  // Fetch related data in parallel
  const [wfRun, { results: variables }] = await Promise.all([
    client.getWfRun(wfRunId),
    client.listVariables({
      wfRunId,
    }),
  ])

  return {
    wfRun,
    variables,
  }
}
