import { getVariableDefType } from '@/app/utils'
import { ThreadVarDef } from 'littlehorse-client/proto'
import { FC } from 'react'
import { FieldValues, FormState, UseFormRegister } from 'react-hook-form'
import { FormComponent } from './formType'

type Prop = {
  variables: ThreadVarDef
  register: UseFormRegister<FieldValues>
  formState: FormState<FieldValues>
}
export const FormFields: FC<Prop> = props => {
  if (!props.variables?.varDef) return
  const type = getVariableDefType(props.variables.varDef)
  if (!type) return
  const Component = FormComponent[type]
  return <Component {...props} />
}
