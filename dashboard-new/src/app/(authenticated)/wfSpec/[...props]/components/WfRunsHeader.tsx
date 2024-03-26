import { WF_RUN_STATUSES } from '@/app/constants'
import { ClockIcon } from '@heroicons/react/24/outline'
import { LHStatus } from 'littlehorse-client/dist/proto/common_enums'
import Link from 'next/link'
import { FC } from 'react'

type Props = {
  currentStatus: LHStatus
}

export const WfRunsHeader: FC<Props> = ({ currentStatus }) => {
  return (
    <div className="mb-4 flex items-center justify-between">
      <h2 className="text-2xl font-bold">WfRun Search</h2>
      <div className="flex">
        <button className="flex items-center gap-2 rounded-lg border-2 px-2 py-1 text-xs">
          <ClockIcon className="h-5 w-5 fill-none stroke-black" />
          <span className="hidden md:inline">Last 5 minutes</span>
        </button>
      </div>
      <div className="flex">
        {WF_RUN_STATUSES.map(status => (
          <Link
            key={status}
            href={`?status=${status}`}
            className={`flex items-center border-y-2 border-l-2 p-2 text-xs first-of-type:rounded-l-lg first-of-type:border-l-2 last-of-type:rounded-r-lg last-of-type:border-r-2 ${status === currentStatus ? 'border-blue-500 bg-blue-500 text-white' : ' text-gray-500'}`}
          >
            {status}
          </Link>
        ))}
      </div>
    </div>
  )
}
