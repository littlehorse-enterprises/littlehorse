'use client'

import { SelectionLink } from '@/app/(authenticated)/[tenantId]/components/SelectionLink'
import { FUTURE_TIME_RANGES, TimeRange } from '@/app/constants'
import { utcToLocalDateTime } from '@/app/utils'
import { getCronTimeWindow } from '@/app/utils/getCronTimeWindow'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { parseExpression } from 'cron-parser'
import { WfSpec } from 'littlehorse-client/proto'
import { ClockIcon, RefreshCwIcon } from 'lucide-react'
import { useParams } from 'next/navigation'
import { useMemo, useState } from 'react'
import useSWR from 'swr'
import { getScheduleWfSpec } from '../actions/getScheduleWfSpec'

export const ScheduledWfRuns = (spec: WfSpec) => {
  const [currentWindow, setWindow] = useState<TimeRange>(-1)
  const tenantId = useParams().tenantId as string

  const fetchScheduledWfRuns = async () => {
    return await getScheduleWfSpec({
      name: spec.id!.name,
      version: spec.id!.majorVersion + '.' + spec.id!.revision,
      tenantId: tenantId,
    })
  }

  const { data: scheduledWfRuns, error } = useSWR(['scheduledWfRuns', spec.id, tenantId], fetchScheduledWfRuns)

  const isLoading = !scheduledWfRuns && !error

  const filteredScheduledWfRuns = useMemo(
    () =>
      (scheduledWfRuns || [])
        .filter(scheduledWfRun => {
          if (currentWindow === -1) return true
          const timeWindow = getCronTimeWindow(scheduledWfRun.cronExpression)
          return timeWindow && timeWindow <= currentWindow
        })
        .sort((a, b) => {
          const timeA = parseExpression(a.cronExpression).next().toDate().getTime()
          const timeB = parseExpression(b.cronExpression).next().toDate().getTime()
          return timeA - timeB
        }),
    [currentWindow, scheduledWfRuns]
  )

  if (isLoading) {
    return (
      <div className="flex min-h-[500px] items-center justify-center">
        <RefreshCwIcon className="h-8 w-8 animate-spin text-blue-500" />
      </div>
    )
  }

  if (error) {
    return (
      <div className="flex min-h-[500px] flex-col items-center justify-center text-red-500">
        <p>Error loading scheduled runs</p>
        <p className="text-sm">{error.message}</p>
      </div>
    )
  }

  return (
    <div className="flex min-h-[500px] flex-col">
      <div className="flex gap-4">
        <Select value={currentWindow.toString()} onValueChange={value => setWindow(parseInt(value) as TimeRange)}>
          <SelectTrigger className="w-[150px] min-w-fit">
            <div className="flex items-center gap-2">
              <ClockIcon className="h-5 w-5 fill-none stroke-black" />
              <SelectValue>{FUTURE_TIME_RANGES.find(time => time.value === currentWindow)?.label}</SelectValue>
            </div>
          </SelectTrigger>
          <SelectContent>
            {FUTURE_TIME_RANGES.map(time => (
              <SelectItem key={time.value} value={time.value.toString()}>
                {time.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>
      <div className="mt-4 flex flex-col">
        {filteredScheduledWfRuns.map(scheduledWfRun => (
          <SelectionLink aria-disabled key={scheduledWfRun.id?.id} href={undefined}>
            <p>{scheduledWfRun.id?.id}</p>
            <div className="flex items-center gap-2 rounded-md bg-gray-200 p-1 text-sm">
              <ClockIcon className="h-5 w-5 fill-none stroke-black" />
              <p>{utcToLocalDateTime(parseExpression(scheduledWfRun.cronExpression).next().toDate().toISOString())}</p>
            </div>
          </SelectionLink>
        ))}
      </div>
    </div>
  )
}
