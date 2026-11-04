import { ThreadVarDef, VariableType, WfRunVariableAccessLevel } from 'littlehorse-client/proto'
import { FC } from 'react'
import { ContainerTypeDef, ContainerVariableField } from './ContainerVariableField'
import FormField from './FormField'
import VariableFieldHeader from './VariableFieldHeader'
import { StructDefGroup } from './StructDefGroup'
import { TimestampVariableField } from './TimestampVariableField'
import { VariableTypeToFieldComponent } from './VariableTypeToFieldComponent'

interface VariableFormFieldProps {
  variable: ThreadVarDef
}

const VariableFormField: FC<VariableFormFieldProps> = ({ variable }) => {
  const varDef = variable.varDef
  if (!varDef) return null

  const name = varDef.name
  if (!name) return null

  const definedType = varDef.typeDef?.definedType
  if (!definedType) return null

  if (variable.accessLevel === WfRunVariableAccessLevel.INHERITED_VAR) {
    return (
      <VariableFieldHeader
        name={name}
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
    const { type, inputMode, validate, component } = VariableTypeToFieldComponent[definedType.primitiveType]

    return (
      <FormField
        label={name}
        as={component}
        id={name}
        type={type}
        inputMode={inputMode}
        validate={validate}
        protoRequired={variable.required}
        formRequired={variable.required}
        accessLevel={variable.accessLevel}
        typeDef={definedType}
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
        accessLevel={variable.accessLevel}
        masked={varDef.typeDef?.masked}
        defaultValue={varDef.defaultValue}
      />
    )
  }

  if (definedType.oneofKind === 'inlineStructDef') {
    return (
      <StructDefGroup
        inlineStructDef={definedType.inlineStructDef}
        name={name}
        required={variable.required}
        accessLevel={variable.accessLevel}
        masked={varDef.typeDef?.masked}
        defaultValue={varDef.defaultValue}
      />
    )
  }

  if (definedType.oneofKind === 'inlineArrayDef' || definedType.oneofKind === 'inlineMapDef') {
    return (
      <ContainerVariableField
        name={name}
        typeDef={varDef.typeDef as ContainerTypeDef}
        required={variable.required}
        accessLevel={variable.accessLevel}
        masked={varDef.typeDef?.masked}
        defaultValue={varDef.defaultValue}
      />
    )
  }

  return null
}

export default VariableFormField
