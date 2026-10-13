'use server'

import { lhClient } from '@/app/lhClient'
import { isResourceExhausted } from 'littlehorse-client'
import { NodeRun, TaskRun, ThreadRun, Variable, VariableValue, WfRun, WfRunId, WfSpec } from 'littlehorse-client/proto'
import { buildNodeOutputValuesFromNodeRuns, getConditionTaskRunIds } from '@/app/utils/taskRunOutput'
import { getInheritedVariables } from './getInheritedVariables'

type Props = {
  wfRunId: WfRunId
  tenantId: string
  includeNodeOutputs?: boolean
}

const TASK_RUN_FETCH_CONCURRENCY = 10

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
export const getWfRun = async ({ wfRunId, tenantId, includeNodeOutputs = true }: Props): Promise<WfRunResponse> => {
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

  const taskRunIds = includeNodeOutputs ? getConditionTaskRunIds(wfSpec, nodeRuns) : []
  const taskRunsByGuid = new Map<string, TaskRun>()
  // Outputs enrich the diagram; a missing or unavailable task must not hide the workflow.
  for (let i = 0; i < taskRunIds.length; i += TASK_RUN_FETCH_CONCURRENCY) {
    await Promise.all(
      taskRunIds.slice(i, i + TASK_RUN_FETCH_CONCURRENCY).map(async id => {
        try {
          taskRunsByGuid.set(id.taskGuid, await client.getTaskRun(id))
        } catch (error) {
          console.error('Error getting task output for diagram', id, error)
        }
      })
    )
  }

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
