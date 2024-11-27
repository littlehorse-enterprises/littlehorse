'use client'
import { WorkflowEventDef } from 'littlehorse-client/proto'
import { FC } from 'react'

type DetailsProps = {
  spec: WorkflowEventDef
}

export const Details: FC<DetailsProps> = ({ spec: { id } }) => {
  return (
    <div className="mb-4">
      <span className="italic">WorkflowEventDef</span>
      <h1 className="block text-2xl font-bold">{id?.name}</h1>
    </div>
  )
}
