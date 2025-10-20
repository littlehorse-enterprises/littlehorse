import { getVariableValue } from '@/app/utils'
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
            <span className="rounded bg-gray-100 px-2 py-1 font-mono text-fuchsia-500">{name}</span>
            <TypeDisplay
              definedType={fieldType}
            />
            {isRequired && <span className="rounded bg-orange-300 p-1 text-xs">Required</span>}
            {fieldDef.fieldType.masked && <span className="rounded bg-red-100 p-1 text-xs">Masked</span>}
            {fieldDef.defaultValue && (
              <span className="rounded bg-green-100 p-1 text-xs">
                Default: {getVariableValue(fieldDef.defaultValue)}
              </span>
            )}
          </div>
        )
      })}
    </div>
  )
}
