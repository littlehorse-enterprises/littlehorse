'use client'

import { Card } from '@/components/ui/card'
import { Tabs } from '@/components/ui/tabs'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { WfSpecId } from 'littlehorse-client/proto'
import { FC, useCallback, useMemo, useState } from 'react'
import useSWR from 'swr'
import { getWfMetrics } from '../../actions/getWfMetrics'
import {
  CountDataPoint,
  LatencyDataPoint,
  PieDataPoint,
  transformToCountData,
  transformToLatencyData,
  transformToPieData,
} from './metricsData'
import { WfSpecMetricsContent } from './WfSpecMetricsContent'
import { WfSpecMetricsHeader } from './WfSpecMetricsHeader'
import { ViewMode } from './wfSpecMetricsTypes'

type WfSpecMetricsProps = {
  wfSpecId: WfSpecId
}

export const WfSpecMetrics: FC<WfSpecMetricsProps> = ({ wfSpecId }) => {
  const { tenantId } = useWhoAmI()
  const [rangeMinutes, setRangeMinutes] = useState('60')
  const [bucketMinutes, setBucketMinutes] = useState('5')
  const [viewMode, setViewMode] = useState<ViewMode>('count')

  const rangeNum = parseInt(rangeMinutes)
  const bucketNum = parseInt(bucketMinutes)

  const fetcher = useCallback(async () => {
    const nowMs = Date.now()
    const rangeStartMs = nowMs - rangeNum * 60 * 1000
    const windowStart = new Date(rangeStartMs).toISOString()
    const windowEnd = new Date(nowMs).toISOString()
    const result = await getWfMetrics({ wfSpecId, windowStart, windowEnd, tenantId })
    return { result, rangeStartMs, rangeEndMs: nowMs }
  }, [wfSpecId, rangeNum, tenantId])

  const { data, error, isLoading } = useSWR(
    ['wfMetrics', wfSpecId.name, wfSpecId.majorVersion, tenantId, rangeMinutes],
    fetcher,
    { refreshInterval: 120_000, revalidateOnFocus: true, revalidateOnMount: true }
  )

  const { countData, latencyData, pieData } = useMemo(() => {
    if (data === undefined) {
      return { countData: [] as CountDataPoint[], latencyData: [] as LatencyDataPoint[], pieData: [] as PieDataPoint[] }
    }
    const { rangeStartMs, rangeEndMs } = data
    const windows = data.result.windows ?? []
    return {
      countData: transformToCountData(windows, bucketNum, rangeNum, rangeStartMs, rangeEndMs),
      latencyData: transformToLatencyData(windows, bucketNum, rangeNum, rangeStartMs, rangeEndMs),
      pieData: transformToPieData(windows, viewMode),
    }
  }, [data, bucketNum, rangeNum, viewMode])

  const chartData = viewMode === 'count' ? countData : latencyData
  const hasData = chartData.length > 0 || pieData.length > 0

  return (
    <Tabs defaultValue="line">
      <Card>
        <WfSpecMetricsHeader
          viewMode={viewMode}
          onViewModeChange={setViewMode}
          bucketMinutes={bucketMinutes}
          onBucketMinutesChange={setBucketMinutes}
          rangeMinutes={rangeMinutes}
          onRangeMinutesChange={setRangeMinutes}
        />
        <WfSpecMetricsContent
          isLoading={isLoading}
          error={error}
          hasData={hasData}
          viewMode={viewMode}
          countData={countData}
          latencyData={latencyData}
          pieData={pieData}
        />
      </Card>
    </Tabs>
  )
}
