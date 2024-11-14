import React, { FC, useState } from 'react'
import { FormFieldProp } from '@/types'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { MarkFieldNull } from './MarkFieldNull'
import { useFormContext } from 'react-hook-form'

export const FormTextarea: FC<FormFieldProp> = props => {
  const [isDisabled, setIsDisabled] = useState(false)
  const { setValue } = useFormContext()

  if (!props.variables?.varDef?.name) return
  const {
    variables: {
      varDef: { name },
      required,
    },
    register,
    formState: { errors },
  } = props
  return (
    <div>
      <div className="flex justify-between">
        <Label htmlFor={name}>
          {name} {required && <span className="text-red-700">*</span>}
        </Label>
        {!required && <MarkFieldNull name={name} setIsDisabled={setIsDisabled} />}
      </div>
      <Textarea
        className={errors[name] ? 'mb-1 mt-1' : 'mb-4 mt-1'}
        id={name}
        disabled={isDisabled}
        {...register(name, {
          required: required ? `${name} is required` : false,
          onChange: e => setValue(name, e.target.value),
        })}
      />
      {errors[name] && <p className="mb-3 text-sm text-red-700">{errors[name]?.message}</p>}
    </div>
  )
}
