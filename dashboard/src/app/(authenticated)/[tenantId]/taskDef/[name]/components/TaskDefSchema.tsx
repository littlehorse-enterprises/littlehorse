import { OutputTypeDisplay } from '@/app/(authenticated)/[tenantId]/components/OutputTypeDisplay'
import { VARIABLE_TYPES } from '@/app/constants'
import { getVariableDefType } from '@/app/utils/variables'
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
          <OutputTypeDisplay outputType={outputType} />
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
                <div className="rounded bg-gray-100 px-2 py-1 font-mono text-fuchsia-500">{varDef.name}</div>
                <div className="rounded bg-yellow-100 p-1 text-xs">{VARIABLE_TYPES[getVariableDefType(varDef)]}</div>
                {varDef.defaultValue && <div>{Object.values(varDef.defaultValue)[0]}</div>}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
