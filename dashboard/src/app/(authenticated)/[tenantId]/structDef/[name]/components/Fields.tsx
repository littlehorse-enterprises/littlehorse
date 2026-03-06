import { getVariableValue } from '@/app/utils'
import { Badge, IdentifierBadge, MaskedBadge, RequiredBadge } from '@/components/ui/badge'
import { InlineStructDef } from 'littlehorse-client/proto'
import { FC } from 'react'
import { TypeDisplay } from '../../../components/TypeDisplay'

type Props = {
  fields: InlineStructDef['fields']
}

export const Fields: FC<Props> = ({ fields }) => {
  return (
    <div>
      <h2 className="text-md mb-2 font-bold">Fields</h2>
      {Object.entries(fields).map(([name, fieldDef]) => {
        if (!fieldDef.fieldType) return

        const isRequired = !fieldDef.defaultValue
        const fieldType = fieldDef.fieldType.definedType
        if (!fieldType) return

        return (
          <div key={name} className="mb-1 flex items-center gap-1">
            <IdentifierBadge name={name} />
            <TypeDisplay definedType={fieldType} />
            {isRequired && <RequiredBadge />}
            {fieldDef.fieldType.masked && <MaskedBadge />}
            {fieldDef.defaultValue && (
              <Badge className="bg-green-100">Default: {getVariableValue(fieldDef.defaultValue)}</Badge>
            )}
          </div>
        )
      })}
    </div>
  )
}
