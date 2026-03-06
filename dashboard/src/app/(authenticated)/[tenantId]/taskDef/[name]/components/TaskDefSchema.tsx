import { TypeDisplay } from '@/app/(authenticated)/[tenantId]/components/TypeDisplay'
import { IdentifierBadge } from '@/components/ui/badge'
import { TaskDef } from 'littlehorse-client/proto'
import { FC } from 'react'

type Props = {
  spec: TaskDef
}

export const TaskDefSchema: FC<Props> = ({ spec }) => {
  const outputType = spec.returnType

  return (
    <div className="space-y-4">
      <div>
        <h2 className="mb-2 text-lg font-bold">Output</h2>
        <div className="flex flex-wrap items-center gap-2 text-sm text-gray-600">
          {!outputType ? (
            <span className="font-mono text-gray-400">Unknown Output Type</span>
          ) : (
            <TypeDisplay definedType={outputType.returnType?.definedType} />
          )}
        </div>
      </div>

      <div>
        <h2 className="mb-2 text-lg font-bold">Input</h2>
        {spec.inputVars.length === 0 ? (
          <div className="italic">No input variables</div>
        ) : (
          <div className="space-y-1">
            {spec.inputVars.map(varDef => (
              <div key={varDef.name} className="flex items-center gap-1">
                <IdentifierBadge name={varDef.name} />
                <TypeDisplay definedType={varDef.typeDef?.definedType} />
                {varDef.defaultValue && <div>{Object.values(varDef.defaultValue)[0]}</div>}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
