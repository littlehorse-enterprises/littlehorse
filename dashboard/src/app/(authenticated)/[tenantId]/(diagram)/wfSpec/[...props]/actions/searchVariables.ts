'use server'
import { lhClient } from '@/app/lhClient'
import { WithBookmark, WithTenant } from '@/types'
import { SearchVariableRequest, VariableIdList } from 'littlehorse-client/proto'

export type VariableSearchProps = Omit<SearchVariableRequest, 'bookmark'> & WithTenant & WithBookmark
export const searchVariables = async ({
  tenantId,
  bookmark,
  ...req
}: VariableSearchProps): Promise<Omit<VariableIdList, 'bookmark'> & WithBookmark> => {
  const client = await lhClient({ tenantId })
  const requestWithBookmark = bookmark ? { ...req, bookmark: Buffer.from(bookmark, 'base64') } : req
  const variableIdList = await client.searchVariable(requestWithBookmark)
  return {
    ...variableIdList,
    bookmark: variableIdList.bookmark?.toString('base64'),
  }
}
