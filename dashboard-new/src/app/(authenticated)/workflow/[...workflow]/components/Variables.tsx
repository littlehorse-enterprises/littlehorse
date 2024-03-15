import { VariableType } from 'littlehorse-client/dist/proto/common_enums'
import { ThreadSpec, WfRunVariableAccessLevel, WfSpec } from 'littlehorse-client/dist/proto/wf_spec'
import React, { FC } from 'react'

type VariablesProps = Pick<ThreadSpec, 'variableDefs'>

const variableTypes: { [key in VariableType]: string } = {
  JSON_OBJ: 'JSON Object',
  JSON_ARR: 'JSON Array',
  DOUBLE: 'Double',
  BOOL: 'Boolean',
  STR: 'String',
  INT: 'Integer',
  BYTES: 'Bytes',
  UNRECOGNIZED: 'Unrecognized',
}

const accessLevels: { [key in WfRunVariableAccessLevel]: string } = {
  PUBLIC_VAR: 'Public',
  INHERITED_VAR: 'Inherited',
  PRIVATE_VAR: 'Private',
  UNRECOGNIZED: '',
}
export const Variables: FC<VariablesProps> = ({ variableDefs }) => {
  if (variableDefs.length === 0) return <></>
  return (
    <div className="">
      <h2 className="text-md font-bold mb-2">Variables</h2>
      {variableDefs.map(variable => (
        <div key={variable.varDef?.name} className="flex items-center gap-1 mb-1">
          <span className="text-fuchsia-500	font-mono bg-gray-100 rounded py-1 px-2">{variable.varDef?.name}</span>
          <span className="text-xs bg-yellow-100 rounded p-1">{variableTypes[variable.varDef!.type]}</span>
          {variable.required && <span className="text-xs bg-orange-300 rounded p-1">Required</span>}
          {variable.searchable && <span className="text-xs bg-blue-300 rounded p-1">Searchable</span>}
          <span className="text-xs bg-green-300 rounded p-1">{accessLevels[variable.accessLevel]}</span>
        </div>
      ))}
    </div>
  )
}
