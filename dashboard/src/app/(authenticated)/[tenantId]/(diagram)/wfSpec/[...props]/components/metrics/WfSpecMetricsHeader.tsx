'use client'

import { CardHeader, CardTitle } from '@/components/ui/card'
import { Label } from '@/components/ui/label'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { TabsList, TabsTrigger } from '@/components/ui/tabs'
import { FC } from 'react'
import { BUCKET_OPTIONS, TIME_RANGE_OPTIONS } from './metricsConstants'
import { ViewMode } from './wfSpecMetricsTypes'

export type WfSpecMetricsHeaderProps = {
  viewMode: ViewMode
  onViewModeChange: (mode: ViewMode) => void
  bucketMinutes: string
  onBucketMinutesChange: (value: string) => void
  rangeMinutes: string
  onRangeMinutesChange: (value: string) => void
}

export const WfSpecMetricsHeader: FC<WfSpecMetricsHeaderProps> = ({
  viewMode,
  onViewModeChange,
  bucketMinutes,
  onBucketMinutesChange,
  rangeMinutes,
  onRangeMinutesChange,
}) => (
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
        <Select value={viewMode} onValueChange={v => onViewModeChange(v as ViewMode)}>
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
        <Select value={bucketMinutes} onValueChange={onBucketMinutesChange}>
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
        <Select value={rangeMinutes} onValueChange={onRangeMinutesChange}>
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
)
