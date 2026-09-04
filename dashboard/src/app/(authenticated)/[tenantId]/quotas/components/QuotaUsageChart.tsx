'use client'

import {
  ChartContainer,
  ChartLegend,
  ChartLegendContent,
  ChartTooltip,
  ChartTooltipContent,
} from '@/components/ui/chart'
import { type CSSProperties, FC } from 'react'
import { CartesianGrid, Line, LineChart, ReferenceLine, XAxis, YAxis } from 'recharts'
import { formatDurationMs, QuotaCountDataPoint, QuotaThrottleDataPoint } from './quotaMetricsData'
import { QUOTA_COUNT_CHART_CONFIG, QUOTA_THROTTLE_CHART_CONFIG, QuotaViewMode } from './quotaUsageConstants'

export type QuotaUsageChartProps = {
  viewMode: QuotaViewMode
  countData: QuotaCountDataPoint[]
  throttleData: QuotaThrottleDataPoint[]
  bucketLimit?: number
}

export const QuotaUsageChart: FC<QuotaUsageChartProps> = ({ viewMode, countData, throttleData, bucketLimit }) => (
  <ChartContainer
    config={viewMode === 'requests' ? QUOTA_COUNT_CHART_CONFIG : QUOTA_THROTTLE_CHART_CONFIG}
    className="h-[300px] w-full"
  >
    {viewMode === 'requests' ? (
      <LineChart data={countData} margin={{ top: 5, right: 10, left: 10, bottom: 0 }}>
        <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
        <XAxis dataKey="time" tick={{ fontSize: 11 }} tickLine={false} axisLine={false} />
        <YAxis
          tick={{ fontSize: 11 }}
          tickLine={false}
          axisLine={false}
          allowDecimals={false}
          domain={bucketLimit !== undefined ? [0, (dataMax: number) => Math.max(dataMax, bucketLimit)] : undefined}
        />
        <ChartTooltip content={<ChartTooltipContent />} />
        <ChartLegend content={<ChartLegendContent />} />
        {bucketLimit !== undefined && (
          <ReferenceLine
            y={bucketLimit}
            stroke="hsl(215, 16%, 47%)"
            strokeDasharray="6 4"
            label={{ value: 'Quota limit', position: 'insideTopRight', fontSize: 11, fill: 'hsl(215, 16%, 47%)' }}
          />
        )}
        <Line type="monotone" dataKey="observed" stroke="var(--color-observed)" strokeWidth={2} dot={false} />
        <Line type="monotone" dataKey="throttled" stroke="var(--color-throttled)" strokeWidth={2} dot={false} />
      </LineChart>
    ) : (
      <LineChart data={throttleData} margin={{ top: 5, right: 10, left: 10, bottom: 0 }}>
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
              formatter={(value, name, item) => (
                <>
                  <div
                    className="h-2.5 w-2.5 shrink-0 rounded-[2px] border-[--color-border] bg-[--color-bg]"
                    style={{ '--color-bg': item.color, '--color-border': item.color } as CSSProperties}
                  />
                  <div className="flex flex-1 items-center justify-between leading-none">
                    <span className="text-muted-foreground">
                      {QUOTA_THROTTLE_CHART_CONFIG[name as keyof typeof QUOTA_THROTTLE_CHART_CONFIG]?.label ?? name}
                    </span>
                    <span className="ml-2 font-mono font-medium tabular-nums text-foreground">
                      {formatDurationMs(Number(value))}
                    </span>
                  </div>
                </>
              )}
            />
          }
        />
        <ChartLegend content={<ChartLegendContent />} />
        <Line type="monotone" dataKey="avgThrottleMs" stroke="var(--color-avgThrottleMs)" strokeWidth={2} dot={false} />
      </LineChart>
    )}
  </ChartContainer>
)
