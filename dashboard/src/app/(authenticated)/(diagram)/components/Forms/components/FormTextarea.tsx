import { VARIABLE_TYPES } from '@/app/constants'
import { Textarea } from '@/components/ui/textarea'
import { FormFieldProp } from '@/types'
import { FC } from 'react'
import { useFormContext } from 'react-hook-form'
import { BaseFormField } from './BaseFormField'
import { getValidation } from './validation'

export const FormTextarea: FC<FormFieldProp> = props => {
  const { setValue, trigger, register } = useFormContext()

  if (!props.variable?.varDef?.name) return null

  const {
    variable: {
      varDef: { type, name },
      required,
    },
  } = props

  return (
    <BaseFormField variables={props.variable}>
      <Textarea
        id={name}
        placeholder={`Enter ${VARIABLE_TYPES[type]?.toLowerCase()} value`}
        {...register(name, {
          required: required ? `${name} is required` : false,
          validate: getValidation(type),
          onChange: e => {
            setValue(name, e.target.value || undefined)
            trigger(name)
          },
        })}
        defaultValue=""
      />
    </BaseFormField>
  )
}
