'use client'
import LinkWithTenant from '@/app/(authenticated)/[tenantId]/components/LinkWithTenant'
import { flattenWfRunId, formatDate, wfRunIdToPath } from '@/app/utils'
import { ThreadType } from '@/app/(authenticated)/[tenantId]/(diagram)/context'
import { useSelectedThreadError } from '@/app/(authenticated)/[tenantId]/(diagram)/hooks/useSelectedThreadError'
import { WfRun } from 'littlehorse-client/proto'
import { Expand } from 'lucide-react'
import { FC, useState } from 'react'
import { CopyToClipboard } from './CopyToClipboard'
import { WF_RUN_STATUS } from '../../../components/Sidebar/Components/StatusColor'

type DetailsProps = WfRun & { selectedThread?: ThreadType }

export const Details: FC<DetailsProps> = ({ selectedThread, ...wfRun }) => {
  const { id, status, wfSpecId, startTime } = wfRun
  const { threadError, onExpandError } = useSelectedThreadError(wfRun, selectedThread)
  const [isHovered, setIsHovered] = useState(false)

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
          <LinkWithTenant href={`/wfRun/${wfRunIdToPath(id.parentWfRunId)}`} linkStyle>
            <p>{id.parentWfRunId.id}</p>
          </LinkWithTenant>
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
          Status: <span className={`ml-2 rounded px-2 ${WF_RUN_STATUS[status].backgroundColor}`}>{`${status}`}</span>
        </div>
        <div className="flex items-center">
          Started: <span className={` ml-2`}>{`${formatDate(Date.parse(startTime || ''))}`}</span>
        </div>
      </div>
      {threadError && (
        <div
          className="mt-2 flex items-center gap-3 rounded border border-yellow-200 bg-yellow-50 px-3 py-2"
          onMouseEnter={() => setIsHovered(true)}
          onMouseLeave={() => setIsHovered(false)}
        >
          <span
            className={`shrink-0 rounded px-2 py-0.5 text-sm font-medium ${WF_RUN_STATUS[status].backgroundColor} ${WF_RUN_STATUS[status].textColor}`}
          >
            {status}
          </span>
          {threadError.threadName && (
            <span className="shrink-0 rounded bg-yellow-100 px-2 py-0.5 text-sm font-medium text-yellow-800">
              {threadError.threadName}
            </span>
          )}
          <button
            type="button"
            className="flex min-w-0 flex-1 cursor-pointer items-center gap-1 border-0 bg-transparent p-0 text-left text-sm text-yellow-900 hover:underline"
            onClick={onExpandError}
          >
            <span className="truncate">{threadError.errorMessage}</span>
            {isHovered && <Expand className="shrink-0 text-yellow-700" size={14} />}
          </button>
        </div>
      )}
    </div>
  )
}
