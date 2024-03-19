'use client'
import { TaskDef } from 'littlehorse-client/dist/proto/task_def'
import { FC } from 'react'

type DetailsProps = Pick<TaskDef, 'id'>

export const Details: FC<DetailsProps> = ({ id }) => {
  return (
    <div className="mb-4">
      <span className="italic">TaskDef</span>
      <h1 className="block font-bold text-2xl">{id?.name}</h1>
    </div>
  )
}
