'use server'

import { LHMethodParamType } from '@/types/executeRPCTypes'
import { WithBookmark, WithTenant } from '@/types/withs'
import { UserTaskRunId } from 'littlehorse-client/proto'
import { executeRpc } from './executeRPC'

export interface SearchUserTaskRunResponse {
  results: UserTaskRunId[]
  bookmark?: string
}

export const searchUserTaskRun = async ({
  userTaskDefName,
  tenantId,
  bookmark,
  limit,
}: Omit<LHMethodParamType<'searchUserTaskRun'>, 'bookmark'> &
  WithBookmark &
  WithTenant): Promise<SearchUserTaskRunResponse> => {
  const results = await executeRpc(
    'searchUserTaskRun',
    {
      userTaskDefName,
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
