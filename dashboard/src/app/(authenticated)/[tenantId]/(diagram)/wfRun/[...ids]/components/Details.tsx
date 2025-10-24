'use client'
import LinkWithTenant from '@/app/(authenticated)/[tenantId]/components/LinkWithTenant'
import { flattenWfRunId, formatDate, wfRunIdToPath } from '@/app/utils'
import { WfRun } from 'littlehorse-client/proto'
import { FC } from 'react'
import { CopyToClipboard } from './CopyToClipboard'
import { wfRunStatusColor } from '../../../StatusColor'

type DetailsProps = WfRun


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
          Status: <span className={`ml-2 rounded px-2 ${wfRunStatusColor[status]}`}>{`${status}`}</span>
        </div>
        <div className="flex items-center">
          Started: <span className={` ml-2`}>{`${formatDate(Date.parse(startTime || ''))}`}</span>
        </div>
      </div>
    </div>
  )
}
