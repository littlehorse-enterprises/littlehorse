import React, { FC, useState } from 'react'
import { Label } from '@/components/ui/label'
import { FormFieldProp } from '@/types'
import { SwitchButton } from '@/components/ui/switch'
import { MarkFieldNull } from './MarkFieldNull'

export const FormSwitch: FC<FormFieldProp> = props => {
  const [isDisabled, setIsDisabled] = useState(false)
  if (!props.variables?.varDef?.name) return
  const {
    variables: {
      varDef: { type, name },
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
   
      <div className="mb-4 mt-1">
        <SwitchButton  disabled={isDisabled}  />
      </div>
      {errors[name] && <p className="text-red-700 text-sm">{errors[name]?.message}</p>}
    </div>
  )
}
