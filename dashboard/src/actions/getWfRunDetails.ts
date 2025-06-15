'use server'

import { NodeRun, ThreadRun, WfRunId } from 'littlehorse-client/proto'
import { lhClient } from '@/utils/client/lhClient'
import { WfRunDetails, ThreadRunWithNodeRuns } from '@/types/wfRunDetails'

type Props = {
  wfRunId: WfRunId
  tenantId: string
}

export const getWfRunDetails = async ({ wfRunId, tenantId }: Props): Promise<WfRunDetails> => {
  const client = await lhClient(tenantId)

  // Get the workflow run first
  const wfRun = await client.getWfRun(wfRunId)

  // Fetch related data in parallel
  const [{ results: nodeRuns }, { results: variables }] = await Promise.all([
    client.listNodeRuns({
      wfRunId,
    }),
    client.listVariables({
      wfRunId,
    }),
  ])

  // Merge thread runs with their corresponding node runs
  const threadRuns = wfRun.threadRuns.map((threadRun: ThreadRun) => mergeThreadRunsWithNodeRuns(threadRun, nodeRuns))

  const taskRuns = await Promise.all(
    nodeRuns
      .filter(nodeRun => nodeRun.task?.taskRunId?.taskGuid)
      .map(async (nodeRun: NodeRun) => {
        return await client.getTaskRun({
          wfRunId,
          taskGuid: nodeRun.task!.taskRunId!.taskGuid,
        })
      })
  )

  return {
    wfRun: { ...wfRun, threadRuns },
    nodeRuns,
    variables,
    taskRuns,
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
