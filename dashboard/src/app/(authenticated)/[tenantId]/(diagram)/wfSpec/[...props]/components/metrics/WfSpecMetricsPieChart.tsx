'use client'

import { FC } from 'react'
import { Cell, Legend, Pie, PieChart, ResponsiveContainer, Tooltip } from 'recharts'
import { PieDataPoint } from './metricsData'
import { ViewMode } from './wfSpecMetricsTypes'

export type WfSpecMetricsPieChartProps = {
  viewMode: ViewMode
  pieData: PieDataPoint[]
}

export const WfSpecMetricsPieChart: FC<WfSpecMetricsPieChartProps> = ({ viewMode, pieData }) => {
  if (pieData.length === 0) {
    return (
      <div className="flex h-[300px] items-center justify-center text-sm text-muted-foreground">
        No data for pie chart
      </div>
    )
  }

  return (
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
  )
}
