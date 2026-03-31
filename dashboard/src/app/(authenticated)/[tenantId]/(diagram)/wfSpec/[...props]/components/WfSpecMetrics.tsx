'use client'
import { ChartConfig, ChartContainer, ChartLegend, ChartLegendContent, ChartTooltip, ChartTooltipContent } from '@/components/ui/chart'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { MetricWindow, WfSpecId } from 'littlehorse-client/proto'
import { RefreshCwIcon } from 'lucide-react'
import { FC, useCallback, useMemo, useState } from 'react'
import { CartesianGrid, Line, LineChart, XAxis, YAxis } from 'recharts'
import useSWR from 'swr'
import { getWfMetrics } from '../actions/getWfMetrics'

type ViewMode = 'count' | 'latency'

const TIME_RANGE_OPTIONS = [
  { value: '30', label: 'Last 30 minutes' },
  { value: '60', label: 'Last 1 hour' },
  { value: '360', label: 'Last 6 hours' },
  { value: '720', label: 'Last 12 hours' },
  { value: '1440', label: 'Last 24 hours' },
  { value: '4320', label: 'Last 3 days' },
  { value: '10080', label: 'Last 7 days' },
] as const

const COUNT_CHART_CONFIG = {
  started: { label: 'Started', color: 'hsl(221, 83%, 53%)' },
  completed: { label: 'Completed', color: 'hsl(142, 71%, 45%)' },
  error: { label: 'Error', color: 'hsl(0, 84%, 60%)' },
  exception: { label: 'Exception', color: 'hsl(38, 92%, 50%)' },
} satisfies ChartConfig

const LATENCY_CHART_CONFIG = {
  completedAvg: { label: 'Completed (avg)', color: 'hsl(142, 71%, 45%)' },
  completedMax: { label: 'Completed (max)', color: 'hsl(142, 71%, 30%)' },
  errorAvg: { label: 'Error (avg)', color: 'hsl(0, 84%, 60%)' },
  errorMax: { label: 'Error (max)', color: 'hsl(0, 84%, 40%)' },
} satisfies ChartConfig

type CountDataPoint = {
  time: string
  timestamp: number
  started: number
  completed: number
  error: number
  exception: number
}

type LatencyDataPoint = {
  time: string
  timestamp: number
  completedAvg: number
  completedMax: number
  errorAvg: number
  errorMax: number
}

