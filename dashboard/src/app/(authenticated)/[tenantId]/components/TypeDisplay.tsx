import { VARIABLE_TYPES } from '@/app/constants'
import { getVariableCaseFromType } from '@/app/utils'
import { TypeBadge } from '@/components/ui/badge'
import { TypeDefinition } from 'littlehorse-client/proto'
import { FC } from 'react'
import LinkWithTenant from './LinkWithTenant'

type Props = {
  definedType?: TypeDefinition['definedType']
}

export const TypeDisplay: FC<Props> = ({ definedType }) => {
  if (!definedType) {
    return <TypeBadge>void</TypeBadge>
  }

  if (definedType?.$case === 'structDefId') {
    return (
      <TypeBadge>
        <LinkWithTenant
          className="flex underline"
          href={`/structDef/${definedType.value.name}/${definedType.value.version}`}
        >
          {`Struct<${definedType.value.name},${definedType.value.version}>`}
        </LinkWithTenant>
      </TypeBadge>
    )
  }

  if (definedType?.$case === 'primitiveType') {
    return <TypeBadge>{VARIABLE_TYPES[getVariableCaseFromType(definedType.value)]}</TypeBadge>
  }
}
