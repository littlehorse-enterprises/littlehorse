'use server'
import { lhClient } from '@/app/lhClient'
import { WithTenant } from '@/types'
import { Quota } from 'littlehorse-client/proto'

export type QuotaScope = 'principal' | 'tenant' | 'none'

export type ApplicableQuota = {
  scope: QuotaScope
  quota?: Quota
  principalId?: string
}

/**
 * Resolves the quota governing the signed-in principal: their own quota when one is
 * configured, otherwise the tenant-wide quota. Reads each quota by id rather than
 * listing them, so a principal only ever sees the quota they are subject to.
 */
export const getApplicableQuota = async ({ tenantId }: WithTenant): Promise<ApplicableQuota> => {
  const client = await lhClient({ tenantId })

  let principalId: string | undefined
  try {
    principalId = (await client.whoami({})).id?.id
  } catch {
    principalId = undefined
  }

  if (principalId) {
    try {
      const quota = await client.getQuota({ tenant: { id: tenantId }, principal: { id: principalId } })
      return { scope: 'principal', quota, principalId }
    } catch {
      // no principal-scoped quota, fall back to the tenant-wide one
    }
  }

  try {
    const quota = await client.getQuota({ tenant: { id: tenantId } })
    return { scope: 'tenant', quota, principalId }
  } catch {
    return { scope: 'none', principalId }
  }
}
