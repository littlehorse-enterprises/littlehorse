import { getVariableDefType } from '@/app/utils/variables'
import { Input } from '@/components/ui/input'
import { cn } from '@/components/utils'
import { FormFieldProp } from '@/types'
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
      varDef: { name },
      required,
    },
    register,
    formState: { errors },
  } = props

  const type = getVariableDefType(props.variables.varDef)

  return (
    <BaseFormField {...props} isDisabled={isDisabled} setIsDisabled={setIsDisabled}>
      <Input
        type={type === 'int' || type === 'double' ? 'number' : 'text'}
        className={cn(errors[name] && 'border-destructive')}
        id={name}
        step={type === 'double' ? '0.01' : undefined}
        disabled={isDisabled}
        placeholder={`Enter ${type} value`}
        {...register(name, {
          required: required ? `${name} is required` : false,
          setValueAs: (value: string) => {
            if (type === 'int' || type === 'double') {
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
