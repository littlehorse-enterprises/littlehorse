import { FormFieldProp } from '@/types'
import { FC } from 'react'
import { FormComponent } from './formType'

export const FormFields: FC<FormFieldProp> = ({ variable: variables, register, formState }) => {
  if (!variables?.varDef?.type) return null

  const Component = FormComponent[variables.varDef.type]
  return <Component variable={variables} register={register} formState={formState} />
}
