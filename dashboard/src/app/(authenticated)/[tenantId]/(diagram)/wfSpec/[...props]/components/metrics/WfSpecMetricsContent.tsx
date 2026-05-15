'use client'

import { CardContent } from '@/components/ui/card'
import { TabsContent } from '@/components/ui/tabs'
import type { ChartConfig } from '@/components/ui/chart'
import { RefreshCwIcon } from 'lucide-react'
import { FC } from 'react'
import { COUNT_CHART_CONFIG, LATENCY_CHART_CONFIG } from './metricsConstants'
import { CountDataPoint, LatencyDataPoint, PieDataPoint } from './metricsData'
import { ViewMode } from './wfSpecMetricsTypes'
import { WfSpecMetricsLineChart } from './WfSpecMetricsLineChart'
import { WfSpecMetricsPieChart } from './WfSpecMetricsPieChart'

export type WfSpecMetricsContentProps = {
  isLoading: boolean
  error: unknown
  hasData: boolean
  viewMode: ViewMode
  countData: CountDataPoint[]
  latencyData: LatencyDataPoint[]
  pieData: PieDataPoint[]
}

export const WfSpecMetricsContent: FC<WfSpecMetricsContentProps> = ({
  isLoading,
  error,
  hasData,
  viewMode,
  countData,
  latencyData,
  pieData,
}) => {
  const chartConfig: ChartConfig = viewMode === 'count' ? COUNT_CHART_CONFIG : LATENCY_CHART_CONFIG

  return (
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
            <WfSpecMetricsLineChart
              viewMode={viewMode}
              chartConfig={chartConfig}
              countData={countData}
              latencyData={latencyData}
            />
          </TabsContent>
          <TabsContent value="pie" className="mt-0">
            <WfSpecMetricsPieChart viewMode={viewMode} pieData={pieData} />
          </TabsContent>
        </>
      )}
    </CardContent>
  )
}
