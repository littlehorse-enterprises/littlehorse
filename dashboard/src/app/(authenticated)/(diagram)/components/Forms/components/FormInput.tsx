import { VARIABLE_TYPES } from '@/app/constants'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { FormFieldProp } from '@/types'
import { VariableType } from 'littlehorse-client/proto'
import { CircleAlert } from 'lucide-react'
import { FC, useState } from 'react'
import { accessLevels } from '../../../wfSpec/[...props]/components/Variables'

export const FormInput: FC<FormFieldProp> = props => {
  const [isDisabled, setIsDisabled] = useState(false)
  if (!props.variables?.varDef?.name) return null

  const {
    variables: {
      varDef: { type, name },
      required,
      accessLevel,
    },
    register,
    formState: { errors },
  } = props

  const variableToType = (variable: VariableType) => {
    switch (variable) {
      case VariableType.INT:
        return 'number'
      case VariableType.DOUBLE:
        return 'number'
      case VariableType.BYTES:
        return 'number'
      case VariableType.STR:
        return 'text'
      default:
        return 'text'
    }
  }

  const setValue = (value: number | string) => {
    if (value === null) return value
    return variableToType(type) === 'number' ? parseFloat(value?.toString()) || undefined : value || undefined
  }

  return (
    <div>
      <div className="mb-2 flex justify-between">
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
      <Input
        type={variableToType(type)}
        className={errors[name] ? 'mb-1 mt-2 border-red-700' : 'mb-4 mt-1'}
        id={name}
        step={type === VariableType.DOUBLE ? '0.01' : undefined}
        disabled={isDisabled}
        placeholder={`Enter ${VARIABLE_TYPES[type]?.toLowerCase()} value`}
        {...register(name, {
          required: required ? `${name} is required` : false,
          setValueAs: setValue,
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
