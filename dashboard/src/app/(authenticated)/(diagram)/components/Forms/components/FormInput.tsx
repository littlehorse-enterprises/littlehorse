import { VARIABLE_TYPES } from '@/app/constants'
import { Input } from '@/components/ui/input'
import { FormFieldProp } from '@/types'
import { VariableType } from 'littlehorse-client/proto'
import { FC } from 'react'
import { useFormContext } from 'react-hook-form'
import { BaseFormField } from './BaseFormField'

export const FormInput: FC<FormFieldProp> = props => {
  const { register } = useFormContext()

  if (!props.variable?.varDef?.name) return null

  const {
    variable: {
      varDef: { type, name },
      required,
    },
  } = props

  return (
    <BaseFormField variables={props.variable}>
      <Input
        type={type === VariableType.DOUBLE || type === VariableType.INT ? 'number' : 'text'}
        id={name}
        step={type === VariableType.DOUBLE ? '0.01' : undefined}
        placeholder={`Enter ${VARIABLE_TYPES[type]?.toLowerCase()} value`}
        {...register(name, {
          required: required ? `${name} is required` : false,
          setValueAs: value => {
            if (value === '') return undefined

            if ((type === VariableType.DOUBLE || type === VariableType.INT) && value) {
              return parseFloat(value)
            }

            return value
          },
        })}
      />
    </BaseFormField>
  )
}
