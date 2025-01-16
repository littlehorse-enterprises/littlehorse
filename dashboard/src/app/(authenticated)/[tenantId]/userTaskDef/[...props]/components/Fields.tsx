import { VARIABLE_TYPES } from '@/app/constants'
import { UserTaskDef } from 'littlehorse-client/proto'
import { FC } from 'react'

type Props = Pick<UserTaskDef, 'fields'>
export const Fields: FC<Props> = ({ fields }) => {
  if (fields.length === 0) return <></>
  return (
    <div className="">
      <h2 className="text-md mb-2 font-bold">Fields</h2>
      {fields.map(({ name, displayName, description, required, type }) => (
        <div key={name} className="mb-1 flex items-center gap-1">
          <span className="rounded	bg-gray-100 px-2 py-1 font-mono text-fuchsia-500">{name}</span>
          {displayName && (
            <span className="rounded	bg-gray-100 px-2 py-1 font-mono text-sm text-gray-500">display: {displayName}</span>
          )}
          <span className="rounded bg-yellow-100 p-1 text-xs">{VARIABLE_TYPES[type]}</span>
          {required && <span className="rounded bg-orange-300 p-1 text-xs">Required</span>}
          {description && <span className="rounded p-1 text-xs italic">{description}</span>}
        </div>
      ))}
    </div>
  )
}
