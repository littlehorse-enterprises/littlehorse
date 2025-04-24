'use server'
import { WithTenant } from '@/types'
import { lhClient } from '../lhClient'
import { SearchWfSpecRequest, WfSpecIdList } from 'littlehorse-client/proto'

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
  taskDefName,
}: SearchWfSpecRequest & WithBookmarkAsString & WithTenant): Promise<PaginatedWfSpecList> => {
  const client = await lhClient({ tenantId })
  const wfSpecs = await client.searchWfSpec({
    taskDefName,
    bookmark: bookmarkAsString ? Buffer.from(bookmarkAsString, 'base64') : undefined,
    limit,
  })
  return {
    ...wfSpecs,
    bookmarkAsString: wfSpecs.bookmark?.toString('base64'),
  }
}
