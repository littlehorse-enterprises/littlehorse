'use client'

import { Navigation } from '@/app/(authenticated)/[tenantId]/components/Navigation'
import { routes } from '@/app/routes'
import { Card } from '@/components/ui/card'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { FC, useCallback, useMemo, useState } from 'react'
import useSWR from 'swr'
import { ApplicableQuota } from '../actions/getApplicableQuota'
import { getQuotaUsageMetrics } from '../actions/getQuotaUsageMetrics'
import { Details } from './Details'
import {
  bucketQuotaUsage,
  QuotaUsageSummary,
  summarizeQuotaUsage,
  toQuotaCountData,
  toQuotaThrottleData,
} from './quotaMetricsData'
import { QuotaUsageContent } from './QuotaUsageContent'
import { QuotaUsageHeader } from './QuotaUsageHeader'
import { QuotaViewMode } from './quotaUsageConstants'

const EMPTY_SUMMARY: QuotaUsageSummary = { observed: 0, throttled: 0, throttleRate: 0, totalThrottleTimeMs: 0 }

export const QuotaUsage: FC<ApplicableQuota> = ({ scope, quota, principalId }) => {
  const { tenantId } = useWhoAmI()
  const [rangeMinutes, setRangeMinutes] = useState('60')
  const [bucketMinutes, setBucketMinutes] = useState('5')
  const [viewMode, setViewMode] = useState<QuotaViewMode>('requests')

  const rangeNum = parseInt(rangeMinutes)
  const bucketNum = parseInt(bucketMinutes)

  const quotaId = quota?.id

  const fetcher = useCallback(async () => {
    const nowMs = Date.now()
    const rangeStartMs = nowMs - rangeNum * 60 * 1000
    if (!quotaId) return undefined
    const result = await getQuotaUsageMetrics({
      quotaId,
      windowStart: new Date(rangeStartMs).toISOString(),
      windowEnd: new Date(nowMs).toISOString(),
      tenantId,
    })
    return { result, rangeStartMs, rangeEndMs: nowMs }
  }, [quotaId, rangeNum, tenantId])

  const { data, error, isLoading } = useSWR(
    quotaId ? ['quotaUsageMetrics', tenantId, scope, principalId, rangeMinutes] : null,
    fetcher,
    {
      refreshInterval: 120_000,
      revalidateOnFocus: true,
      revalidateOnMount: true,
    }
  )

  const { countData, throttleData, summary } = useMemo(() => {
    if (data === undefined) {
      return { countData: [], throttleData: [], summary: EMPTY_SUMMARY }
    }
    const buckets = bucketQuotaUsage(data.result.windows ?? [], bucketNum, rangeNum, data.rangeStartMs, data.rangeEndMs)
    return {
      countData: toQuotaCountData(buckets),
      throttleData: toQuotaThrottleData(buckets),
      summary: summarizeQuotaUsage(buckets),
    }
  }, [data, bucketNum, rangeNum])

  const bucketLimit = quota !== undefined ? quota.writeRequestsPerSecond * 60 * bucketNum : undefined

  return (
    <>
      <Navigation href={routes.appRoot()} title="Go back to home" />
      <Details tenantId={tenantId} />
      <hr className="mt-6" />
      <div className="mt-6">
        <Card>
          <QuotaUsageHeader
            viewMode={viewMode}
            onViewModeChange={setViewMode}
            bucketMinutes={bucketMinutes}
            onBucketMinutesChange={setBucketMinutes}
            rangeMinutes={rangeMinutes}
            onRangeMinutesChange={setRangeMinutes}
          />
          <QuotaUsageContent
            isLoading={isLoading}
            error={error}
            hasData={summary.observed > 0 || summary.throttled > 0}
            viewMode={viewMode}
            countData={countData}
            throttleData={throttleData}
            summary={summary}
            bucketLimit={bucketLimit}
            noQuotaConfigured={scope === 'none'}
          />
        </Card>
      </div>
    </>
  )
}
