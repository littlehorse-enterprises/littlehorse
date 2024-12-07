'use client'
import { UserTaskDef } from 'littlehorse-client/proto'
import { FC } from 'react'
import { Versions } from './Versions'
import { TagIcon } from 'lucide-react'

type Props = {
  id: Pick<UserTaskDef, 'name' | 'version' | 'description'>
  staticVersion?: boolean
}
export const Details: FC<Props> = ({ id, staticVersion = false }) => {
  return (
    <div className="mb-4">
      <span className="italic">UserTaskDef</span>
      <h1 className="block text-2xl font-bold">{id.name}</h1>
      {id.description && <div className="italic text-gray-400">{id.description}</div>}
      <div className="flex flex-row gap-2 text-sm text-gray-500">
        {staticVersion ? (
          <div className="flex items-center gap-2">
            <TagIcon className="h-5 w-5" />
            {id.version}
          </div>
        ) : (
          <Versions id={id} />
        )}
      </div>
    </div>
  )
}
