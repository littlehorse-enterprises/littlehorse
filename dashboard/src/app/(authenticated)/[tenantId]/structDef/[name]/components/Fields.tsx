import { VARIABLE_TYPES } from '@/app/constants'
import { getVariableCaseFromType, getVariableValue } from '@/app/utils'
import { InlineStructDef } from 'littlehorse-client/proto'
import { FC } from 'react'
import LinkWithTenant from '../../../components/LinkWithTenant'

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
        console.log(fieldType)

        return (
          <div key={name} className="mb-1 flex items-center gap-1">
            <span className="rounded bg-gray-100 px-2 py-1 font-mono text-fuchsia-500">{name}</span>
            <span className="rounded bg-yellow-100 p-1 text-xs">
              {fieldType?.$case === 'structDefId' ? (
                <LinkWithTenant
                  className="flex underline"
                  href={`/structDef/${fieldType.value.name}/${fieldType.value.version}`}
                >
                  Struct
                </LinkWithTenant>
              ) : (
                VARIABLE_TYPES[getVariableCaseFromType(fieldType?.value)]
              )}
            </span>
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
