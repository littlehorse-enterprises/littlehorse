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

interface runDetails {
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

  // Strip the raw Uint8Array bookmark: server actions can only return plain
  // objects to client components, so only its base64 form crosses the boundary.
  const { bookmark, ...userTaskRunIdListRest } = userTaskRunIdList
  return {
    ...userTaskRunIdListRest,
    bookmarkAsString: bookmark ? Buffer.from(bookmark).toString('base64') : undefined,
    resultsWithDetails: userTaskRunWithDetails,
  }
}
