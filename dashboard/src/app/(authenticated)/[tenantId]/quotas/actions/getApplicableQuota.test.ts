import { QuotaId } from 'littlehorse-client/proto'
import { getApplicableQuota } from './getApplicableQuota'

jest.mock('@/app/lhClient', () => ({
  lhClient: jest.fn(),
}))

import { lhClient } from '@/app/lhClient'

const mockLhClient = lhClient as jest.MockedFunction<typeof lhClient>

const tenantQuotaId: QuotaId = { tenant: { id: 'acme' } }
const principalQuotaId: QuotaId = { tenant: { id: 'acme' }, principal: { id: 'alice' } }

const grpcError = (code: string) => Object.assign(new Error(code), { code })

const quota = (id: QuotaId) => ({ id, writeRequestsPerSecond: 10 })

function mockClient(principalId: string | undefined, quotas: QuotaId[], failWith?: Error) {
  const getQuota = jest.fn(async (id: QuotaId) => {
    if (failWith) throw failWith
    const found = quotas.find(q => JSON.stringify(q) === JSON.stringify(id))
    if (!found) throw grpcError('NOT_FOUND')
    return quota(found)
  })
  mockLhClient.mockResolvedValue({
    whoami: jest.fn().mockResolvedValue({ id: principalId ? { id: principalId } : undefined }),
    getQuota,
  } as any)
  return getQuota
}

describe('getApplicableQuota', () => {
  it('prefers the principal quota', async () => {
    mockClient('alice', [tenantQuotaId, principalQuotaId])
    const result = await getApplicableQuota({ tenantId: 'acme' })
    expect(result).toEqual({ scope: 'principal', quota: quota(principalQuotaId), principalId: 'alice' })
  })

  it('falls back to the tenant quota when the principal has none', async () => {
    const getQuota = mockClient('alice', [tenantQuotaId])
    const result = await getApplicableQuota({ tenantId: 'acme' })
    expect(result).toEqual({ scope: 'tenant', quota: quota(tenantQuotaId), principalId: 'alice' })
    expect(getQuota).toHaveBeenCalledTimes(2)
  })

  it('reports no quota when neither exists', async () => {
    mockClient('alice', [])
    expect(await getApplicableQuota({ tenantId: 'acme' })).toEqual({ scope: 'none', principalId: 'alice' })
  })

  it('skips the principal lookup when whoami has no id', async () => {
    const getQuota = mockClient(undefined, [tenantQuotaId])
    const result = await getApplicableQuota({ tenantId: 'acme' })
    expect(result).toEqual({ scope: 'tenant', quota: quota(tenantQuotaId), principalId: undefined })
    expect(getQuota).toHaveBeenCalledTimes(1)
    expect(getQuota).toHaveBeenCalledWith(tenantQuotaId)
  })

  it('rethrows errors other than NOT_FOUND', async () => {
    mockClient('alice', [], grpcError('PERMISSION_DENIED'))
    await expect(getApplicableQuota({ tenantId: 'acme' })).rejects.toMatchObject({ code: 'PERMISSION_DENIED' })
  })
})
