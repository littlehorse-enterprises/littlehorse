import { VARIABLE_TYPES } from '@/app/constants'
import { getVariableCaseFromType } from '@/app/utils'
import { TypeDefinition } from 'littlehorse-client/proto'
import { FC, ReactNode } from 'react'
import LinkWithTenant from './LinkWithTenant'

type TypeBadgeProps = {
  children: ReactNode
}

const TypeBadge: FC<TypeBadgeProps> = ({ children }) => (
  <span className="rounded bg-yellow-100 p-1 text-xs">{children}</span>
)

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
