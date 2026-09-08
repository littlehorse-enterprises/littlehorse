import { ChartConfig } from '@/components/ui/chart'

export type QuotaViewMode = 'requests' | 'throttleTime'

export const QUOTA_COUNT_CHART_CONFIG = {
  observed: { label: 'Observed', color: 'hsl(221, 83%, 53%)' },
  throttled: { label: 'Throttled', color: 'hsl(0, 84%, 60%)' },
} satisfies ChartConfig

export const QUOTA_THROTTLE_CHART_CONFIG = {
  avgThrottleMs: { label: 'Avg backoff', color: 'hsl(38, 92%, 50%)' },
} satisfies ChartConfig
