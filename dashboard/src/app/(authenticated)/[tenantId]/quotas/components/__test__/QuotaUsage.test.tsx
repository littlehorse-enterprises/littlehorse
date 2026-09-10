import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { render, screen } from '@testing-library/react'
import { MetricWindow, QuotaId, Timestamp } from 'littlehorse-client/proto'
import useSWR from 'swr'
import { ApplicableQuota } from '../../actions/getApplicableQuotas'
import { QuotaUsage } from '../QuotaUsage'

jest.mock('swr', () => ({ __esModule: true, default: jest.fn() }))
jest.mock('@/contexts/WhoAmIContext', () => ({ useWhoAmI: jest.fn() }))
jest.mock('../../actions/getQuotaUsageMetrics', () => ({ getQuotaUsageMetrics: jest.fn() }))
jest.mock('@/app/(authenticated)/[tenantId]/components/Navigation', () => ({ Navigation: () => null }))
jest.mock('../QuotaUsageChart', () => ({ QuotaUsageChart: () => <div data-testid="chart" /> }))

const useSWRMock = useSWR as jest.MockedFunction<typeof useSWR>
const useWhoAmIMock = useWhoAmI as jest.MockedFunction<typeof useWhoAmI>

const tenantQuotaId: QuotaId = { tenant: { id: 'acme' } }
const principalQuotaId: QuotaId = { tenant: { id: 'acme' }, principal: { id: 'alice' } }

const tenantQuota: ApplicableQuota = {
  scope: 'tenant',
  quotaId: tenantQuotaId,
  quota: { id: tenantQuotaId, writeRequestsPerSecond: 2 },
}
const principalQuota: ApplicableQuota = {
  scope: 'principal',
  quotaId: principalQuotaId,
  quota: { id: principalQuotaId, writeRequestsPerSecond: 50 },
}

const rangeStartMs = Date.parse('2026-09-08T19:00:00Z')
const rangeEndMs = Date.parse('2026-09-08T20:00:00Z')

function window(observed: number, throttled: number, totalThrottleMs: number): MetricWindow {
  return {
    id: {
      windowStart: Timestamp.fromDate(new Date('2026-09-08T19:26:00Z')),
      id: { oneofKind: undefined },
      tenantId: undefined,
    },
    metric: {
      oneofKind: 'quotaUsage',
      quotaUsage: {
        requestsObserved: observed,
        requestsThrottled: throttled,
        totalThrottleTimeMs: String(totalThrottleMs),
      },
    },
  }
}

const tileValues = (label: string) => screen.getAllByText(label).map(tile => tile.nextElementSibling?.textContent)

describe('QuotaUsage', () => {
  beforeEach(() => {
    jest.clearAllMocks()
    useWhoAmIMock.mockReturnValue({ tenantId: 'acme' } as any)
    useSWRMock.mockImplementation(((key: [string, string, QuotaId, string]) => {
      const windows = key[2].principal ? [window(8, 0, 0)] : [window(8, 7, 3500)]
      return { data: { result: { windows }, rangeStartMs, rangeEndMs }, error: undefined, isLoading: false }
    }) as any)
  })

  it('explains that nothing is recorded when no quota applies', () => {
    render(<QuotaUsage quotas={[]} />)
    expect(screen.getByText(/No quota applies to you in this tenant/)).toBeInTheDocument()
    expect(screen.queryByText('Requests observed')).not.toBeInTheDocument()
  })

  it("shows one card per quota with each quota's own throttling", () => {
    render(<QuotaUsage quotas={[tenantQuota, principalQuota]} />)

    expect(screen.getByText('Tenant-wide quota')).toBeInTheDocument()
    expect(screen.getByText('Principal quota · alice')).toBeInTheDocument()
    expect(screen.getByText('2 writes/s')).toBeInTheDocument()
    expect(screen.getByText('50 writes/s')).toBeInTheDocument()

    expect(tileValues('Requests observed')).toEqual(['8', '8'])
    expect(tileValues('Requests throttled')).toEqual(['7', '0'])
    expect(tileValues('Throttle rate')).toEqual(['87.5%', '0.0%'])
    expect(tileValues('Time throttled')).toEqual(['3.5s', '0ms'])
    expect(screen.getAllByTestId('chart')).toHaveLength(2)
  })

  it('fetches metrics per quota id', () => {
    render(<QuotaUsage quotas={[tenantQuota, principalQuota]} />)
    const keys = useSWRMock.mock.calls.map(call => call[0] as [string, string, QuotaId, string])
    const quotaIds = [...new Set(keys.map(key => JSON.stringify(key[2])))].map(id => JSON.parse(id))
    expect(quotaIds).toEqual([tenantQuotaId, principalQuotaId])
    expect(keys.every(key => key[0] === 'quotaUsageMetrics' && key[1] === 'acme' && key[3] === '60')).toBe(true)
  })
})
