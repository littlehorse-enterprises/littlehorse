'use server'
import { lhClient } from '@/app/lhClient'
import { WithTenant } from '@/types'
import { Quota } from 'littlehorse-client/proto'

export type GetQuotasResult = {
  quotas: Quota[]
  quotasAvailable: boolean
}

export const getQuotas = async ({ tenantId }: WithTenant): Promise<GetQuotasResult> => {
  const client = await lhClient({ tenantId })
  try {
    const { results } = await client.searchQuota({ limit: 100, tenantId })
    const quotas = await Promise.all(results.map(id => client.getQuota(id)))
    return { quotas, quotasAvailable: true }
  } catch {
    // Usage metrics only need ACL_WORKFLOW READ, but enumerating quota definitions
    // needs ACL_QUOTA READ; degrade to tenant-wide usage without limits.
    return { quotas: [], quotasAvailable: false }
  }
}
