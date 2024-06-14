import { TIME_RANGES, TIME_RANGES_NAMES, TimeRange, WF_RUN_STATUSES } from '@/app/constants'
import { Listbox, ListboxButton, ListboxOptions } from '@headlessui/react'
import { LHStatus, WfSpec } from 'littlehorse-client'
import { ClockIcon } from 'lucide-react'
import Link from 'next/link'
import { FC } from 'react'
import { SearchVariableDialog } from './SearchVariableDialog'

type Props = {
  spec: WfSpec
  currentStatus: LHStatus | 'ALL'
  currentWindow: TimeRange
  setWindow: (window: TimeRange) => void
}

export const WfRunsHeader: FC<Props> = ({ spec, currentStatus, currentWindow, setWindow }) => {
  return (
    <div className="mb-4 flex items-center justify-between">
      <h2 className="text-2xl font-bold">WfRun Search</h2>

      <div className="flex items-center justify-between gap-4">
        <Listbox value={currentWindow} onChange={setWindow}>
          <div className="relative">
            <ListboxButton className="flex items-center gap-2 rounded-lg border-2 px-2 py-1 text-xs">
              <ClockIcon className="h-5 w-5 fill-none stroke-black" />
              Last {TIME_RANGES_NAMES[currentWindow]}
            </ListboxButton>
            <ListboxOptions className="absolute mt-1 w-[120px] rounded-md bg-white py-1 text-base shadow-lg ring-1 ring-black/5 focus:outline-none sm:text-sm">
              {TIME_RANGES.map(time => (
                <Listbox.Option
                  key={time}
                  value={time}
                  className={({ active }) =>
                    `relative cursor-default select-none py-2 pl-2 pr-4 text-xs ${
                      active ? 'bg-blue-500 text-white' : 'text-gray-900'
                    }`
                  }
                >
                  Last {TIME_RANGES_NAMES[time]}
                </Listbox.Option>
              ))}
            </ListboxOptions>
          </div>
        </Listbox>
        <div className="flex">
          {['ALL', ...WF_RUN_STATUSES].map(status => (
            <Link
              key={status}
              href={status === 'ALL' ? '?' : `?status=${status}`}
              replace
              scroll={false}
              className={`flex items-center border-y-2 border-l-2 p-2 text-xs first-of-type:rounded-l-lg first-of-type:border-l-2 last-of-type:rounded-r-lg last-of-type:border-r-2 ${status === currentStatus ? 'border-blue-500 bg-blue-500 text-white' : ' text-gray-500'}`}
            >
              {status}
            </Link>
          ))}
        </div>
        {Object.keys(spec.threadSpecs).flatMap(threadSpec =>
          spec.threadSpecs[threadSpec].variableDefs.filter(variableDef => variableDef.searchable)
        ).length > 0 && <SearchVariableDialog spec={spec} />}
      </div>
    </div>
  )
}
