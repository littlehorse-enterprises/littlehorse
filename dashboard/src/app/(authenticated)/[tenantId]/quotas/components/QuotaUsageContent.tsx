'use client'

import { CardContent } from '@/components/ui/card'
import { RefreshCwIcon } from 'lucide-react'
import { FC } from 'react'
import { formatDurationMs } from '@/app/utils'
import { QuotaCountDataPoint, QuotaThrottleDataPoint, QuotaUsageSummary } from './quotaMetricsData'
import { QuotaUsageChart } from './QuotaUsageChart'
import { QuotaViewMode } from './quotaUsageConstants'

export type QuotaUsageContentProps = {
  isLoading: boolean
  error: unknown
  hasData: boolean
  viewMode: QuotaViewMode
  countData: QuotaCountDataPoint[]
  throttleData: QuotaThrottleDataPoint[]
  summary: QuotaUsageSummary
  bucketLimit?: number
  noQuotaConfigured: boolean
}

const SummaryTile: FC<{ label: string; value: string }> = ({ label, value }) => (
  <div className="rounded-lg border p-3">
    <p className="text-xs text-muted-foreground">{label}</p>
    <p className="mt-1 text-xl font-semibold tabular-nums">{value}</p>
  </div>
)

export const QuotaUsageContent: FC<QuotaUsageContentProps> = ({
  isLoading,
  error,
  hasData,
  viewMode,
  countData,
  throttleData,
  summary,
  bucketLimit,
  noQuotaConfigured,
}) => (
  <CardContent>
    {noQuotaConfigured && (
      <p className="pb-4 text-sm text-muted-foreground">
        No quota applies to you in this tenant, so no usage is being recorded.
      </p>
    )}
    {isLoading ? (
      <div className="flex h-[300px] items-center justify-center">
        <RefreshCwIcon className="h-6 w-6 animate-spin text-blue-500" />
      </div>
    ) : error ? (
      <div className="flex h-[300px] items-center justify-center text-sm text-muted-foreground">
        Failed to load quota usage
      </div>
    ) : (
      <>
        <div className="grid grid-cols-2 gap-3 pb-4 md:grid-cols-4">
          <SummaryTile label="Requests observed" value={summary.observed.toLocaleString()} />
          <SummaryTile label="Requests throttled" value={summary.throttled.toLocaleString()} />
          <SummaryTile label="Throttle rate" value={`${(summary.throttleRate * 100).toFixed(1)}%`} />
          <SummaryTile label="Time throttled" value={formatDurationMs(summary.totalThrottleTimeMs)} />
        </div>
        {hasData ? (
          <QuotaUsageChart
            viewMode={viewMode}
            countData={countData}
            throttleData={throttleData}
            bucketLimit={bucketLimit}
          />
        ) : (
          <div className="flex h-[300px] items-center justify-center text-sm text-muted-foreground">
            No quota usage recorded for this time range
          </div>
        )}
      </>
    )}
  </CardContent>
)
