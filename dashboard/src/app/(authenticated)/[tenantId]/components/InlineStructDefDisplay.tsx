import { formatTypeDefinition, getVariableValue, isStructFieldRequired } from '@/app/utils'
import { Badge, IdentifierBadge, MaskedBadge, RequiredBadge, TypeBadge } from '@/components/ui/badge'
import { InlineStructDef, TypeDefinition } from 'littlehorse-client/proto'
import { FC } from 'react'
import { TypeDisplay } from './TypeDisplay'

type Props = {
  inlineStructDef: InlineStructDef
}

const NestedInlineStructDef: FC<{ definedType?: TypeDefinition['definedType'] }> = ({ definedType }) => {
  switch (definedType?.oneofKind) {
    case 'inlineStructDef':
      return (
        <details open className="mt-1 border-l pl-3">
          <summary className="cursor-pointer text-xs font-medium text-muted-foreground">Nested fields</summary>
          <div className="mt-3">
            <InlineStructDefDisplay inlineStructDef={definedType.inlineStructDef} />
          </div>
        </details>
      )
    case 'inlineArrayDef':
      return <NestedInlineStructDef definedType={definedType.inlineArrayDef.arrayType?.definedType} />
    case 'inlineMapDef':
      return <NestedInlineStructDef definedType={definedType.inlineMapDef.valueType?.definedType} />
    default:
      return null
  }
}

export const InlineStructDefDisplay: FC<Props> = ({ inlineStructDef }) => (
  <div className="flex min-w-0 flex-col gap-3">
    {Object.entries(inlineStructDef.fields).map(([name, fieldDef]) => {
      const definedType = fieldDef.fieldType?.definedType
      if (!definedType?.oneofKind) return null

      const isInlineStruct = definedType.oneofKind === 'inlineStructDef'

      return (
        <div key={name} className="flex flex-col gap-1 border-b pb-3 last:border-b-0 last:pb-0">
          <div className="flex flex-wrap items-center gap-1">
            <IdentifierBadge name={name} />
            {isInlineStruct ? (
              <TypeBadge>{formatTypeDefinition(definedType)}</TypeBadge>
            ) : (
              <TypeDisplay definedType={definedType} />
            )}
            {isStructFieldRequired(fieldDef) && <RequiredBadge />}
            {fieldDef.fieldType?.masked && <MaskedBadge />}
            {fieldDef.isNullable && <Badge className="bg-gray-100">Nullable</Badge>}
          </div>
          {fieldDef.defaultValue && (
            <span className="text-xs text-muted-foreground">Default: {getVariableValue(fieldDef.defaultValue)}</span>
          )}
          {fieldDef.description && <p className="text-xs text-muted-foreground">{fieldDef.description}</p>}
          <NestedInlineStructDef definedType={definedType} />
        </div>
      )
    })}
  </div>
)
