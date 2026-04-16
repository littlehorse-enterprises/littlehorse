import { formatTypeDefinition } from '@/app/utils'
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

  switch (definedType.$case) {
    case 'inlineArrayDef':
      return <TypeBadge>{formatTypeDefinition(definedType)}</TypeBadge>
    case 'primitiveType':
      return <TypeBadge>{formatTypeDefinition(definedType)}</TypeBadge>
    case 'structDefId':
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
    default:
      throw new Error(`Unimplemented type case`)
  }
}
