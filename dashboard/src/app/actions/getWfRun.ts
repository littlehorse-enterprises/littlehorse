'use server'

import { lhClient } from '@/app/lhClient'
import { WorkflowDefinition } from '@/types'
import { isResourceExhausted } from 'littlehorse-client'
import { NodeRun, ThreadRun, Variable, WfRun, WfRunId } from 'littlehorse-client/proto'
import { getInheritedVariables } from './getInheritedVariables'

type Props = {
  wfRunId: WfRunId
  tenantId: string
}

export type ThreadRunWithNodeRuns = ThreadRun & { nodeRuns: NodeRun[] }

export type WfRunResponse = {
  wfRun: WfRun & { threadRuns: ThreadRunWithNodeRuns[] }
  wfSpec: WorkflowDefinition
  variables: Variable[]
  variablesTooLarge: boolean
}
export const getWfRun = async ({ wfRunId, tenantId }: Props): Promise<WfRunResponse> => {
  const client = await lhClient({ tenantId })
  const wfRun = await client.getWfRun(wfRunId)
  const source = wfRun.wfSpecSource
  if (source.oneofKind === undefined || (source.oneofKind === 'isInline' && !source.isInline)) {
    throw new Error('WfRun has no valid workflow definition source')
  }
  const [wfSpec, { results: nodeRuns }, ownVariables] = await Promise.all([
    source.oneofKind === 'wfSpecId' ? client.getWfSpec(source.wfSpecId) : client.getInlineWfSpec(wfRunId),
    client.listNodeRuns({
      wfRunId,
    }),
    client.listVariables({ wfRunId }).then(
      ({ results }) => ({ results, tooLarge: false }),
      error => {
        if (isResourceExhausted(error)) return { results: [] as Variable[], tooLarge: true }
        throw error
      }
    ),
  ])

  const entrypointThreadRun =
    wfRun.threadRuns.find(tr => tr.number === 0) ??
    wfRun.threadRuns.find(tr => tr.threadSpecName === wfSpec.entrypointThreadName)
  const inheritedVariables = entrypointThreadRun
    ? await getInheritedVariables(
        wfRunId,
        wfSpec.threadSpecs[entrypointThreadRun.threadSpecName].variableDefs,
        tenantId
      )
    : { variables: [], tooLarge: false }

  const threadRuns = wfRun.threadRuns.map(threadRun => mergeThreadRunsWithNodeRuns(threadRun, nodeRuns))
  return {
    wfRun: { ...wfRun, threadRuns },
    wfSpec,
    variables: [...ownVariables.results, ...inheritedVariables.variables],
    variablesTooLarge: ownVariables.tooLarge || inheritedVariables.tooLarge,
  }
}

const mergeThreadRunsWithNodeRuns = (threadRun: ThreadRun, nodeRuns: NodeRun[]): ThreadRunWithNodeRuns => {
  return {
    ...threadRun,
    nodeRuns: nodeRuns.filter(
      nodeRun => nodeRun.threadSpecName === threadRun.threadSpecName && nodeRun.id?.threadRunNumber === threadRun.number
    ),
  }
}
