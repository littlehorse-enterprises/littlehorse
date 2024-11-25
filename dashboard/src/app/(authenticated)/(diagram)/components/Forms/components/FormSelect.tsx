import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { FormFieldProp } from '@/types'
import { FC, useState } from 'react'
import { useFormContext } from 'react-hook-form'
import { BaseFormField } from './BaseFormField'

export const FormSelect: FC<FormFieldProp> = props => {
  const { register, setValue, getValues, trigger } = useFormContext()
  const [isDisabled] = useState(false)

  if (!props.variable?.varDef?.name) return null

  const {
    variable: {
      varDef: { name },
      required,
    },
  } = props

  const handleChange = (value: string | null) => {
    if (value === null) setValue(name, null)
    else if (value === 'none') setValue(name, undefined)
    else {
      const booleanValue = value === 'true'
      setValue(name, booleanValue)
    }
    trigger(name)
  }

  const value = getValues(name)

  return (
    <BaseFormField variables={props.variable}>
      <Select
        value={value?.toString() || ''}
        onValueChange={handleChange}
        disabled={isDisabled}
        {...register(name, { required: required ? `${name} is required` : false })}
      >
        <SelectTrigger id={name} className="mb-4 mt-1">
          <SelectValue placeholder="Select True or False" />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value="true">True</SelectItem>
          <SelectItem value="false">False</SelectItem>
          <SelectItem value="none">None</SelectItem>
        </SelectContent>
      </Select>
    </BaseFormField>
  )
}
