import { QuotaId } from 'littlehorse-client/proto'
import { getApplicableQuotas } from './getApplicableQuotas'

jest.mock('@/app/lhClient', () => ({
  lhClient: jest.fn(),
}))

import { lhClient } from '@/app/lhClient'

const mockLhClient = lhClient as jest.MockedFunction<typeof lhClient>

const tenantQuotaId: QuotaId = { tenant: { id: 'acme' } }
const principalQuotaId: QuotaId = { tenant: { id: 'acme' }, principal: { id: 'alice' } }

const grpcError = (code: string) => Object.assign(new Error(code), { code })

const quota = (id: QuotaId, writeRequestsPerSecond: number) => ({ id, writeRequestsPerSecond })

function mockClient(principalId: string | undefined, quotas: ReturnType<typeof quota>[], failWith?: Error) {
  const getQuota = jest.fn(async (id: QuotaId) => {
    if (failWith) throw failWith
    const found = quotas.find(q => JSON.stringify(q.id) === JSON.stringify(id))
    if (!found) throw grpcError('NOT_FOUND')
    return found
  })
  mockLhClient.mockResolvedValue({
    whoami: jest.fn().mockResolvedValue({ id: principalId ? { id: principalId } : undefined }),
    getQuota,
  } as any)
  return getQuota
}

describe('getApplicableQuotas', () => {
  it('returns the tenant quota and the principal quota when both exist, tenant first', async () => {
    const tenantQuota = quota(tenantQuotaId, 2)
    const principalQuota = quota(principalQuotaId, 50)
    mockClient('alice', [tenantQuota, principalQuota])
    expect(await getApplicableQuotas({ tenantId: 'acme' })).toEqual([
      { scope: 'tenant', quotaId: tenantQuotaId, quota: tenantQuota },
      { scope: 'principal', quotaId: principalQuotaId, quota: principalQuota },
    ])
  })

  it('returns only the principal quota when the tenant has none', async () => {
    const principalQuota = quota(principalQuotaId, 50)
    mockClient('alice', [principalQuota])
    expect(await getApplicableQuotas({ tenantId: 'acme' })).toEqual([
      { scope: 'principal', quotaId: principalQuotaId, quota: principalQuota },
    ])
  })

  it('returns only the tenant quota when the principal has none', async () => {
    const tenantQuota = quota(tenantQuotaId, 2)
    const getQuota = mockClient('alice', [tenantQuota])
    expect(await getApplicableQuotas({ tenantId: 'acme' })).toEqual([
      { scope: 'tenant', quotaId: tenantQuotaId, quota: tenantQuota },
    ])
    expect(getQuota).toHaveBeenCalledTimes(2)
  })

  it('returns nothing when neither exists', async () => {
    mockClient('alice', [])
    expect(await getApplicableQuotas({ tenantId: 'acme' })).toEqual([])
  })

  it('skips the principal lookup when whoami has no id', async () => {
    const tenantQuota = quota(tenantQuotaId, 2)
    const getQuota = mockClient(undefined, [tenantQuota])
    expect(await getApplicableQuotas({ tenantId: 'acme' })).toEqual([
      { scope: 'tenant', quotaId: tenantQuotaId, quota: tenantQuota },
    ])
    expect(getQuota).toHaveBeenCalledTimes(1)
    expect(getQuota).toHaveBeenCalledWith(tenantQuotaId)
  })

  it('rethrows errors other than NOT_FOUND', async () => {
    mockClient('alice', [], grpcError('PERMISSION_DENIED'))
    await expect(getApplicableQuotas({ tenantId: 'acme' })).rejects.toMatchObject({ code: 'PERMISSION_DENIED' })
  })
})
