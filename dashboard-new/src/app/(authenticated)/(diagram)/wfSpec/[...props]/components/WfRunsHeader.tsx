import { TIME_RANGES, TIME_RANGES_NAMES, TimeRange, WF_RUN_STATUSES } from '@/app/constants'
import { Listbox } from '@headlessui/react'
import { ClockIcon } from '@heroicons/react/24/outline'
import { LHStatus } from 'littlehorse-client/dist/proto/common_enums'
import Link from 'next/link'
import { FC } from 'react'

type Props = {
  currentStatus: LHStatus
  currentWindow: TimeRange
  setWindow: (window: TimeRange) => void
}

export const WfRunsHeader: FC<Props> = ({ currentStatus, currentWindow, setWindow }) => {
  return (
    <div className="mb-4 flex items-center justify-between">
      <h2 className="text-2xl font-bold">WfRun Search</h2>
      <div className="flex">
        <Listbox value={currentWindow} onChange={setWindow}>
          <div className="relative">
            <Listbox.Button className="flex items-center gap-2 rounded-lg border-2 px-2 py-1 text-xs">
              <ClockIcon className="h-5 w-5 fill-none stroke-black" />
              Last {TIME_RANGES_NAMES[currentWindow]}
            </Listbox.Button>
            <Listbox.Options className="absolute mt-1 w-[120px] rounded-md bg-white py-1 text-base shadow-lg ring-1 ring-black/5 focus:outline-none sm:text-sm">
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
            </Listbox.Options>
          </div>
        </Listbox>
      </div>
      <div className="flex">
        {WF_RUN_STATUSES.map(status => (
          <Link
            key={status}
            href={`?status=${status}`}
            replace
            scroll={false}
            className={`flex items-center border-y-2 border-l-2 p-2 text-xs first-of-type:rounded-l-lg first-of-type:border-l-2 last-of-type:rounded-r-lg last-of-type:border-r-2 ${status === currentStatus ? 'border-blue-500 bg-blue-500 text-white' : ' text-gray-500'}`}
          >
            {status}
          </Link>
        ))}
      </div>
    </div>
  )
}
