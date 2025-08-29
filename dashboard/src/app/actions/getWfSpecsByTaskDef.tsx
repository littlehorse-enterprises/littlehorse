'use server'
import { WithTenant } from '@/types'
import { SearchWfSpecRequest, WfSpecIdList } from 'littlehorse-client/proto'
import { lhClient } from '../lhClient'

export interface PaginatedWfSpecList extends WfSpecIdList {
  bookmarkAsString: string | undefined
}
type WithBookmarkAsString = {
  bookmarkAsString: string | undefined
}

export const searchWfSpecs = async ({
  tenantId,
  bookmarkAsString,
  limit,
  wfSpecCriteria,
}: SearchWfSpecRequest & WithBookmarkAsString & WithTenant): Promise<PaginatedWfSpecList> => {
  const client = await lhClient({ tenantId })
  const wfSpecs = await client.searchWfSpec({
    wfSpecCriteria,
    bookmark: bookmarkAsString ? Buffer.from(bookmarkAsString, 'base64') : undefined,
    limit,
  })
  return {
    ...wfSpecs,
    bookmarkAsString: wfSpecs.bookmark?.toString('base64'),
  }
}
