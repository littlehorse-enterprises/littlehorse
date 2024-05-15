'use server'
import { lhClient } from '@/app/lhClient'
import { WithTenant } from '@/types'
import {
  SearchUserTaskRunRequest,
  UserTaskRunIdList,
} from 'littlehorse-client/dist/proto/service'
import { UserTaskRun } from 'littlehorse-client/dist/proto/user_tasks'
import { UserTaskRunId } from 'littlehorse-client/dist/proto/object_id'

export interface PaginatedUserTaskRunList extends UserTaskRunIdList {
  resultsWithDetails: UserTaskRun[]
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
  const hydrateWithUserTaskRunDetails = () => {
    return userTaskRunIdList.results.map(async (userTaskRunId: UserTaskRunId) => {
      return await client.getUserTaskRun({ wfRunId: userTaskRunId.wfRunId, userTaskGuid: userTaskRunId.userTaskGuid })
    })
  }

  const userTaskRunWithDetails: UserTaskRun[] = await Promise.all(hydrateWithUserTaskRunDetails())

  return {
    ...userTaskRunIdList,
    bookmarkAsString: userTaskRunIdList.bookmark?.toString('base64'),
    resultsWithDetails: userTaskRunWithDetails,
  }
}
