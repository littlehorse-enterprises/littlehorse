'use server'
import { lhClient } from '@/app/lhClient'
import { WithTenant } from '@/types'
import { NodeRun } from 'littlehorse-client/dist/proto/node_run'
import { TaskRunId } from 'littlehorse-client/dist/proto/object_id';
import { SearchTaskRunRequest, TaskRunIdList } from 'littlehorse-client/dist/proto/service'
import { TaskRun } from 'littlehorse-client/dist/proto/task_run'

export interface runDetails {
  taskRun: TaskRun
  nodeRun: NodeRun
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
      const nodeRun = await client.getNodeRun(taskRun.id!)

      return {
        taskRun,
        nodeRun,
      }
    })
  }

  const userTaskRunWithDetails: runDetails[] = await Promise.all(hydrateWithTaskRunDetails())

  return {
    ...taskRunIdList,
    bookmarkAsString: taskRunIdList.bookmark?.toString('base64'),
    resultsWithDetails: userTaskRunWithDetails,
  }
}
