'use client'
import LinkWithTenant from '@/app/(authenticated)/[tenantId]/components/LinkWithTenant'
import { concatWfRunIds, formatDate } from '@/app/utils'
import { WfRun, WfRunId } from 'littlehorse-client/proto'
import { FC } from 'react'
import { CopyToClipboard } from './CopyToClipboard'

type DetailsProps = WfRun

export const statusColors: { [key in WfRun['status']]: string } = {
  STARTING: 'bg-teal-200',
  RUNNING: 'bg-blue-200',
  COMPLETED: 'bg-green-200',
  HALTING: 'bg-orange-200',
  HALTED: 'bg-gray-200',
  ERROR: 'bg-yellow-200',
  EXCEPTION: 'bg-red-200',
  UNRECOGNIZED: 'bg-gray-200',
}

const flattenWfRunId = (wfRunId: WfRunId): string => {
  if (!wfRunId.parentWfRunId) return wfRunId.id
  return flattenWfRunId(wfRunId.parentWfRunId) + '_' + wfRunId.id
}

export const Details: FC<DetailsProps> = ({ id, status, wfSpecId, startTime }) => {
  if (!id || !wfSpecId) return null

  return (
    <div className="mb-4">
      <span className="italic">WfRun</span>
      <div className="flex items-center">
        <h1 className="block text-2xl font-bold">{id.id}</h1>
        <CopyToClipboard tooltipText="Copy full wfRun ID" textToCopy={flattenWfRunId(id)} />
      </div>
      {id.parentWfRunId && (
        <div className="flex items-center gap-2">
          Parent WfRun:
          <LinkWithTenant href={`/wfRun/${concatWfRunIds(id.parentWfRunId)}`} linkStyle><p>{id.parentWfRunId.id}</p></LinkWithTenant>
        </div>
      )}
      <div className="flex flex-row gap-2 text-sm text-gray-500">
        <div className="flex items-center gap-2">
          WfSpec:
          <LinkWithTenant
            href={`/wfSpec/${wfSpecId.name}/${wfSpecId.majorVersion}/${wfSpecId.revision}`}
            className="flex items-center gap-2 text-blue-500 underline"
          >
            {`${wfSpecId.name} ${wfSpecId.majorVersion}.${wfSpecId.revision}`}
          </LinkWithTenant>
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
