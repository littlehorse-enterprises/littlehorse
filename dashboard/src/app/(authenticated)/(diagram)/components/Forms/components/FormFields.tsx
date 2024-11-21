import React, { FC } from 'react'
import { FormComponent } from './formType'
import { VariableType, ThreadVarDef } from 'littlehorse-client/proto'
import { FieldValues, UseFormRegister, FormState } from 'react-hook-form'

type Prop = {
  variables: ThreadVarDef
  register: UseFormRegister<FieldValues>
  formState: FormState<FieldValues>
}
export const FormFields: FC<Prop> = props => {
  const type = props.variables?.varDef?.type as VariableType
  if (!type) return
  const Component = FormComponent[type]
  return <Component {...props} />
}
