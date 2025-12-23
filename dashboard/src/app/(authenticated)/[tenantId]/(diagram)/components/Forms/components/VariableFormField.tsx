import { VariableTypeToFieldComponent } from '@/app/utils'
import { ThreadVarDef, WfRunVariableAccessLevel } from 'littlehorse-client/proto'
import { FC } from 'react'
import FormField from './FormField'
import FormLabel from './FormLabel'
import { StructDefGroup } from './StructDefGroup'

interface VariableFormFieldProps {
  variable: ThreadVarDef
}

export const VariableFormField: FC<VariableFormFieldProps> = ({ variable }) => {
  const varDef = variable.varDef
  if (!varDef) return null

  const name = varDef.name
  if (!name) return null

  const definedType = varDef.typeDef?.definedType
  if (!definedType) return null

  if (variable.accessLevel === WfRunVariableAccessLevel.INHERITED_VAR) {
    return <FormLabel label={name} accessLevel={variable.accessLevel} required={variable.required} />
  }

  if (definedType.$case === 'primitiveType') {
    const { type, component } = VariableTypeToFieldComponent[definedType.value]

    return (
      <FormField
        label={name}
        as={component}
        id={name}
        type={type}
        protoRequired={variable.required}
        accessLevel={variable.accessLevel}
        variableType={definedType.value}
      />
    )
  }

  if (definedType.$case === 'structDefId') {
    return (
      <StructDefGroup
        structDefId={definedType.value}
        name={name}
        required={variable.required}
        defaultValue={varDef.defaultValue}
      />
    )
  }

  return null
}

export default VariableFormField
