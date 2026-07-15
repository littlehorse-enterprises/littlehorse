'use server'
import { lhClient } from '@/app/lhClient'
import { WithTenant } from '@/types'
import { NodeRun, SearchTaskRunRequest, TaskRun, TaskRunId, TaskRunIdList } from 'littlehorse-client/proto'

interface runDetails {
  taskRun: TaskRun
  nodeRun?: NodeRun
}
export interface PaginatedTaskRunList extends TaskRunIdList {
  resultsWithDetails: runDetails[]
  bookmarkAsString: string | undefined
}

type WithBookmarkAsString = {
  bookmarkAsString: string | undefined
}

export type TaskRunSearchProps = SearchTaskRunRequest & WithTenant & WithBookmarkAsString
export const searchTaskRun = async ({
  tenantId,
  bookmarkAsString,
  ...req
}: TaskRunSearchProps): Promise<PaginatedTaskRunList> => {
  const client = await lhClient({ tenantId })
  const requestWithBookmark = bookmarkAsString ? { ...req, bookmark: Buffer.from(bookmarkAsString, 'base64') } : req
  const taskRunIdList: TaskRunIdList = await client.searchTaskRun(requestWithBookmark)
  const hydrateWithTaskRunDetails = (): Promise<runDetails>[] => {
    return taskRunIdList.results.map(async (taskRunId: TaskRunId) => {
      const taskRun = await client.getTaskRun({
        wfRunId: taskRunId.wfRunId,
        taskGuid: taskRunId.taskGuid,
      })

      const taskRunSource = taskRun.source?.taskRunSource
      const nodeRunId =
        taskRunSource?.oneofKind === 'taskNode'
          ? taskRunSource.taskNode.nodeRunId
          : taskRunSource?.oneofKind === 'userTaskTrigger'
            ? taskRunSource.userTaskTrigger.nodeRunId
            : undefined

      let nodeRun: NodeRun | undefined
      if (nodeRunId) {
        try {
          nodeRun = await client.getNodeRun(nodeRunId)
        } catch {
          nodeRun = undefined
        }
      }

      return {
        taskRun,
        nodeRun,
      }
    })
  }

  const taskRunWithDetails: runDetails[] = await Promise.all(hydrateWithTaskRunDetails())

  return {
    ...taskRunIdList,
    bookmarkAsString: taskRunIdList.bookmark ? Buffer.from(taskRunIdList.bookmark).toString('base64') : undefined,
    resultsWithDetails: taskRunWithDetails,
  }
}
