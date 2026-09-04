'use client'

import { Navigation } from '@/app/(authenticated)/[tenantId]/components/Navigation'
import { routes } from '@/app/routes'
import { Card } from '@/components/ui/card'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { Quota } from 'littlehorse-client/proto'
import { FC, useCallback, useMemo, useState } from 'react'
import useSWR from 'swr'
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
import { QuotaOption, quotaOptionKey, QuotaViewMode, TENANT_WIDE_QUOTA_KEY } from './quotaUsageConstants'

const EMPTY_SUMMARY: QuotaUsageSummary = { observed: 0, throttled: 0, throttleRate: 0, totalThrottleTimeMs: 0 }

type QuotaUsageProps = {
  quotas: Quota[]
  quotasAvailable: boolean
  initialQuotaKey?: string
}

export const QuotaUsage: FC<QuotaUsageProps> = ({ quotas, quotasAvailable, initialQuotaKey }) => {
  const { tenantId } = useWhoAmI()
  const [selectedQuotaKey, setSelectedQuotaKey] = useState(initialQuotaKey ?? TENANT_WIDE_QUOTA_KEY)
  const [rangeMinutes, setRangeMinutes] = useState('60')
  const [bucketMinutes, setBucketMinutes] = useState('5')
  const [viewMode, setViewMode] = useState<QuotaViewMode>('requests')

  const quotaOptions = useMemo<QuotaOption[]>(() => {
    const fromServer = quotas.flatMap<QuotaOption>(q =>
      q.id
        ? [
            {
              key: quotaOptionKey(q.id),
              label: q.id.principal ? `Principal: ${q.id.principal.id}` : 'Tenant-wide',
              quotaId: q.id,
              limitRps: q.writeRequestsPerSecond,
            },
          ]
        : []
    )
    const deduped = fromServer.filter((option, i) => fromServer.findIndex(o => o.key === option.key) === i)
    const options = deduped.some(o => o.key === TENANT_WIDE_QUOTA_KEY)
      ? deduped
      : [{ key: TENANT_WIDE_QUOTA_KEY, label: 'Tenant-wide', quotaId: { tenant: { id: tenantId } } }, ...deduped]
    return options.sort((a, b) => {
      if (a.key === TENANT_WIDE_QUOTA_KEY) return -1
      if (b.key === TENANT_WIDE_QUOTA_KEY) return 1
      return a.label.localeCompare(b.label)
    })
  }, [quotas, tenantId])

  const selected = quotaOptions.find(o => o.key === selectedQuotaKey) ?? quotaOptions[0]

  const rangeNum = parseInt(rangeMinutes)
  const bucketNum = parseInt(bucketMinutes)

  const fetcher = useCallback(async () => {
    const nowMs = Date.now()
    const rangeStartMs = nowMs - rangeNum * 60 * 1000
    const result = await getQuotaUsageMetrics({
      quotaId: selected.quotaId,
      windowStart: new Date(rangeStartMs).toISOString(),
      windowEnd: new Date(nowMs).toISOString(),
      tenantId,
    })
    return { result, rangeStartMs, rangeEndMs: nowMs }
  }, [selected, rangeNum, tenantId])

  const { data, error, isLoading } = useSWR(['quotaUsageMetrics', tenantId, selected.key, rangeMinutes], fetcher, {
    refreshInterval: 120_000,
    revalidateOnFocus: true,
    revalidateOnMount: true,
  })

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

  const bucketLimit = selected.limitRps !== undefined ? selected.limitRps * 60 * bucketNum : undefined

  return (
    <>
      <Navigation href={routes.appRoot()} title="Go back to home" />
      <Details tenantId={tenantId} limitRps={selected.limitRps} limitsKnown={quotasAvailable} />
      <hr className="mt-6" />
      <div className="mt-6">
        <Card>
          <QuotaUsageHeader
            quotaOptions={quotaOptions}
            selectedQuotaKey={selected.key}
            onQuotaChange={setSelectedQuotaKey}
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
            noQuotasConfigured={quotasAvailable && quotas.length === 0}
            quotasAvailable={quotasAvailable}
          />
        </Card>
      </div>
    </>
  )
}
