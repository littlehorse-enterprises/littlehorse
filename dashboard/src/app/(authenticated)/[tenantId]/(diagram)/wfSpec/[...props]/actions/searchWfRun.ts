'use server'
import { getWfRun, WfRunResponse } from '@/app/actions/getWfRun'
import { lhClient } from '@/app/lhClient'
import { WithTenant } from '@/types'
import { SearchWfRunRequest, WfRunIdList } from 'littlehorse-client/proto'

export interface PaginatedWfRunIdList extends WfRunIdList {
  bookmarkAsString: string | undefined
}

export type PaginatedWfRunResponseList = {
  results: WfRunResponse[]
  bookmarkAsString: string | undefined
}

type WithBookmarkAsString = {
  bookmarkAsString: string | undefined
}

export type WfRunSearchProps = SearchWfRunRequest & WithTenant & WithBookmarkAsString
export const searchWfRun = async ({
  tenantId,
  bookmarkAsString,
  ...req
}: WfRunSearchProps): Promise<PaginatedWfRunResponseList> => {
  const client = await lhClient({ tenantId })
  const requestWithBookmark = bookmarkAsString ? { ...req, bookmark: Buffer.from(bookmarkAsString, 'base64') } : req
  const wfRunIdList = await client.searchWfRun(requestWithBookmark)

  const wfRunData = await Promise.all(wfRunIdList.results.map(wfRunId => getWfRun({ wfRunId, tenantId })))

  return {
    results: wfRunData,
    bookmarkAsString: wfRunIdList.bookmark?.toString('base64'),
  }
}
