'use server'
import { lhClient } from '@/app/lhClient'
import { WithTenant } from '@/types'
import { Quota, QuotaId } from 'littlehorse-client/proto'

export type QuotaScope = 'tenant' | 'principal'

export type ApplicableQuota = {
  scope: QuotaScope
  quotaId: QuotaId
  quota: Quota
}

type LhClient = Awaited<ReturnType<typeof lhClient>>

// The protobuf-ts client throws RpcError with string status codes; nice-grpc's ClientError never matches it.
const isNotFound = (error: unknown): boolean =>
  typeof error === 'object' && error !== null && 'code' in error && error.code === 'NOT_FOUND'

const lookup = async (client: LhClient, scope: QuotaScope, quotaId: QuotaId): Promise<ApplicableQuota | null> => {
  try {
    return { scope, quotaId, quota: await client.getQuota(quotaId) }
  } catch (error) {
    if (isNotFound(error)) return null
    throw error
  }
}

// The server enforces the tenant-wide quota and the principal's own quota at the same time, so both are returned.
export const getApplicableQuotas = async ({ tenantId }: WithTenant): Promise<ApplicableQuota[]> => {
  const client = await lhClient({ tenantId })
  const principalId = (await client.whoami({})).id?.id
  const tenant = { id: tenantId }
  const lookups = [lookup(client, 'tenant', { tenant })]
  if (principalId) lookups.push(lookup(client, 'principal', { tenant, principal: { id: principalId } }))
  return (await Promise.all(lookups)).filter((found): found is ApplicableQuota => found !== null)
}
