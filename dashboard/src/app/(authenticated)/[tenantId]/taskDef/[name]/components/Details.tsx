'use client'
import { TaskDef } from 'littlehorse-client/proto'
import { FC } from 'react'

type DetailsProps = {
  spec: TaskDef
}

export const Details: FC<DetailsProps> = ({ spec }) => {
  return (
    <div className="mb-4">
      <span className="italic">TaskDef</span>
      <h1 className="block text-2xl font-bold">{spec.id?.name}</h1>
    </div>
  )
}
