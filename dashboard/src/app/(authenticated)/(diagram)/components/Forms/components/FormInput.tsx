import React, { FC, useState } from 'react'
import { VariableType } from 'littlehorse-client/proto'
import { FormFieldProp } from '@/types'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { MarkFieldNull } from "./MarkFieldNull"
export const FormInput: FC<FormFieldProp> = props => {
  const [isDisabled, setIsDisabled] = useState(false)
  if (!props.variables?.varDef?.name) return null

  const {
    variables: {
      varDef: { type, name },
      required,
    },
    register,
    formState: { errors },
  } = props

  const variableToType = (variable: VariableType) => {
    switch (variable) {
      case VariableType.INT:
        return 'number'
      case VariableType.STR:
        return 'text'
      default:
        return 'text'
    }
  }

  return (
    <div>
      <div className="flex justify-between">
        <Label htmlFor={name}>
          {name} {required && <span className="text-red-700">*</span>}
        </Label>
        {!required && <MarkFieldNull name={name} setIsDisabled={setIsDisabled}   />}
      </div>
      <Input
        type={variableToType(type)}
        className={errors[name] ? "mb-1 mt-1" : "mb-4 mt-1"}
        id={name}
        disabled={isDisabled} 
        {...register(name, { required: required ? `${name} is required` : false })}
      />
      {errors[name] && <p className="text-sm text-red-700 mb-3">{errors[name]?.message}</p>}
    </div>
  )
}
