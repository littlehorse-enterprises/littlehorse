'use server'
import { lhClient } from '@/app/lhClient'
import { WithTenant } from '@/types'
import {
  NodeRun,
  SearchUserTaskRunRequest,
  UserTaskRun,
  UserTaskRunId,
  UserTaskRunIdList,
} from 'littlehorse-client/proto'

export interface runDetails {
  userTaskRun: UserTaskRun
  nodeRun: NodeRun
}
export interface PaginatedUserTaskRunList extends UserTaskRunIdList {
  resultsWithDetails: runDetails[]
  bookmarkAsString: string | undefined
}

type WithBookmarkAsString = {
  bookmarkAsString: string | undefined
}

export type UserTaskRunSearchProps = SearchUserTaskRunRequest & WithTenant & WithBookmarkAsString
export const searchUserTaskRun = async ({
  tenantId,
  bookmarkAsString,
  ...req
}: UserTaskRunSearchProps): Promise<PaginatedUserTaskRunList> => {
  const client = await lhClient({ tenantId })
  const requestWithBookmark = bookmarkAsString ? { ...req, bookmark: Buffer.from(bookmarkAsString, 'base64') } : req
  const userTaskRunIdList: UserTaskRunIdList = await client.searchUserTaskRun(requestWithBookmark)
  const hydrateWithUserTaskRunDetails = (): Promise<runDetails>[] => {
    return userTaskRunIdList.results.map(async (userTaskRunId: UserTaskRunId) => {
      const userTaskRun = await client.getUserTaskRun({
        wfRunId: userTaskRunId.wfRunId,
        userTaskGuid: userTaskRunId.userTaskGuid,
      })
      const nodeRun = await client.getNodeRun(userTaskRun.nodeRunId!)

      return {
        userTaskRun,
        nodeRun,
      }
    })
  }

  const userTaskRunWithDetails: runDetails[] = await Promise.all(hydrateWithUserTaskRunDetails())

  return {
    ...userTaskRunIdList,
    bookmarkAsString: userTaskRunIdList.bookmark?.toString('base64'),
    resultsWithDetails: userTaskRunWithDetails,
  }
}
