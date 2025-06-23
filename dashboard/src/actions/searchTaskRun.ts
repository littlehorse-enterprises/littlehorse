'use server'

import { LHMethodParamType } from '@/types/executeRPCTypes'
import { WithBookmark, WithTenant } from '@/types/withs'
import { TaskRunId } from 'littlehorse-client/proto'
import { executeRpc } from './executeRPC'

export interface SearchTaskRunResponse {
  results: TaskRunId[]
  bookmark?: string
}

export const searchTaskRun = async ({
  taskDefName,
  tenantId,
  bookmark,
  limit,
}: Omit<LHMethodParamType<'searchTaskRun'>, 'bookmark'> &
  WithBookmark &
  WithTenant): Promise<SearchTaskRunResponse> => {
  const results = await executeRpc(
    'searchTaskRun',
    {
      taskDefName,
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
