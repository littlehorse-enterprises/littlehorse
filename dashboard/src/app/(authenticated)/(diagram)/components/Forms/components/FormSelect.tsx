import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { FormFieldProp } from '@/types'
import { FC, useEffect, useState } from 'react'
import { useFormContext } from 'react-hook-form'
import { BaseFormField } from './BaseFormField'
import { cn } from '@/components/utils'

export const FormSelect: FC<FormFieldProp> = props => {
  const [isDisabled, setIsDisabled] = useState(!props.variables?.required)
  const { register, setValue, getValues, trigger } = useFormContext()

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
  } = props

  const handleChange = (value: string) => {
    const booleanValue = value === 'true'
    setValue(name, booleanValue)
    trigger(name)
  }

  const value = getValues(name)

  return (
    <BaseFormField {...props} isDisabled={isDisabled} setIsDisabled={setIsDisabled}>
      <Select
        value={value?.toString() || ''}
        onValueChange={handleChange}
        disabled={isDisabled}
        {...register(name, { required: required ? `${name} is required` : false })}
      >
        <SelectTrigger id={name} className={cn(props.formState.errors[name] && 'border-destructive')}>
          <SelectValue placeholder="Select True or False" />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value="true">True</SelectItem>
          <SelectItem value="false">False</SelectItem>
        </SelectContent>
      </Select>
    </BaseFormField>
  )
}
