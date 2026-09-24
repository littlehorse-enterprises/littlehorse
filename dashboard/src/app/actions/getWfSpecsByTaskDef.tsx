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
  // Strip the raw Uint8Array bookmark: server actions can only return plain
  // objects to client components, so only its base64 form crosses the boundary.
  const { bookmark, ...wfSpecsRest } = wfSpecs
  return {
    ...wfSpecsRest,
    bookmarkAsString: bookmark ? Buffer.from(bookmark).toString('base64') : undefined,
  }
}
