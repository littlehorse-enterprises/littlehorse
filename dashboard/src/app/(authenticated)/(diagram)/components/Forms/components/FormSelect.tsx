import React, { FC, useState, useEffect } from 'react'
import { useFormContext } from 'react-hook-form'
import { FormFieldProp } from '@/types'
import { Label } from '@/components/ui/label'
import { MarkFieldNull } from './MarkFieldNull'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { CircleAlert } from 'lucide-react'

export const FormSelect: FC<FormFieldProp> = props => {
  const {
    control,
    register,
    setValue,
    formState: { errors },
    getValues,
    trigger,
  } = useFormContext()
  const [isDisabled, setIsDisabled] = useState(false)

  if (!props.variables?.varDef?.name) return null

  const {
    variables: {
      varDef: { name },
      required,
    },
  } = props

  const handleChange = (value: string) => {
    const booleanValue = value === 'true' ? true : value === 'false' ? false : undefined
    setValue(name, booleanValue)
    trigger(name)
  }

  const value = getValues(name)

  useEffect(() => {
    setValue(name, '')
  }, [name, setValue])

  return (
    <div>
      <div className="mb-2 flex justify-between">
        <Label htmlFor={name}>
          {name} {required && <span className="text-red-700">*</span>}
        </Label>
        {!required && <MarkFieldNull name={name} setIsDisabled={setIsDisabled} />}
      </div>
      <Select
        value={value?.toString() || ''}
        onValueChange={handleChange}
        disabled={isDisabled}
        {...register(name, { required: required ? `${name} is required` : false })}
      >
        <SelectTrigger id={name} className={errors[name] ? 'mb-1 mt-1 border-red-700' : 'mb-4 mt-1 border-sky-600'}>
          <SelectValue placeholder="Select true or false" />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value="true">True</SelectItem>
          <SelectItem value="false">False</SelectItem>
        </SelectContent>
      </Select>
      {errors[name]?.message && (
        <p className="mb-3 flex items-center gap-1 text-sm text-red-700">
          <CircleAlert size={16} />
          {String(errors[name]?.message)}
        </p>
      )}
    </div>
  )
}
