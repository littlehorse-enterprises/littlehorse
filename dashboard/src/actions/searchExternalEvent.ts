'use server'

import { LHMethodParamType } from '@/types'
import { WithBookmark, WithTenant } from '@/types'
import { ExternalEventId } from 'littlehorse-client/proto'
import { executeRpc } from './executeRPC'

export interface SearchExternalEventResponse {
  results: ExternalEventId[]
  bookmark?: string
}

export const searchExternalEvent = async ({
  externalEventDefId,
  tenantId,
  bookmark,
  limit,
}: Omit<LHMethodParamType<'searchExternalEvent'>, 'bookmark'> &
  WithBookmark &
  WithTenant): Promise<SearchExternalEventResponse> => {
  const results = await executeRpc(
    'searchExternalEvent',
    {
      externalEventDefId,
      limit,
      bookmark: bookmark ? Buffer.from(bookmark, 'base64') : undefined,
    },
    tenantId
  )

  return {
    results: results.results,
    bookmark: results.bookmark?.toString('base64'),
  }
}
