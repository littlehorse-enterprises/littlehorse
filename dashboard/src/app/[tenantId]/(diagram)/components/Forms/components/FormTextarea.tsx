import { VARIABLE_TYPES } from '@/app/constants'
import { Textarea } from '@/components/ui/textarea'
import { cn } from '@/components/utils'
import { FormFieldProp } from '@/types'
import { FC, useEffect, useState } from 'react'
import { useFormContext } from 'react-hook-form'
import { BaseFormField } from './BaseFormField'
import { getValidation } from './validation'

export const FormTextarea: FC<FormFieldProp> = props => {
  const [isDisabled, setIsDisabled] = useState(!props.variables?.required)
  const { setValue, trigger } = useFormContext()

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
      <Textarea
        className={cn(errors[name] && 'border-destructive')}
        id={name}
        disabled={isDisabled}
        placeholder={`Enter ${VARIABLE_TYPES[type]?.toLowerCase()} value`}
        {...register(name, {
          required: required ? `${name} is required` : false,
          validate: getValidation(type),
          onChange: e => {
            setValue(name, e.target.value)
            trigger(name)
          },
        })}
        defaultValue=""
      />
    </BaseFormField>
  )
}