function formatTime(isoString: string, rangeMinutes: number): string {
  const date = new Date(isoString)
  if (rangeMinutes <= 60) return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  if (rangeMinutes <= 1440) return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  return date.toLocaleDateString([], { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

function avgLatency(ct: { count: number; totalLatencyMs: number } | undefined): number {
  if (!ct || ct.count === 0) return 0
  return Math.round(ct.totalLatencyMs / ct.count)
}

function transformToCountData(windows: MetricWindow[], rangeMinutes: number): CountDataPoint[] {
  return windows
    .filter(w => w.metric?.$case === 'workflow')
    .map(w => {
      const wf = w.metric!.value as import('littlehorse-client/proto').WfMetrics
      const windowStart = w.id?.windowStart ?? ''
      return {
        time: formatTime(windowStart, rangeMinutes),
        timestamp: new Date(windowStart).getTime(),
        started: wf.started?.count ?? 0,
        completed: wf.runningToCompleted?.count ?? 0,
        error: wf.runningToError?.count ?? 0,
        exception: wf.runningToException?.count ?? 0,
      }
    })
    .sort((a, b) => a.timestamp - b.timestamp)
}

function transformToLatencyData(windows: MetricWindow[], rangeMinutes: number): LatencyDataPoint[] {
  return windows
    .filter(w => w.metric?.$case === 'workflow')
    .map(w => {
      const wf = w.metric!.value as import('littlehorse-client/proto').WfMetrics
      const windowStart = w.id?.windowStart ?? ''
      return {
        time: formatTime(windowStart, rangeMinutes),
        timestamp: new Date(windowStart).getTime(),
        completedAvg: avgLatency(wf.runningToCompleted),
        completedMax: wf.runningToCompleted?.maxLatencyMs ?? 0,
        errorAvg: avgLatency(wf.runningToError),
        errorMax: wf.runningToError?.maxLatencyMs ?? 0,
      }
    })
    .sort((a, b) => a.timestamp - b.timestamp)
}

type WfSpecMetricsProps = {
  wfSpecId: WfSpecId
}

export const WfSpecMetrics: FC<WfSpecMetricsProps> = ({ wfSpecId }) => {
  const { tenantId } = useWhoAmI()
  const [rangeMinutes, setRangeMinutes] = useState('60')
  const [viewMode, setViewMode] = useState<ViewMode>('count')

  const rangeNum = parseInt(rangeMinutes)

  const fetcher = useCallback(async () => {
    const now = new Date()
    const windowStart = new Date(now.getTime() - rangeNum * 60 * 1000).toISOString()
    const windowEnd = now.toISOString()
    return getWfMetrics({ wfSpecId, windowStart, windowEnd, tenantId })
  }, [wfSpecId, rangeNum, tenantId])

  const { data, error, isLoading } = useSWR(
    ['wfMetrics', wfSpecId.name, wfSpecId.majorVersion, tenantId, rangeMinutes],
    fetcher,
    { refreshInterval: 120_000, revalidateOnFocus: true, revalidateOnMount: true }
  )

  const countData = useMemo(() => {
    if (!data?.windows) return []
    return transformToCountData(data.windows, rangeNum)
  }, [data, rangeNum])

  const latencyData = useMemo(() => {
    if (!data?.windows) return []
    return transformToLatencyData(data.windows, rangeNum)
  }, [data, rangeNum])

  const chartConfig = viewMode === 'count' ? COUNT_CHART_CONFIG : LATENCY_CHART_CONFIG
  const chartData = viewMode === 'count' ? countData : latencyData
  const hasData = chartData.length > 0

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-4">
        <CardTitle className="text-base font-medium">Workflow Metrics</CardTitle>
        <div className="flex items-center gap-2">
          <Select value={viewMode} onValueChange={v => setViewMode(v as ViewMode)}>
            <SelectTrigger className="h-8 w-[140px] text-xs">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="count">Count</SelectItem>
              <SelectItem value="latency">Latency</SelectItem>
            </SelectContent>
          </Select>
          <Select value={rangeMinutes} onValueChange={setRangeMinutes}>
            <SelectTrigger className="h-8 w-[160px] text-xs">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {TIME_RANGE_OPTIONS.map(opt => (
                <SelectItem key={opt.value} value={opt.value}>{opt.label}</SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <div className="flex h-[300px] items-center justify-center">
            <RefreshCwIcon className="h-6 w-6 animate-spin text-blue-500" />
          </div>
        ) : error ? (
          <div className="flex h-[300px] items-center justify-center text-sm text-muted-foreground">
            Failed to load metrics
          </div>
        ) : !hasData ? (
          <div className="flex h-[300px] items-center justify-center text-sm text-muted-foreground">
            No metric data available for this time range
          </div>
        ) : (
          <ChartContainer config={chartConfig} className="h-[300px] w-full">
            {viewMode === 'count' ? (
              <LineChart data={countData} margin={{ top: 5, right: 10, left: 10, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
                <XAxis dataKey="time" tick={{ fontSize: 11 }} tickLine={false} axisLine={false} />
                <YAxis tick={{ fontSize: 11 }} tickLine={false} axisLine={false} allowDecimals={false} />
                <ChartTooltip content={<ChartTooltipContent />} />
                <ChartLegend content={<ChartLegendContent />} />
                <Line type="monotone" dataKey="started" stroke="var(--color-started)" strokeWidth={2} dot={false} />
                <Line type="monotone" dataKey="completed" stroke="var(--color-completed)" strokeWidth={2} dot={false} />
                <Line type="monotone" dataKey="error" stroke="var(--color-error)" strokeWidth={2} dot={false} />
                <Line type="monotone" dataKey="exception" stroke="var(--color-exception)" strokeWidth={2} dot={false} />
              </LineChart>
            ) : (
              <LineChart data={latencyData} margin={{ top: 5, right: 10, left: 10, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
                <XAxis dataKey="time" tick={{ fontSize: 11 }} tickLine={false} axisLine={false} />
                <YAxis
                  tick={{ fontSize: 11 }}
                  tickLine={false}
                  axisLine={false}
                  tickFormatter={v => (v >= 1000 ? `${(v / 1000).toFixed(1)}s` : `${v}ms`)}
                />
                <ChartTooltip
                  content={
                    <ChartTooltipContent
                      formatter={(value, name) => {
                        const ms = Number(value)
                        const formatted = ms >= 1000 ? `${(ms / 1000).toFixed(2)}s` : `${ms}ms`
                        return <span>{formatted}</span>
                      }}
                    />
                  }
                />
                <ChartLegend content={<ChartLegendContent />} />
                <Line type="monotone" dataKey="completedAvg" stroke="var(--color-completedAvg)" strokeWidth={2} dot={false} />
                <Line type="monotone" dataKey="completedMax" stroke="var(--color-completedMax)" strokeWidth={2} dot={false} strokeDasharray="5 5" />
                <Line type="monotone" dataKey="errorAvg" stroke="var(--color-errorAvg)" strokeWidth={2} dot={false} />
                <Line type="monotone" dataKey="errorMax" stroke="var(--color-errorMax)" strokeWidth={2} dot={false} strokeDasharray="5 5" />
              </LineChart>
            )}
          </ChartContainer>
        )}
      </CardContent>
    </Card>
  )
}
