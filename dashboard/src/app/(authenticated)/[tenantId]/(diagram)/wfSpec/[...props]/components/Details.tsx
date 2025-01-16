'use client'
import { WfSpec } from 'littlehorse-client/proto'
import { FC } from 'react'
import { Versions } from './Versions'

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
      <span className="italic">WfSpec</span>
      <h1 className="block text-2xl font-bold">{id?.name}</h1>
      <div className="flex flex-row gap-2 text-sm text-gray-500">
        <Versions wfSpecId={id} />
        <div className="flex items-center">
          Status: <span className={`ml-2 rounded px-2 ${statusColors[status]}`}>{`${status}`}</span>
        </div>
      </div>
    </div>
  )
}
