'use server'
import { lhClient } from '@/app/lhClient'
import { WithTenant } from '@/types'
import { SearchWfRunRequest } from 'littlehorse-client/dist/proto/service'

export type WfRunSearchProps = SearchWfRunRequest & WithTenant
export const searchWfRun = async ({ tenantId, ...req }: WfRunSearchProps) => {
  const client = await lhClient({ tenantId })
  return client.searchWfRun(req)
}
