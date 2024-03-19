'use client'
import { WfSpec } from 'littlehorse-client/dist/proto/wf_spec'
import { FC } from 'react'
import { VersionSelector } from './VersionSelector'

type DetailsProps = Pick<WfSpec, 'id' | 'status'>

const statusColors: { [key in WfSpec['status']]: string } = {
  ARCHIVED: 'bg-gray-200',
  ACTIVE: 'bg-blue-200',
  TERMINATING: 'bg-yellow-200',
  UNRECOGNIZED: 'bg-red-200',
}

export const Details: FC<DetailsProps> = ({ id, status }) => {
  return (
    <div className="mb-4">
      <span>WfSpec</span>
      <h1 className="block font-bold text-2xl">{id?.name}</h1>
      <div className="flex flex-row gap-2 text-sm text-gray-500">
        <VersionSelector wfSpecId={id} />
        <div className="flex items-center">
          Status: <span className={`rounded ml-2 px-2 ${statusColors[status]}`}>{`${status}`}</span>
        </div>
      </div>
    </div>
  )
}
