'use server'

import { lhClient } from '@/app/lhClient'
import { buildNodeOutputValuesFromNodeRuns } from '@/app/utils/taskRunOutput'
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
import { getInheritedVariables } from './getInheritedVariables'

type Props = {
  wfRunId: WfRunId
  tenantId: string
}

export type ThreadRunWithNodeRuns = ThreadRun & {
  nodeRuns: NodeRun[]
  /** Resolved TaskRun outputs keyed by WfSpec node name (for edge condition labels). */
  nodeOutputValues?: Record<string, VariableValue>
}

export type WfRunResponse = {
  wfRun: WfRun & { threadRuns: ThreadRunWithNodeRuns[] }
  wfSpec: WfSpec
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

  const entrypointThreadRun =
    wfRun.threadRuns.find(tr => tr.number === 0) ??
    wfRun.threadRuns.find(tr => tr.threadSpecName === wfSpec.entrypointThreadName)
  const inheritedVariables = entrypointThreadRun
    ? await getInheritedVariables(
        wfRunId,
        wfSpec.threadSpecs[entrypointThreadRun.threadSpecName].variableDefs,
        tenantId
      )
    : []

  const taskRunIds = nodeRuns
    .map(nr => (nr.nodeType?.$case === 'task' ? nr.nodeType.value.taskRunId : undefined))
    .filter((id): id is TaskRunId => id != null)

  const taskRunsByGuid = new Map(
    (
      await Promise.all(
        taskRunIds.map(async id => {
          const taskRun = await client.getTaskRun(id)
          return [id.taskGuid, taskRun] as const
        })
      )
    ).map(([guid, taskRun]) => [guid, taskRun])
  )

  const threadRuns = wfRun.threadRuns.map(threadRun => mergeThreadRunsWithNodeRuns(threadRun, nodeRuns, taskRunsByGuid))
  return { wfRun: { ...wfRun, threadRuns }, wfSpec, variables: [...variables, ...inheritedVariables] }
}

const mergeThreadRunsWithNodeRuns = (
  threadRun: ThreadRun,
  nodeRuns: NodeRun[],
  taskRunsByGuid: Map<string, TaskRun>
): ThreadRunWithNodeRuns => {
  const filtered = nodeRuns.filter(
    nodeRun => nodeRun.threadSpecName === threadRun.threadSpecName && nodeRun.id?.threadRunNumber === threadRun.number
  )
  return {
    ...threadRun,
    nodeRuns: filtered,
    nodeOutputValues: buildNodeOutputValuesFromNodeRuns(filtered, taskRunsByGuid),
  }
}
