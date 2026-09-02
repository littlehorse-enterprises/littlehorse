'use client'

import { WfSpecMetricsContent } from '@/app/(authenticated)/[tenantId]/(diagram)/wfSpec/[...props]/components/metrics/WfSpecMetricsContent'
import { ViewMode } from '@/app/(authenticated)/[tenantId]/(diagram)/wfSpec/[...props]/components/metrics/wfSpecMetricsTypes'
import { Card } from '@/components/ui/card'
import { Tabs } from '@/components/ui/tabs'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { TaskDefId } from 'littlehorse-client/proto'
import { FC, useCallback, useMemo, useState } from 'react'
import useSWR from 'swr'
import { getTaskMetrics } from '../../actions/getTaskMetrics'
import { TaskDefMetricsHeader } from './TaskDefMetricsHeader'
import {
  CountDataPoint,
  LatencyDataPoint,
  PieDataPoint,
  transformTaskToCountData,
  transformTaskToLatencyData,
  transformTaskToPieData,
} from './taskMetricsData'

type TaskDefMetricsProps = {
  taskDefId: TaskDefId
}

export const TaskDefMetrics: FC<TaskDefMetricsProps> = ({ taskDefId }) => {
  const { tenantId } = useWhoAmI()
  const [rangeMinutes, setRangeMinutes] = useState('60')
  const [bucketMinutes, setBucketMinutes] = useState('5')
  const [viewMode, setViewMode] = useState<ViewMode>('count')

  const rangeNum = parseInt(rangeMinutes)
  const bucketNum = parseInt(bucketMinutes)
  const taskDefName = taskDefId.name ?? ''

  const fetcher = useCallback(async () => {
    const nowMs = Date.now()
    const rangeStartMs = nowMs - rangeNum * 60 * 1000
    const windowStart = new Date(rangeStartMs).toISOString()
    const windowEnd = new Date(nowMs).toISOString()
    const result = await getTaskMetrics({ taskDefId, windowStart, windowEnd, tenantId })
    return { result, rangeStartMs, rangeEndMs: nowMs }
  }, [taskDefId, rangeNum, tenantId])

  const { data, error, isLoading } = useSWR(['taskMetrics', taskDefName, tenantId, rangeMinutes], fetcher, {
    refreshInterval: 120_000,
    revalidateOnFocus: true,
    revalidateOnMount: true,
  })

  const { countData, latencyData, pieData } = useMemo(() => {
    if (data === undefined) {
      return { countData: [] as CountDataPoint[], latencyData: [] as LatencyDataPoint[], pieData: [] as PieDataPoint[] }
    }
    const { rangeStartMs, rangeEndMs } = data
    const windows = data.result.windows ?? []
    return {
      countData: transformTaskToCountData(windows, bucketNum, rangeNum, rangeStartMs, rangeEndMs),
      latencyData: transformTaskToLatencyData(windows, bucketNum, rangeNum, rangeStartMs, rangeEndMs),
      pieData: transformTaskToPieData(windows, viewMode),
    }
  }, [data, bucketNum, rangeNum, viewMode])

  const chartData = viewMode === 'count' ? countData : latencyData
  const hasData = chartData.length > 0 || pieData.length > 0

  return (
    <Tabs defaultValue="line">
      <Card>
        <TaskDefMetricsHeader
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
