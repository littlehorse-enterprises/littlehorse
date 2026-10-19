'use server'

import { lhClient } from '@/app/lhClient'
import { WorkflowDefinition } from '@/types'
import { isResourceExhausted } from 'littlehorse-client'
import { NodeRun, TaskRun, ThreadRun, Variable, VariableValue, WfRun, WfRunId, WfSpec } from 'littlehorse-client/proto'
import { buildNodeOutputValuesFromNodeRuns, getConditionTaskRunIds } from '@/app/utils/taskRunOutput'
import { NodeRun, ThreadRun, Variable, WfRun, WfRunId } from 'littlehorse-client/proto'
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
  wfSpec: WorkflowDefinition
  variables: Variable[]
  variablesTooLarge: boolean
}
export const getWfRun = async ({ wfRunId, tenantId, includeNodeOutputs = true }: Props): Promise<WfRunResponse> => {
  const client = await lhClient({ tenantId })
  const wfRun = await client.getWfRun(wfRunId)
  const [wfSpec, nodeRuns, ownVariables] = await Promise.all([
    client.getWfSpec(wfRun.wfSpecId!),
    listAllNodeRuns(client, wfRunId),
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

const listAllNodeRuns = async (client: Awaited<ReturnType<typeof lhClient>>, wfRunId: WfRunId): Promise<NodeRun[]> => {
  const nodeRuns: NodeRun[] = []
  let bookmark: Uint8Array | undefined
  do {
    const page = await client.listNodeRuns({ wfRunId, bookmark })
    nodeRuns.push(...page.results)
    bookmark = page.bookmark
  } while (bookmark && bookmark.length > 0)
  return nodeRuns
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
