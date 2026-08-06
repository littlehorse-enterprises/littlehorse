import { AccessLevelBadge, IdentifierBadge, MaskedBadge, RequiredBadge, SearchableBadge } from '@/components/ui/badge'
import { ThreadSpec } from 'littlehorse-client/proto'
import { FC } from 'react'
import { TypeDisplay } from '../../../../components/TypeDisplay'
import { SpecEmpty, SpecSectionTitle } from './SpecTags'

type VariablesProps = Pick<ThreadSpec, 'variableDefs'>

export const Variables: FC<VariablesProps> = ({ variableDefs }) => {
  if (variableDefs.length === 0) {
    return (
      <div>
        <SpecSectionTitle>Variables</SpecSectionTitle>
        <SpecEmpty>No variables</SpecEmpty>
      </div>
    )
  }

  return (
    <div>
      <SpecSectionTitle>Variables</SpecSectionTitle>
      <ul className="space-y-2">
        {variableDefs.map(variable => (
          <li
            key={variable.varDef?.name}
            className="flex flex-wrap items-center gap-1 rounded-md border border-gray-100 px-3 py-2"
          >
            {variable.varDef?.name && <IdentifierBadge name={variable.varDef.name} />}
            <TypeDisplay definedType={variable.varDef?.typeDef?.definedType} />
            {variable.required && <RequiredBadge />}
            {variable.searchable && <SearchableBadge />}
            {variable.varDef?.typeDef?.masked && <MaskedBadge />}
            <AccessLevelBadge accessLevel={variable.accessLevel} />
          </li>
        ))}
      </ul>
    </div>
  )
}
