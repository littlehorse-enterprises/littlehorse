import React, { FC, useState } from 'react'
import { FormFieldProp } from '@/types'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { MarkFieldNull } from './MarkFieldNull'

export const FormTextarea: FC<FormFieldProp> = props => {
  const [isDisabled, setIsDisabled] = useState(false)
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
      {!required && <MarkFieldNull name={name} setIsDisabled={setIsDisabled}   />}
      </div>
      <Textarea
        className="mb-4 mt-1"
        id={name}
        disabled={isDisabled} 
        {...register(name, { required: required ? `${name} is required` : false })}
      />
      {errors[name] && <p className="text-red-700 text-sm">{errors[name]?.message}</p>}
    </div>
  )
}
