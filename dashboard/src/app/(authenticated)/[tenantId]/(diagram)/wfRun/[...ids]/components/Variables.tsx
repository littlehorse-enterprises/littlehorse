import { VARIABLE_TYPES } from '@/app/constants'
import { getVariableDefType, getVariableValue } from '@/app/utils'
import { ThreadVarDef, Variable, WfRunVariableAccessLevel } from 'littlehorse-client/proto'
import { FC } from 'react'
import { OverflowText } from '../../../../components/OverflowText'

type VariablesProps = {
  variableDefs: ThreadVarDef[]
  variables: Variable[]
  inheritedVariables: (Variable | null)[]
}

const accessLevels: { [key in WfRunVariableAccessLevel]: string } = {
  PUBLIC_VAR: 'Public',
  INHERITED_VAR: 'Inherited',
  PRIVATE_VAR: 'Private',
  UNRECOGNIZED: '',
}

export const Variables: FC<VariablesProps> = ({ variableDefs, variables, inheritedVariables }) => {
  if (variableDefs.length === 0) return <></>

  const allVariables = [...variables, ...inheritedVariables]

  return (
    <div className="">
      <h2 className="text-md mb-2 font-bold">Variables</h2>
      {variableDefs.map(variable => (
        <div key={variable.varDef?.name} className="mb-1 flex items-center gap-1">
          <span className="rounded	bg-gray-100 px-2 py-1 font-mono text-fuchsia-500">{variable.varDef?.name}</span>
          <span className="rounded bg-yellow-100 p-1 text-xs">
            {VARIABLE_TYPES[getVariableDefType(variable.varDef!)]}
          </span>
          {variable.required && <span className="rounded bg-orange-300 p-1 text-xs">Required</span>}
          {variable.searchable && <span className="rounded bg-blue-300 p-1 text-xs">Searchable</span>}
          <span className="rounded bg-green-300 p-1 text-xs">{accessLevels[variable.accessLevel]}</span>
          <span>=</span>
          <span className="truncate">
            <OverflowText
              className="max-w-96"
              text={
                getVariableValue(allVariables.find(v => v?.id?.name === variable.varDef?.name)?.value)?.toString() ?? ''
              }
            />
          </span>
        </div>
      ))}
    </div>
  )
}
