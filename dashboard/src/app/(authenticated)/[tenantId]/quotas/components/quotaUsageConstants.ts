import { ChartConfig } from '@/components/ui/chart'
import { QuotaId } from 'littlehorse-client/proto'

export type QuotaViewMode = 'requests' | 'throttleTime'

export type QuotaOption = {
  key: string
  label: string
  quotaId: QuotaId
  limitRps?: number
}

export const TENANT_WIDE_QUOTA_KEY = 'tenant'

export const quotaOptionKey = (quotaId: QuotaId): string =>
  quotaId.principal ? `principal:${quotaId.principal.id}` : TENANT_WIDE_QUOTA_KEY

export const QUOTA_COUNT_CHART_CONFIG = {
  observed: { label: 'Observed', color: 'hsl(221, 83%, 53%)' },
  throttled: { label: 'Throttled', color: 'hsl(0, 84%, 60%)' },
} satisfies ChartConfig

export const QUOTA_THROTTLE_CHART_CONFIG = {
  avgThrottleMs: { label: 'Avg backoff', color: 'hsl(38, 92%, 50%)' },
} satisfies ChartConfig
