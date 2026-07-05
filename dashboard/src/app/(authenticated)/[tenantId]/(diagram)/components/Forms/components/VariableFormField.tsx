import { ThreadVarDef, VariableType, WfRunVariableAccessLevel } from 'littlehorse-client/proto'
import { FC } from 'react'
import FormField from './FormField'
import FormLabel from './FormLabel'
import { StructDefGroup } from './StructDefGroup'
import { TimestampVariableField } from './TimestampVariableField'
import { VariableTypeToFieldComponent } from './VariableTypeToFieldComponent'

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
    return (
      <FormLabel
        label={name}
        accessLevel={variable.accessLevel}
        required={variable.required}
        masked={varDef.typeDef?.masked}
      />
    )
  }

  if (definedType.oneofKind === 'primitiveType' && definedType.primitiveType === VariableType.TIMESTAMP) {
    return (
      <TimestampVariableField
        label={name}
        id={name}
        protoRequired={variable.required}
        accessLevel={variable.accessLevel}
        masked={varDef.typeDef?.masked}
      />
    )
  }

  if (definedType.oneofKind === 'primitiveType') {
    const { type, component } = VariableTypeToFieldComponent[definedType.primitiveType]

    return (
      <FormField
        label={name}
        as={component}
        id={name}
        type={type}
        protoRequired={variable.required}
        accessLevel={variable.accessLevel}
        variableType={definedType.primitiveType}
        masked={varDef.typeDef?.masked}
      />
    )
  }

  if (definedType.oneofKind === 'structDefId') {
    return (
      <StructDefGroup
        structDefId={definedType.structDefId}
        name={name}
        required={variable.required}
        masked={varDef.typeDef?.masked}
        defaultValue={varDef.defaultValue}
      />
    )
  }

  return null
}

export default VariableFormField
