import LinkWithTenant from '@/app/(authenticated)/[tenantId]/components/LinkWithTenant'
import { routes } from '@/app/routes'
import { formatTypeDefinition } from '@/app/utils'
import { TypeBadge } from '@/components/ui/badge'
import { TypeDefinition } from 'littlehorse-client/proto'
import { FC } from 'react'

interface TypeDefinitionBadgeProps {
  typeDef?: TypeDefinition | TypeDefinition['definedType']
}

/** Badge showing a variable's type. StructDef references link to their detail page. */
export const TypeDefinitionBadge: FC<TypeDefinitionBadgeProps> = ({ typeDef }) => {
  const definedType = !typeDef ? undefined : 'oneofKind' in typeDef ? typeDef : typeDef.definedType
  if (!definedType?.oneofKind) return null

  const label = formatTypeDefinition(definedType)

  if (definedType.oneofKind === 'structDefId') {
    const { name, version } = definedType.structDefId
    return (
      <TypeBadge>
        <LinkWithTenant className="underline" href={routes.structDef.detail(name, version)}>
          {label}
        </LinkWithTenant>
      </TypeBadge>
    )
  }

  return <TypeBadge>{label}</TypeBadge>
}

export default TypeDefinitionBadge
