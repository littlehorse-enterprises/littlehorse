'use client'

import {
  ChartContainer,
  ChartLegend,
  ChartLegendContent,
  ChartTooltip,
  ChartTooltipContent,
  type ChartConfig,
} from '@/components/ui/chart'
import { type CSSProperties, FC } from 'react'
import { CartesianGrid, Line, LineChart, XAxis, YAxis } from 'recharts'
import { LATENCY_CHART_CONFIG } from './metricsConstants'
import { CountDataPoint, LatencyDataPoint } from './metricsData'
import { ViewMode } from './wfSpecMetricsTypes'

export type WfSpecMetricsLineChartProps = {
  viewMode: ViewMode
  chartConfig: ChartConfig
  countData: CountDataPoint[]
  latencyData: LatencyDataPoint[]
}

export const WfSpecMetricsLineChart: FC<WfSpecMetricsLineChartProps> = ({
  viewMode,
  chartConfig,
  countData,
  latencyData,
}) => (
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
              formatter={(value, name, item) => {
                const ms = Number(value)
                const formatted = ms >= 1000 ? `${(ms / 1000).toFixed(2)}s` : `${ms}ms`
                return (
                  <>
                    <div
                      className="h-2.5 w-2.5 shrink-0 rounded-[2px] border-[--color-border] bg-[--color-bg]"
                      style={{ '--color-bg': item.color, '--color-border': item.color } as CSSProperties}
                    />
                    <div className="flex flex-1 items-center justify-between leading-none">
                      <span className="text-muted-foreground">
                        {LATENCY_CHART_CONFIG[name as keyof typeof LATENCY_CHART_CONFIG]?.label ?? name}
                      </span>
                      <span className="ml-2 font-mono font-medium tabular-nums text-foreground">{formatted}</span>
                    </div>
                  </>
                )
              }}
            />
          }
        />
        <ChartLegend content={<ChartLegendContent />} />
        <Line type="monotone" dataKey="completedAvg" stroke="var(--color-completedAvg)" strokeWidth={2} dot={false} />
        <Line
          type="monotone"
          dataKey="completedMax"
          stroke="var(--color-completedMax)"
          strokeWidth={2}
          dot={false}
          strokeDasharray="5 5"
        />
        <Line type="monotone" dataKey="errorAvg" stroke="var(--color-errorAvg)" strokeWidth={2} dot={false} />
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
)
