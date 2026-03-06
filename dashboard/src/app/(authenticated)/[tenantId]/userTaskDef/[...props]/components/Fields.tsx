import { VARIABLE_TYPES } from '@/app/constants'
import { getVariableCaseFromType } from '@/app/utils'
import { Badge, IdentifierBadge, RequiredBadge, TypeBadge } from '@/components/ui/badge'
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
          <IdentifierBadge name={name} />
          {displayName && (
            <Badge className="bg-gray-100 px-2 py-1 font-mono text-sm text-gray-500">display: {displayName}</Badge>
          )}
          <TypeBadge>{VARIABLE_TYPES[getVariableCaseFromType(type)]}</TypeBadge>
          {required && <RequiredBadge />}
          {description && <Badge className="italic">{description}</Badge>}
        </div>
      ))}
    </div>
  )
}
