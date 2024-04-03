'use server'

import { lhClient } from '@/app/lhClient'
import { WfRun } from 'littlehorse-client/dist/proto/wf_run'
import { WfSpec } from 'littlehorse-client/dist/proto/wf_spec'
import { cookies } from 'next/headers'

type Props = {
  id: string
}

export type WfRunResponse = {
  wfRun: WfRun
  wfSpec: WfSpec
}
export const getWfRun = async ({ id }: Props): Promise<WfRunResponse> => {
  const tenantId = cookies().get('tenantId')?.value
  const client = await lhClient({ tenantId })
  const wfRun = await client.getWfRun({ id })
  const wfSpec = await client.getWfSpec({ ...wfRun.wfSpecId })
  const threadRun = await client.getNodeRun({
    wfRunId: wfRun.id,
  })
  return { wfRun, wfSpec }
}
