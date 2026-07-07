import { formatTypeDefinition } from '@/app/utils'
import { routes } from '@/app/routes'
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

  switch (definedType.oneofKind) {
    case 'inlineArrayDef':
      return <TypeBadge>{formatTypeDefinition(definedType)}</TypeBadge>
    case 'inlineMapDef':
      return <TypeBadge>{formatTypeDefinition(definedType)}</TypeBadge>
    case 'primitiveType':
      return <TypeBadge>{formatTypeDefinition(definedType)}</TypeBadge>
    case 'structDefId':
      return (
        <TypeBadge>
          <LinkWithTenant
            className="flex underline"
            href={routes.structDef.detail(definedType.structDefId.name, definedType.structDefId.version)}
          >
            {`Struct<${definedType.structDefId.name},${definedType.structDefId.version}>`}
          </LinkWithTenant>
        </TypeBadge>
      )
    default:
      throw new Error(`Unimplemented type case`)
  }
}
