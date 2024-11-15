import React, { FC, useEffect, useState } from 'react'
import { FormFieldProp } from '@/types'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { MarkFieldNull } from './MarkFieldNull'
import { useFormContext } from 'react-hook-form'
import { CircleAlert } from 'lucide-react'

export const FormTextarea: FC<FormFieldProp> = props => {
  const [isDisabled, setIsDisabled] = useState(false)
  const { setValue, trigger } = useFormContext()

  if (!props.variables?.varDef?.name) return
  const {
    variables: {
      varDef: { name },
      required,
    },
    register,
    formState: { errors },
  } = props

  useEffect(() => {
    setValue(name, '')
  }, [name, setValue])

  return (
    <div>
      <div className="flex justify-between">
        <Label htmlFor={name} className="text-gray-700">
          {name} {required && <span className="text-red-700">*</span>}
        </Label>
        {!required && <MarkFieldNull name={name} setIsDisabled={setIsDisabled} />}
      </div>
      <Textarea
        className={errors[name] ? 'mb-1 mt-1 border-red-700' : 'mb-4 mt-1 rounded-xl border-sky-600'}
        id={name}
        disabled={isDisabled}
        {...register(name, {
          required: required ? `${name} is required` : false,
          onChange: e => {
            setValue(name, e.target.value)
            trigger(name)
          },
        })}
      />
      {errors[name] && (
        <p className="mb-3 flex items-center gap-1 text-sm text-red-700">
          <CircleAlert size={16} />
          {errors[name]?.message}
        </p>
      )}
    </div>
  )
}
