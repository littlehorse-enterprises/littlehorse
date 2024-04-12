'use server'

import { lhClient } from '@/app/lhClient'
import { NodeRun } from 'littlehorse-client/dist/proto/node_run'
import { ThreadRun, WfRun } from 'littlehorse-client/dist/proto/wf_run'
import { WfSpec } from 'littlehorse-client/dist/proto/wf_spec'
import { cookies } from 'next/headers'

type Props = {
  id: string
}

export type ThreadRunWithNodeRuns = ThreadRun & { nodeRuns: NodeRun[] }

export type WfRunResponse = {
  wfRun: WfRun & { threadRuns: ThreadRunWithNodeRuns[] }
  wfSpec: WfSpec
  nodeRuns: NodeRun[]
}
export const getWfRun = async ({ id }: Props): Promise<WfRunResponse> => {
  const tenantId = cookies().get('tenantId')?.value
  const client = await lhClient({ tenantId })
  const wfRun = await client.getWfRun({ id })
  const [wfSpec, { results: nodeRuns }] = await Promise.all([
    client.getWfSpec({ ...wfRun.wfSpecId }),
    client.listNodeRuns({
      wfRunId: wfRun.id,
    }),
  ])

  const threadRuns = wfRun.threadRuns.map(threadRun => mergeThreadRunsWithNodeRuns(threadRun, nodeRuns))
  return { wfRun: { ...wfRun, threadRuns }, wfSpec, nodeRuns }
}

const mergeThreadRunsWithNodeRuns = (threadRun: ThreadRun, nodeRuns: NodeRun[]): ThreadRunWithNodeRuns => {
  return {
    ...threadRun,
    nodeRuns: nodeRuns.filter(nodeRun => nodeRun.threadSpecName === threadRun.threadSpecName),
  }
}
