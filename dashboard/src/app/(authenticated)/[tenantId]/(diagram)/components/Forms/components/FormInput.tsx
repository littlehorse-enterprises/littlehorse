import { VARIABLE_TYPES } from '@/app/constants'
import { Input } from '@/components/ui/input'
import { cn } from '@/components/utils'
import { FormFieldProp } from '@/types'
import { VariableType } from 'littlehorse-client/proto'
import { FC, useEffect, useState } from 'react'
import { useFormContext } from 'react-hook-form'
import { BaseFormField } from './BaseFormField'

export const FormInput: FC<FormFieldProp> = props => {
  const [isDisabled, setIsDisabled] = useState(!props.variables?.required)
  const { setValue } = useFormContext()

  useEffect(() => {
    if (!props.variables?.required && props.variables?.varDef?.name) {
      setValue(props.variables.varDef.name, null)
    }
  }, [props.variables, setValue])

  if (!props.variables?.varDef?.name) return null

  const {
    variables: {
      varDef: { type, name },
      required,
    },
    register,
    formState: { errors },
  } = props

  return (
    <BaseFormField {...props} isDisabled={isDisabled} setIsDisabled={setIsDisabled}>
      <Input
        type={type === VariableType.INT || type === VariableType.DOUBLE ? 'number' : 'text'}
        className={cn(errors[name] && 'border-destructive')}
        id={name}
        step={type === VariableType.DOUBLE ? '0.01' : undefined}
        disabled={isDisabled}
        placeholder={`Enter ${VARIABLE_TYPES[type]?.toLowerCase()} value`}
        {...register(name, {
          required: required ? `${name} is required` : false,
          setValueAs: (value: string) => {
            if (type === VariableType.INT || type === VariableType.DOUBLE) {
              const parsed = parseFloat(value)
              return Number.isNaN(parsed) ? undefined : parsed
            }
            return value
          },
        })}
      />
    </BaseFormField>
  )
}
