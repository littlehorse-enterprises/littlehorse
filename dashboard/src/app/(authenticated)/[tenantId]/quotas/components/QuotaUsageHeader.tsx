'use client'

import {
  BUCKET_OPTIONS,
  TIME_RANGE_OPTIONS,
} from '@/app/(authenticated)/[tenantId]/(diagram)/wfSpec/[...props]/components/metrics/metricsConstants'
import { CardHeader, CardTitle } from '@/components/ui/card'
import { Label } from '@/components/ui/label'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { FC } from 'react'
import { QuotaViewMode } from './quotaUsageConstants'

export type QuotaUsageHeaderProps = {
  viewMode: QuotaViewMode
  onViewModeChange: (mode: QuotaViewMode) => void
  bucketMinutes: string
  onBucketMinutesChange: (value: string) => void
  rangeMinutes: string
  onRangeMinutesChange: (value: string) => void
}

export const QuotaUsageHeader: FC<QuotaUsageHeaderProps> = ({
  viewMode,
  onViewModeChange,
  bucketMinutes,
  onBucketMinutesChange,
  rangeMinutes,
  onRangeMinutesChange,
}) => (
  <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-4">
    <CardTitle className="text-base font-medium">Quota usage metrics</CardTitle>
    <div className="flex flex-wrap items-center justify-end gap-x-4 gap-y-2">
      <div className="flex items-center gap-2">
        <Label htmlFor="quota-usage-view" className="whitespace-nowrap text-xs text-muted-foreground">
          View
        </Label>
        <Select value={viewMode} onValueChange={v => onViewModeChange(v as QuotaViewMode)}>
          <SelectTrigger id="quota-usage-view" className="h-8 w-[140px] text-xs">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="requests">Requests</SelectItem>
            <SelectItem value="throttleTime">Throttle time</SelectItem>
          </SelectContent>
        </Select>
      </div>
      <div className="flex items-center gap-2">
        <Label htmlFor="quota-usage-bucket" className="whitespace-nowrap text-xs text-muted-foreground">
          Bucket size
        </Label>
        <Select value={bucketMinutes} onValueChange={onBucketMinutesChange}>
          <SelectTrigger id="quota-usage-bucket" className="h-8 w-[160px] text-xs">
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
        <Label htmlFor="quota-usage-range" className="whitespace-nowrap text-xs text-muted-foreground">
          Time range
        </Label>
        <Select value={rangeMinutes} onValueChange={onRangeMinutesChange}>
          <SelectTrigger id="quota-usage-range" className="h-8 w-[160px] text-xs">
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
