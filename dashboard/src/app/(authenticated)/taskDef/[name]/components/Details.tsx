'use client'
import { TaskDef } from 'littlehorse-client/proto'
import { FC } from 'react'

type DetailsProps = Pick<TaskDef, 'id'>

export const Details: FC<DetailsProps> = ({ id }) => {
  return (
    <div className="mb-4">
      <span className="italic">TaskDef</span>
      <h1 className="block text-2xl font-bold">{id?.name}</h1>
    </div>
  )
}
