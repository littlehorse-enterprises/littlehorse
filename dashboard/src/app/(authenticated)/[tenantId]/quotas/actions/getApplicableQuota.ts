'use server'
import { lhClient } from '@/app/lhClient'
import { WithTenant } from '@/types'
import { Quota, QuotaId } from 'littlehorse-client/proto'
import { ClientError, Status } from 'nice-grpc-common'

export type QuotaScope = 'principal' | 'tenant' | 'none'

export type ApplicableQuota = {
  scope: QuotaScope
  quota?: Quota
  principalId?: string
}

type LhClient = Awaited<ReturnType<typeof lhClient>>

const getQuotaOrNull = async (client: LhClient, quotaId: QuotaId): Promise<Quota | null> => {
  try {
    return await client.getQuota(quotaId)
  } catch (error) {
    if (error instanceof ClientError && error.code === Status.NOT_FOUND) return null
    throw error
  }
}

/** Reads quotas by id rather than listing them, so a principal only ever sees the quota they are subject to. */
export const getApplicableQuota = async ({ tenantId }: WithTenant): Promise<ApplicableQuota> => {
  const client = await lhClient({ tenantId })
  const principalId = (await client.whoami({})).id?.id

  if (principalId) {
    const principalQuota = await getQuotaOrNull(client, { tenant: { id: tenantId }, principal: { id: principalId } })
    if (principalQuota) return { scope: 'principal', quota: principalQuota, principalId }
  }

  const tenantQuota = await getQuotaOrNull(client, { tenant: { id: tenantId } })
  if (tenantQuota) return { scope: 'tenant', quota: tenantQuota, principalId }

  return { scope: 'none', principalId }
}
