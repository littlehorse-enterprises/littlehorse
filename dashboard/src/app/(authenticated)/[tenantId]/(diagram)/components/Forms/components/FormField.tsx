import { Field, FieldError } from '@/components/ui/field'
import { cn } from '@/components/utils'
import { VariableType, WfRunVariableAccessLevel } from 'littlehorse-client/proto'
import { CircleAlert } from 'lucide-react'
import { FC, HTMLInputTypeAttribute } from 'react'
import { useFormContext } from 'react-hook-form'
import FormLabel from './FormLabel'

interface FormFieldProps {
  label: string
  protoRequired?: boolean
  formRequired?: boolean
  id: string
  type?: HTMLInputTypeAttribute
  variableType?: VariableType
  as: React.ElementType
  accessLevel?: WfRunVariableAccessLevel
  disabled?: boolean
}

const FormField: FC<FormFieldProps> = ({
  label,
  protoRequired = false,
  formRequired = false,
  id,
  as,
  type,
  accessLevel,
  variableType,
  disabled = false,
}) => {
  const {
    register,
    formState: { errors },
  } = useFormContext()
  const As = as

  return (
    <Field>
      <FormLabel label={label} variableType={variableType} accessLevel={accessLevel} required={protoRequired} />

      <As
        id={id}
        {...register(id, { required: formRequired ? `${label} is required` : false })}
        className={cn(errors[id] && 'border-destructive', 'w-fit')}
        type={type}
        disabled={disabled}
        defaultValue={3}
      />

      {errors[id] && (
        <FieldError className="flex items-center gap-1 text-sm text-destructive">
          <CircleAlert size={16} />
          {String(errors[id]?.message)}
        </FieldError>
      )}
    </Field>
  )
}
export default FormField
