import { getVariableDefType } from '@/app/utils/variables'
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
      varDef: { name },
      required,
    },
    register,
    formState: { errors },
  } = props

  const type = getVariableDefType(props.variables.varDef)

  return (
    <BaseFormField {...props} isDisabled={isDisabled} setIsDisabled={setIsDisabled}>
      <Textarea
        className={cn(errors[name] && 'border-destructive')}
        id={name}
        disabled={isDisabled}
        placeholder={`Enter ${type.toLowerCase()} value`}
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
