'use client'
import { formatDate } from '@/app/utils/date'
import { concatWfRunIds } from '@/app/utils/wfRun'
import { WfRun } from 'littlehorse-client/dist/proto/wf_run'
import Link from 'next/link'
import { FC } from 'react'

type DetailsProps = WfRun

const statusColors: { [key in WfRun['status']]: string } = {
  STARTING: 'bg-teal-200',
  RUNNING: 'bg-blue-200',
  COMPLETED: 'bg-green-200',
  HALTING: 'bg-orange-200',
  HALTED: 'bg-gray-200',
  ERROR: 'bg-yellow-200',
  EXCEPTION: 'bg-red-200',
  UNRECOGNIZED: 'bg-gray-200',
}

export const Details: FC<DetailsProps> = ({ id, status, wfSpecId, startTime }) => {
  return (
    <div className="mb-4">
      <span className="italic">WfRun</span>
      <h1 className="block text-2xl font-bold">{id?.id}</h1>
      {id?.parentWfRunId && (
        <div className="flex items-center gap-2">
          Parent WfRun:
          <Link href={`/wfRun/${concatWfRunIds(id?.parentWfRunId)}`} className="text-blue-500 underline">
            {id?.parentWfRunId?.id}
          </Link>
        </div>
      )}
      <div className="flex flex-row gap-2 text-sm text-gray-500">
        <div className="flex items-center gap-2">
          WfSpec:
          <Link
            href={`/wfSpec/${wfSpecId?.name}/${wfSpecId?.majorVersion}/${wfSpecId?.revision}`}
            className="flex items-center gap-2 text-blue-500 underline"
          >
            {`${wfSpecId?.name} ${wfSpecId?.majorVersion}.${wfSpecId?.revision}`}
          </Link>
        </div>
        <div className="flex items-center">
          Status: <span className={`ml-2 rounded px-2 ${statusColors[status]}`}>{`${status}`}</span>
        </div>
        <div className="flex items-center">
          Started: <span className={` ml-2`}>{`${formatDate(Date.parse(startTime || ''))}`}</span>
        </div>
      </div>
    </div>
  )
}
