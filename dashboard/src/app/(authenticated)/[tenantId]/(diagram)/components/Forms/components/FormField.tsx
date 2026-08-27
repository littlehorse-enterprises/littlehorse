import { Field, FieldError } from '@/components/ui/field'
import { cn } from '@/components/utils'
import { TypeDefinition, WfRunVariableAccessLevel } from 'littlehorse-client/proto'
import { CircleAlert } from 'lucide-react'
import { FC, HTMLInputTypeAttribute } from 'react'
import { useFormContext } from 'react-hook-form'
import VariableFieldHeader from './VariableFieldHeader'
import { TypeDefinitionBadge } from './TypeDefinitionBadge'
import type { PrimitiveFieldConfig } from './VariableTypeToFieldComponent'

interface FormFieldProps {
  label: string
  description?: string
  protoRequired?: boolean
  formRequired?: boolean
  id: string
  type?: HTMLInputTypeAttribute
  inputMode?: PrimitiveFieldConfig['inputMode']
  validate?: PrimitiveFieldConfig['validate']
  typeDef?: TypeDefinition['definedType']
  as: React.ElementType
  accessLevel?: WfRunVariableAccessLevel
  masked?: boolean
  disabled?: boolean
}

const FormField: FC<FormFieldProps> = ({
  label,
  description,
  protoRequired = false,
  formRequired = false,
  id,
  as,
  type,
  inputMode,
  validate,
  accessLevel,
  typeDef,
  masked,
  disabled = false,
}) => {
  const {
    register,
    formState: { errors },
  } = useFormContext()
  const As = as

  return (
    <Field>
      <VariableFieldHeader
        name={label}
        description={description}
        typeBadge={<TypeDefinitionBadge typeDef={typeDef} />}
        accessLevel={accessLevel}
        required={protoRequired}
        masked={masked}
      />

      <As
        id={id}
        {...register(id, { required: formRequired ? `${label} is required` : false, validate })}
        className={cn(errors[id] && 'border-destructive', 'w-fit')}
        type={type}
        inputMode={inputMode}
        disabled={disabled}
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
