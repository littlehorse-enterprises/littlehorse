import { getVariableValue } from '@/app/utils'
import { ThreadVarDef, Variable, WfRunVariableAccessLevel } from 'littlehorse-client/proto'
import { FC } from 'react'
import { OverflowText } from '../../../../components/OverflowText'
import { TypeDisplay } from '../../../../components/TypeDisplay'

type VariablesProps = {
  variableDefs: ThreadVarDef[]
  variables: Variable[]
}

const accessLevels: { [key in WfRunVariableAccessLevel]: string } = {
  PUBLIC_VAR: 'Public',
  INHERITED_VAR: 'Inherited',
  PRIVATE_VAR: 'Private',
  UNRECOGNIZED: '',
}

export const Variables: FC<VariablesProps> = ({ variableDefs, variables }) => {
  if (variableDefs.length === 0) return <></>

  return (
    <div>
      <h2 className="text-md mb-2 font-bold">Variables</h2>
      {variableDefs.map(variable => (
        <div key={variable.varDef?.name} className="mb-1 flex items-center gap-1">
          <span className="rounded	bg-gray-100 px-2 py-1 font-mono text-fuchsia-500">{variable.varDef?.name}</span>
          <TypeDisplay definedType={variable.varDef?.typeDef?.definedType} />
          {variable.required && <span className="rounded bg-orange-300 p-1 text-xs">Required</span>}
          {variable.searchable && <span className="rounded bg-blue-300 p-1 text-xs">Searchable</span>}
          {variable.accessLevel && <span className="rounded bg-blue-300 p-1 text-xs">{variable.accessLevel}</span>}
          <span className="rounded bg-green-300 p-1 text-xs">{accessLevels[variable.accessLevel]}</span>
          <span>=</span>
          <span className="truncate">
            <OverflowText className="max-w-96" text={getVariableValueForVariableDef(variable, variables)} />
          </span>
        </div>
      ))}
    </div>
  )
}

const getVariableValueForVariableDef = (variableDef: ThreadVarDef, variables: Variable[]): string => {
  const variable = variables.find(v => v?.id?.name === variableDef.varDef?.name)
  if (!variable || !variable.value) return ''
  if (variable.masked) return '**masked**'
  return getVariableValue(variable.value)
}
