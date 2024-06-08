'use server'
import { lhClient } from '@/app/lhClient'
import { WithTenant } from '@/types'
import { SearchVariableRequest, VariableIdList } from 'littlehorse-client/dist/proto/service'

export interface PaginatedVariableIdList extends VariableIdList {
  bookmarkAsString: string | undefined
}

type WithBookmarkAsString = {
  bookmarkAsString: string | undefined
}

export type VariableSearchProps = SearchVariableRequest & WithTenant & WithBookmarkAsString
export const searchVariables = async ({
  tenantId,
  bookmarkAsString,
  ...req
}: VariableSearchProps): Promise<PaginatedVariableIdList> => {
  console.log(req)
  const client = await lhClient({ tenantId })
  const requestWithBookmark = bookmarkAsString ? { ...req, bookmark: Buffer.from(bookmarkAsString, 'base64') } : req
  const variableIdList = await client.searchVariable(requestWithBookmark)
  return {
    ...variableIdList,
    bookmarkAsString: variableIdList.bookmark?.toString('base64'),
  }
}
