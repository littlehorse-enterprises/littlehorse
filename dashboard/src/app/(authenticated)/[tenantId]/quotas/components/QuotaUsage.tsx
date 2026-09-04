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
  QuotaCountDataPoint,
  QuotaThrottleDataPoint,
  QuotaUsageSummary,
  summarizeQuotaUsage,
  transformToQuotaCountData,
  transformToQuotaThrottleData,
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

  const quotaId = useMemo(
    () =>
      scope === 'principal' && principalId
        ? { tenant: { id: tenantId }, principal: { id: principalId } }
        : { tenant: { id: tenantId } },
    [scope, principalId, tenantId]
  )

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

  const { data, error, isLoading } = useSWR(
    ['quotaUsageMetrics', tenantId, scope, principalId, rangeMinutes],
    fetcher,
    {
      refreshInterval: 120_000,
      revalidateOnFocus: true,
      revalidateOnMount: true,
    }
  )

  const { countData, throttleData, summary } = useMemo(() => {
    if (data === undefined) {
      return {
        countData: [] as QuotaCountDataPoint[],
        throttleData: [] as QuotaThrottleDataPoint[],
        summary: EMPTY_SUMMARY,
      }
    }
    const { rangeStartMs, rangeEndMs } = data
    const windows = data.result.windows ?? []
    return {
      countData: transformToQuotaCountData(windows, bucketNum, rangeNum, rangeStartMs, rangeEndMs),
      throttleData: transformToQuotaThrottleData(windows, bucketNum, rangeNum, rangeStartMs, rangeEndMs),
      summary: summarizeQuotaUsage(windows),
    }
  }, [data, bucketNum, rangeNum])

  const limitRps = quota?.writeRequestsPerSecond
  const bucketLimit = limitRps !== undefined ? limitRps * 60 * bucketNum : undefined

  return (
    <>
      <Navigation href={routes.appRoot()} title="Go back to home" />
      <Details tenantId={tenantId} scope={scope} principalId={principalId} limitRps={limitRps} />
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
            hasData={summary.observed > 0}
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
