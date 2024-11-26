'use server'

import { lhClient } from '@/app/lhClient'
import { NodeRun, ThreadRun, Variable, WfRun, WfRunId, WfSpec } from 'littlehorse-client/proto'

type Props = {
  ids: string[]
  tenantId: string
}

export type ThreadRunWithNodeRuns = ThreadRun & { nodeRuns: NodeRun[] }

export type WfRunResponse = {
  wfRun: WfRun & { threadRuns: ThreadRunWithNodeRuns[] }
  wfSpec: WfSpec
  nodeRuns: NodeRun[]
  variables: Variable[]
}
export const getWfRun = async ({ ids, tenantId }: Props): Promise<WfRunResponse> => {
  const client = await lhClient({ tenantId })
  const wfRunId = ids
    .reverse()
    .reduceRight<WfRunId | undefined>((parentWfRunId, id) => ({ id, parentWfRunId }), undefined)
  const wfRun = await client.getWfRun(wfRunId!)
  const [wfSpec, { results: nodeRuns }, { results: variables }] = await Promise.all([
    client.getWfSpec({ ...wfRun.wfSpecId }),
    client.listNodeRuns({
      wfRunId,
    }),
    client.listVariables({
      wfRunId,
    }),
  ])

  const threadRuns = wfRun.threadRuns.map(threadRun => mergeThreadRunsWithNodeRuns(threadRun, nodeRuns))
  return { wfRun: { ...wfRun, threadRuns }, wfSpec, nodeRuns, variables }
}

const mergeThreadRunsWithNodeRuns = (threadRun: ThreadRun, nodeRuns: NodeRun[]): ThreadRunWithNodeRuns => {
  return {
    ...threadRun,
    nodeRuns: nodeRuns.filter(
      nodeRun => nodeRun.threadSpecName === threadRun.threadSpecName && nodeRun.id?.threadRunNumber === threadRun.number
    ),
  }
}
