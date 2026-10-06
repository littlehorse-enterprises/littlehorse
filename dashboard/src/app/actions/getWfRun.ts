'use server'

import { lhClient } from '@/app/lhClient'
import { isResourceExhausted } from 'littlehorse-client'
import {
  NodeRun,
  TaskRun,
  TaskRunId,
  ThreadRun,
  Variable,
  VariableValue,
  WfRun,
  WfRunId,
  WfSpec,
} from 'littlehorse-client/proto'
import { buildNodeOutputValuesFromNodeRuns } from '@/app/utils/taskRunOutput'
import { getInheritedVariables } from './getInheritedVariables'

type Props = {
  wfRunId: WfRunId
  tenantId: string
}

export type ThreadRunWithNodeRuns = ThreadRun & {
  nodeRuns: NodeRun[]
  nodeOutputValues?: Record<string, VariableValue>
}

export type WfRunResponse = {
  wfRun: WfRun & { threadRuns: ThreadRunWithNodeRuns[] }
  wfSpec: WfSpec
  variables: Variable[]
  variablesTooLarge: boolean
}
export const getWfRun = async ({ wfRunId, tenantId }: Props): Promise<WfRunResponse> => {
  const client = await lhClient({ tenantId })
  const wfRun = await client.getWfRun(wfRunId)
  const [wfSpec, { results: nodeRuns }, ownVariables] = await Promise.all([
    client.getWfSpec(wfRun.wfSpecId!),
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

  const taskRunIds = nodeRuns
    .map(nodeRun => (nodeRun.nodeType.oneofKind === 'task' ? nodeRun.nodeType.task.taskRunId : undefined))
    .filter((id): id is TaskRunId => id !== undefined)
  const taskRunsByGuid = new Map<string, TaskRun>(
    await Promise.all(taskRunIds.map(async id => [id.taskGuid, await client.getTaskRun(id)] as const))
  )

  const threadRuns = wfRun.threadRuns.map(threadRun => mergeThreadRunsWithNodeRuns(threadRun, nodeRuns, taskRunsByGuid))
  return {
    wfRun: { ...wfRun, threadRuns },
    wfSpec,
    variables: [...ownVariables.results, ...inheritedVariables.variables],
    variablesTooLarge: ownVariables.tooLarge || inheritedVariables.tooLarge,
  }
}

const mergeThreadRunsWithNodeRuns = (
  threadRun: ThreadRun,
  nodeRuns: NodeRun[],
  taskRunsByGuid: Map<string, TaskRun>
): ThreadRunWithNodeRuns => {
  const threadNodeRuns = nodeRuns.filter(
    nodeRun => nodeRun.threadSpecName === threadRun.threadSpecName && nodeRun.id?.threadRunNumber === threadRun.number
  )
  return {
    ...threadRun,
    nodeRuns: threadNodeRuns,
    nodeOutputValues: buildNodeOutputValuesFromNodeRuns(threadNodeRuns, taskRunsByGuid),
  }
}
