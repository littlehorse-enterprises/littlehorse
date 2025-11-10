import { ThreadSpec, WfRunVariableAccessLevel } from 'littlehorse-client/proto'
import { FC } from 'react'
import { TypeDisplay } from '../../../../components/TypeDisplay'

type VariablesProps = Pick<ThreadSpec, 'variableDefs'>

export const accessLevels: { [key in WfRunVariableAccessLevel]: string } = {
  PUBLIC_VAR: 'Public',
  INHERITED_VAR: 'Inherited',
  PRIVATE_VAR: 'Private',
  UNRECOGNIZED: '',
}

export const Variables: FC<VariablesProps> = ({ variableDefs }) => {
  if (variableDefs.length === 0) return <p className="font-semibold">No variables</p>
  return (
    <div className="">
      <h2 className="text-md mb-2 font-bold">Variables</h2>
      {variableDefs.map(variable => (
        <div key={variable.varDef?.name} className="mb-1 flex items-center gap-1">
          <span className="rounded	bg-gray-100 px-2 py-1 font-mono text-fuchsia-500">{variable.varDef?.name}</span>
          <TypeDisplay definedType={variable.varDef?.typeDef?.definedType} />
          {variable.required && <span className="rounded bg-orange-300 p-1 text-xs">Required</span>}
          {variable.searchable && <span className="rounded bg-blue-300 p-1 text-xs">Searchable</span>}
          <span className="rounded bg-green-300 p-1 text-xs">{accessLevels[variable.accessLevel]}</span>
        </div>
      ))}
    </div>
  )
}
