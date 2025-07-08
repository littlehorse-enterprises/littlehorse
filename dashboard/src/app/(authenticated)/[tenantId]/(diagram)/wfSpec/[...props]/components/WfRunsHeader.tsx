import { TIME_RANGES, TIME_RANGES_NAMES, TimeRange, WF_RUN_STATUSES } from '@/app/constants'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { LHStatus, WfSpec } from 'littlehorse-client/proto'
import { ClockIcon } from 'lucide-react'
import LinkWithTenant from '@/app/(authenticated)/[tenantId]/components/LinkWithTenant'
import { FC } from 'react'
import { SearchVariableDialog } from './SearchVariableDialog'
import { usePathname } from 'next/navigation'

type Props = {
  spec: WfSpec
  currentStatus: LHStatus | 'ALL'
  currentWindow: TimeRange
  setWindow: (window: TimeRange) => void
}

export const WfRunsHeader: FC<Props> = ({ spec, currentStatus, currentWindow, setWindow }) => {
  const pathname = usePathname()
  const pathWithoutTenant = pathname.replace(/^\/[^/]+/, '')

  return (
    <div className="mb-4 flex items-center justify-between">
      <div className="flex items-center justify-between gap-4">
        <Select value={currentWindow.toString()} onValueChange={value => setWindow(parseInt(value) as TimeRange)}>
          <SelectTrigger className="w-[150px] min-w-fit">
            <div className="flex items-center gap-2">
              <ClockIcon className="h-5 w-5 fill-none stroke-black" />
              <SelectValue>
                {currentWindow !== -1 ? `Last ${TIME_RANGES_NAMES[currentWindow]}` : TIME_RANGES_NAMES[currentWindow]}
              </SelectValue>
            </div>
          </SelectTrigger>
          <SelectContent>
            {TIME_RANGES.map(time => (
              <SelectItem key={time} value={time.toString()}>
                {time !== -1 ? `Last ${TIME_RANGES_NAMES[time]}` : TIME_RANGES_NAMES[time]}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
        <div className="flex">
          {['ALL', ...WF_RUN_STATUSES].map(status => (
            <LinkWithTenant
              key={status}
              href={status === 'ALL' ? pathWithoutTenant : `${pathWithoutTenant}?status=${status}`}
              replace
              scroll={false}
              className={`flex items-center border-y-2 border-l-2 p-2 text-xs first-of-type:rounded-l-lg first-of-type:border-l-2 last-of-type:rounded-r-lg last-of-type:border-r-2 ${status === currentStatus ? 'border-blue-500 bg-blue-500 text-white' : ' text-gray-500'}`}
            >
              {status}
            </LinkWithTenant>
          ))}
        </div>
        {Object.keys(spec.threadSpecs).flatMap(threadSpec =>
          spec.threadSpecs[threadSpec].variableDefs.filter(variableDef => variableDef.searchable)
        ).length > 0 && <SearchVariableDialog spec={spec} />}
      </div>
    </div>
  )
}
