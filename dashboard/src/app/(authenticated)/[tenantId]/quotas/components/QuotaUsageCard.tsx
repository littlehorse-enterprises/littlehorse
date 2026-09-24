'use client'

import { Card, CardHeader, CardTitle } from '@/components/ui/card'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { FC, useCallback, useMemo } from 'react'
import useSWR from 'swr'
import { ApplicableQuota } from '../actions/getApplicableQuotas'
import { getQuotaUsageMetrics } from '../actions/getQuotaUsageMetrics'
import { bucketQuotaUsage, summarizeQuotaUsage, toQuotaCountData, toQuotaThrottleData } from './quotaMetricsData'
import { QuotaUsageContent } from './QuotaUsageContent'
import { QuotaViewMode } from './quotaUsageConstants'

type QuotaUsageCardProps = ApplicableQuota & {
  rangeMinutes: string
  bucketMinutes: string
  viewMode: QuotaViewMode
}

export const QuotaUsageCard: FC<QuotaUsageCardProps> = ({
  scope,
  quotaId,
  quota,
  rangeMinutes,
  bucketMinutes,
  viewMode,
}) => {
  const { tenantId } = useWhoAmI()
  const rangeNum = parseInt(rangeMinutes)
  const bucketNum = parseInt(bucketMinutes)

  const fetcher = useCallback(async () => {
    const nowMs = Date.now()
    const rangeStartMs = nowMs - rangeNum * 60 * 1000
    const result = await getQuotaUsageMetrics({
      quotaId,
      windowStart: new Date(rangeStartMs).toISOString(),
      windowEnd: new Date(nowMs).toISOString(),
      tenantId,
    })
    return { result, rangeStartMs, rangeEndMs: nowMs }
  }, [quotaId, rangeNum, tenantId])

  const { data, error, isLoading } = useSWR(['quotaUsageMetrics', tenantId, quotaId, rangeMinutes], fetcher, {
    refreshInterval: 120_000,
    revalidateOnFocus: true,
    revalidateOnMount: true,
  })

  const { countData, throttleData, summary } = useMemo(() => {
    const buckets = data
      ? bucketQuotaUsage(data.result.windows, bucketNum, rangeNum, data.rangeStartMs, data.rangeEndMs)
      : []
    return {
      countData: toQuotaCountData(buckets),
      throttleData: toQuotaThrottleData(buckets),
      summary: summarizeQuotaUsage(buckets),
    }
  }, [data, bucketNum, rangeNum])

  const title = scope === 'tenant' ? 'Tenant-wide quota' : `Principal quota · ${quotaId.principal?.id}`

  return (
    <Card>
      <CardHeader className="flex flex-row items-baseline gap-2 space-y-0 pb-4">
        <CardTitle className="text-base font-medium">{title}</CardTitle>
        <span className="text-xs text-muted-foreground">{quota.writeRequestsPerSecond} writes/s</span>
      </CardHeader>
      <QuotaUsageContent
        isLoading={isLoading}
        error={error}
        viewMode={viewMode}
        countData={countData}
        throttleData={throttleData}
        summary={summary}
        bucketLimit={quota.writeRequestsPerSecond * 60 * bucketNum}
      />
    </Card>
  )
}
