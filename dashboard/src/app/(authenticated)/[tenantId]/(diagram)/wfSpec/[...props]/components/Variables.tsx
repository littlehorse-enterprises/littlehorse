import { AccessLevelBadge, IdentifierBadge, MaskedBadge, RequiredBadge, SearchableBadge } from '@/components/ui/badge'
import { ThreadSpec } from 'littlehorse-client/proto'
import { FC } from 'react'
import { TypeDisplay } from '../../../../components/TypeDisplay'

type VariablesProps = Pick<ThreadSpec, 'variableDefs'>

export const Variables: FC<VariablesProps> = ({ variableDefs }) => {
  if (variableDefs.length === 0) return <p className="font-semibold">No variables</p>
  return (
    <div className="">
      <h2 className="text-md mb-2 font-bold">Variables</h2>
      {variableDefs.map(variable => (
        <div key={variable.varDef?.name} className="mb-1 flex items-center gap-1">
          {variable.varDef?.name && <IdentifierBadge name={variable.varDef.name} />}
          <TypeDisplay definedType={variable.varDef?.typeDef?.definedType} />
          {variable.required && <RequiredBadge />}
          {variable.searchable && <SearchableBadge />}
          {variable.varDef?.typeDef?.masked && <MaskedBadge />}
          <AccessLevelBadge accessLevel={variable.accessLevel} />
        </div>
      ))}
    </div>
  )
}
