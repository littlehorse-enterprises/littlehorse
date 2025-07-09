'use server'

import { LHMethodParamType } from '@/types'
import { WithBookmark, WithTenant } from '@/types'
import { WorkflowEventId } from 'littlehorse-client/proto'
import { executeRpc } from './executeRPC'

export interface SearchWorkflowEventResponse {
  results: WorkflowEventId[]
  bookmark?: string
}

export const searchWorkflowEvent = async ({
  workflowEventDefId,
  tenantId,
  bookmark,
  limit,
}: Omit<LHMethodParamType<'searchWorkflowEvent'>, 'bookmark'> &
  WithBookmark &
  WithTenant): Promise<SearchWorkflowEventResponse> => {
  const results = await executeRpc(
    'searchWorkflowEvent',
    {
      workflowEventDefId,
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
