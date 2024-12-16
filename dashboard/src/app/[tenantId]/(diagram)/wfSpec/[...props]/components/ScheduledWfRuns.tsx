import { ScheduledWfRunIdList, WfSpec } from 'littlehorse-client/proto'
import { getScheduleWfSpec } from '../actions/getScheduleWfSpec'
import { SelectionLink } from '@/app/[tenantId]/components/SelectionLink'
import { ScheduledWfRun } from '../../../../../../../../sdk-js/dist/proto/scheduled_wf_run'
import { FUTURE_TIME_RANGES, TimeRange } from '@/app/constants'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { ClockIcon } from 'lucide-react'
import { useEffect, useState } from 'react'
import { getCronTimeWindow } from '@/app/utils/getCronTimeWindow'
import { parseExpression } from 'cron-parser'
import { utcToLocalDateTime } from '@/app/utils'
import { SearchVariableDialog } from './SearchVariableDialog'
import { SearchFooter } from '@/app/[tenantId]/components/SearchFooter'

export const ScheduledWfRuns = (spec: WfSpec) => {
  const [currentWindow, setWindow] = useState<TimeRange>(-1)
  const [filteredScheduledWfRuns, setFilteredScheduledWfRuns] = useState<ScheduledWfRun[]>([])

  const searchParams = useSearchParams()
  const status = searchParams.get('status') ? getStatus(searchParams.get('status')) || 'ALL' : 'ALL'
  const [limit, setLimit] = useState<number>(SEARCH_DEFAULT_LIMIT)


  useEffect(() => {
    setFilteredScheduledWfRuns(
      scheduledWfRuns.filter(scheduledWfRun => {
        if (currentWindow === -1) return true

        const timeWindow = getCronTimeWindow(scheduledWfRun.cronExpression)
        return timeWindow && timeWindow <= currentWindow
      })
    )
  }, [currentWindow, scheduledWfRuns])

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
      <SearchFooter currentLimit={limit} setLimit={setLimit} hasNextPage={hasNextPage} fetchNextPage={fetchNextPage} />
    </div>
  )
}
