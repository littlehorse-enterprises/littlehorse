'use client'
import {
  ChartConfig,
  ChartContainer,
  ChartLegend,
  ChartLegendContent,
  ChartTooltip,
  ChartTooltipContent,
} from '@/components/ui/chart'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Label } from '@/components/ui/label'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { WfSpecId } from 'littlehorse-client/proto'
import { RefreshCwIcon } from 'lucide-react'
import { FC, useCallback, useMemo, useState } from 'react'
import {
  CartesianGrid,
  Cell,
  Legend,
  Line,
  LineChart,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import useSWR from 'swr'
import { getWfMetrics } from '../actions/getWfMetrics'
import {
  CountDataPoint,
  LatencyDataPoint,
  PieDataPoint,
  transformToCountData,
  transformToLatencyData,
  transformToPieData,
} from './metricsData'

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

const BUCKET_OPTIONS = [
  { value: '1', label: '1 minute' },
  { value: '5', label: '5 minutes' },
  { value: '10', label: '10 minutes' },
  { value: '30', label: '30 minutes' },
  { value: '60', label: '1 hour' },
  { value: '1440', label: '1 day' },
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

  const chartConfig = viewMode === 'count' ? COUNT_CHART_CONFIG : LATENCY_CHART_CONFIG
  const chartData = viewMode === 'count' ? countData : latencyData
  const hasData = chartData.length > 0 || pieData.length > 0

  return (
    <Tabs defaultValue="line">
      <Card>
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-4">
          <div className="flex items-center gap-3">
            <CardTitle className="text-base font-medium">Workflow Metrics</CardTitle>
            <TabsList className="h-8">
              <TabsTrigger value="line" className="px-2.5 py-1 text-xs">
                Line Chart
              </TabsTrigger>
              <TabsTrigger value="pie" className="px-2.5 py-1 text-xs">
                Pie Chart
              </TabsTrigger>
            </TabsList>
          </div>
          <div className="flex flex-wrap items-center justify-end gap-x-4 gap-y-2">
            <div className="flex items-center gap-2">
              <Label htmlFor="wf-metrics-view" className="whitespace-nowrap text-xs text-muted-foreground">
                View
              </Label>
              <Select value={viewMode} onValueChange={v => setViewMode(v as ViewMode)}>
                <SelectTrigger id="wf-metrics-view" className="h-8 w-[140px] text-xs">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="count">Count</SelectItem>
                  <SelectItem value="latency">Latency</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="flex items-center gap-2">
              <Label htmlFor="wf-metrics-bucket" className="whitespace-nowrap text-xs text-muted-foreground">
                Bucket size
              </Label>
              <Select value={bucketMinutes} onValueChange={setBucketMinutes}>
                <SelectTrigger id="wf-metrics-bucket" className="h-8 w-[160px] text-xs">
                  <SelectValue placeholder="Bucket" />
                </SelectTrigger>
                <SelectContent>
                  {BUCKET_OPTIONS.map(opt => (
                    <SelectItem key={opt.value} value={opt.value}>
                      {opt.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="flex items-center gap-2">
              <Label htmlFor="wf-metrics-range" className="whitespace-nowrap text-xs text-muted-foreground">
                Time range
              </Label>
              <Select value={rangeMinutes} onValueChange={setRangeMinutes}>
                <SelectTrigger id="wf-metrics-range" className="h-8 w-[160px] text-xs">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {TIME_RANGE_OPTIONS.map(opt => (
                    <SelectItem key={opt.value} value={opt.value}>
                      {opt.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
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
            <>
              <TabsContent value="line" className="mt-0">
                <ChartContainer config={chartConfig} className="h-[300px] w-full">
                  {viewMode === 'count' ? (
                    <LineChart data={countData} margin={{ top: 5, right: 10, left: 10, bottom: 0 }}>
                      <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
                      <XAxis dataKey="time" tick={{ fontSize: 11 }} tickLine={false} axisLine={false} />
                      <YAxis tick={{ fontSize: 11 }} tickLine={false} axisLine={false} allowDecimals={false} />
                      <ChartTooltip content={<ChartTooltipContent />} />
                      <ChartLegend content={<ChartLegendContent />} />
                      <Line
                        type="monotone"
                        dataKey="started"
                        stroke="var(--color-started)"
                        strokeWidth={2}
                        dot={false}
                      />
                      <Line
                        type="monotone"
                        dataKey="completed"
                        stroke="var(--color-completed)"
                        strokeWidth={2}
                        dot={false}
                      />
                      <Line type="monotone" dataKey="error" stroke="var(--color-error)" strokeWidth={2} dot={false} />
                      <Line
                        type="monotone"
                        dataKey="exception"
                        stroke="var(--color-exception)"
                        strokeWidth={2}
                        dot={false}
                      />
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
                            formatter={(value, name, item) => {
                              const ms = Number(value)
                              const formatted = ms >= 1000 ? `${(ms / 1000).toFixed(2)}s` : `${ms}ms`
                              return (
                                <>
                                  <div
                                    className="h-2.5 w-2.5 shrink-0 rounded-[2px] border-[--color-border] bg-[--color-bg]"
                                    style={
                                      { '--color-bg': item.color, '--color-border': item.color } as React.CSSProperties
                                    }
                                  />
                                  <div className="flex flex-1 items-center justify-between leading-none">
                                    <span className="text-muted-foreground">
                                      {LATENCY_CHART_CONFIG[name as keyof typeof LATENCY_CHART_CONFIG]?.label ?? name}
                                    </span>
                                    <span className="ml-2 font-mono font-medium tabular-nums text-foreground">
                                      {formatted}
                                    </span>
                                  </div>
                                </>
                              )
                            }}
                          />
                        }
                      />
                      <ChartLegend content={<ChartLegendContent />} />
                      <Line
                        type="monotone"
                        dataKey="completedAvg"
                        stroke="var(--color-completedAvg)"
                        strokeWidth={2}
                        dot={false}
                      />
                      <Line
                        type="monotone"
                        dataKey="completedMax"
                        stroke="var(--color-completedMax)"
                        strokeWidth={2}
                        dot={false}
                        strokeDasharray="5 5"
                      />
                      <Line
                        type="monotone"
                        dataKey="errorAvg"
                        stroke="var(--color-errorAvg)"
                        strokeWidth={2}
                        dot={false}
                      />
                      <Line
                        type="monotone"
                        dataKey="errorMax"
                        stroke="var(--color-errorMax)"
                        strokeWidth={2}
                        dot={false}
                        strokeDasharray="5 5"
                      />
                    </LineChart>
                  )}
                </ChartContainer>
              </TabsContent>
              <TabsContent value="pie" className="mt-0">
                {pieData.length === 0 ? (
                  <div className="flex h-[300px] items-center justify-center text-sm text-muted-foreground">
                    No data for pie chart
                  </div>
                ) : (
                  <div className="h-[300px] w-full">
                    <ResponsiveContainer width="100%" height="100%">
                      <PieChart>
                        <Pie
                          data={pieData}
                          dataKey="value"
                          nameKey="name"
                          cx="50%"
                          cy="50%"
                          outerRadius={100}
                          innerRadius={50}
                          paddingAngle={2}
                          label={({ name, value }) => {
                            if (viewMode === 'latency') {
                              const ms = Number(value)
                              return `${name}: ${ms >= 1000 ? `${(ms / 1000).toFixed(1)}s` : `${ms}ms`}`
                            }
                            return `${name}: ${value.toLocaleString()}`
                          }}
                          labelLine
                        >
                          {pieData.map((entry, i) => (
                            <Cell key={i} fill={entry.fill} />
                          ))}
                        </Pie>
                        <Tooltip
                          formatter={(value: number, name: string) => {
                            if (viewMode === 'latency') {
                              return [value >= 1000 ? `${(value / 1000).toFixed(2)}s` : `${value}ms`, name]
                            }
                            return [value.toLocaleString(), name]
                          }}
                        />
                        <Legend />
                      </PieChart>
                    </ResponsiveContainer>
                  </div>
                )}
              </TabsContent>
            </>
          )}
        </CardContent>
      </Card>
    </Tabs>
  )
}
