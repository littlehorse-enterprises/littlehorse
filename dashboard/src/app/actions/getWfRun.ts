'use server'

import { lhClient } from '@/app/lhClient'
import { NodeRun, ThreadRun, Variable, WfRun, WfRunId, WfSpec } from 'littlehorse-client/proto'
import { getInheritedVariables } from './getInheritedVariables'

type Props = {
  wfRunId: WfRunId
  tenantId: string
}

export type ThreadRunWithNodeRuns = ThreadRun & { nodeRuns: NodeRun[] }

export type WfRunResponse = {
  wfRun: WfRun & { threadRuns: ThreadRunWithNodeRuns[] }
  wfSpec: WfSpec
  nodeRuns: NodeRun[]
  variables: Variable[]
}
export const getWfRun = async ({ wfRunId, tenantId }: Props): Promise<WfRunResponse> => {
  const client = await lhClient({ tenantId })
  const wfRun = await client.getWfRun(wfRunId)
  const [wfSpec, { results: nodeRuns }, { results: variables }] = await Promise.all([
    client.getWfSpec({ ...wfRun.wfSpecId }),
    client.listNodeRuns({
      wfRunId,
    }),
    client.listVariables({
      wfRunId,
    }),
  ])

  const inheritedVariables = await getInheritedVariables(
    wfRunId,
    wfSpec.threadSpecs[wfRun.threadRuns[0].threadSpecName].variableDefs,
    tenantId
  )

  const threadRuns = wfRun.threadRuns.map(threadRun => mergeThreadRunsWithNodeRuns(threadRun, nodeRuns))
  return { wfRun: { ...wfRun, threadRuns }, wfSpec, nodeRuns, variables: [...variables, ...inheritedVariables] }
}

const mergeThreadRunsWithNodeRuns = (threadRun: ThreadRun, nodeRuns: NodeRun[]): ThreadRunWithNodeRuns => {
  return {
    ...threadRun,
    nodeRuns: nodeRuns.filter(
      nodeRun => nodeRun.threadSpecName === threadRun.threadSpecName && nodeRun.id?.threadRunNumber === threadRun.number
    ),
  }
}
