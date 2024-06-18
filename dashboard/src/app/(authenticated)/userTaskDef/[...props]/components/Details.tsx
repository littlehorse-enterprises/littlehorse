'use client'
import { UserTaskDef } from 'littlehorse-client/proto'
import { FC } from 'react'
import { Versions } from './Versions'

type Props = {
  id: Pick<UserTaskDef, 'name' | 'version' | 'description'>
}
export const Details: FC<Props> = ({ id }) => {
  return (
    <div className="mb-4">
      <span className="italic">UserTaskDef</span>
      <h1 className="block text-2xl font-bold">{id.name}</h1>
      {id.description && <div className="italic text-gray-400">{id.description}</div>}
      <div className="flex flex-row gap-2 text-sm text-gray-500">
        <Versions id={id} />
      </div>
    </div>
  )
}
