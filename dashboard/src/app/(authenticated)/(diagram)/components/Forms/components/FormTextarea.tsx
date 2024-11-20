import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { FormFieldProp } from '@/types'
import { FC, useState } from 'react'

import { VARIABLE_TYPES } from '@/app/constants'
import { CircleAlert } from 'lucide-react'
import { useFormContext } from 'react-hook-form'
import { accessLevels } from '../../../wfSpec/[...props]/components/Variables'
import { getValidation } from './validation'

export const FormTextarea: FC<FormFieldProp> = props => {
  const [isDisabled, setIsDisabled] = useState(false)
  const { setValue, trigger } = useFormContext()

  if (!props.variables?.varDef?.name) return
  const {
    variables: {
      varDef: { type, name },
      accessLevel,
      required,
    },
    register,
    formState: { errors },
  } = props

  return (
    <div>
      <div className="flex justify-between">
        <Label htmlFor={name} className="flex items-center gap-2">
          {name}
          <span className="rounded bg-green-300 p-1 text-xs">{accessLevels[accessLevel]}</span>
          {required ? (
            <span className="rounded bg-red-300 p-1 text-xs">Required</span>
          ) : (
            <span className="rounded bg-gray-300 p-1 text-xs">Optional</span>
          )}
        </Label>
      </div>
      <Textarea
        className={errors[name] ? 'mb-1 mt-1 min-h-[120px] border-red-700' : 'mb-4 mt-1 rounded-xl'}
        id={name}
        disabled={isDisabled}
        placeholder={`Enter ${VARIABLE_TYPES[type]?.toLowerCase()} value`}
        {...register(name, {
          required: required ? `${name} is required` : false,
          validate: getValidation(type),
          onChange: e => {
            setValue(name, e.target.value || undefined)
            trigger(name)
          },
        })}
        defaultValue=""
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
