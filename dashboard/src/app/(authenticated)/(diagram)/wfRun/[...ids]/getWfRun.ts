'use server'

import { lhClient } from '@/app/lhClient'
import { NodeRun } from 'littlehorse-client/dist/proto/node_run'
import { WfRunId } from 'littlehorse-client/dist/proto/object_id'
import { Variable } from 'littlehorse-client/dist/proto/variable'
import { ThreadRun, WfRun } from 'littlehorse-client/dist/proto/wf_run'
import { WfSpec } from 'littlehorse-client/dist/proto/wf_spec'
import { cookies } from 'next/headers'

type Props = {
  ids: string[]
}

export type ThreadRunWithNodeRuns = ThreadRun & { nodeRuns: NodeRun[] }

export type WfRunResponse = {
  wfRun: WfRun & { threadRuns: ThreadRunWithNodeRuns[] }
  wfSpec: WfSpec
  nodeRuns: NodeRun[]
  variables: Variable[]
}
export const getWfRun = async ({ ids }: Props): Promise<WfRunResponse> => {
  const tenantId = cookies().get('tenantId')?.value
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
