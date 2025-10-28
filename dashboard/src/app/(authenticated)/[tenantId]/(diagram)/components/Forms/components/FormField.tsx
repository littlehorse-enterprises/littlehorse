import { VARIABLE_CASE_LABELS } from '@/app/utils'
import { Field, FieldError, FieldLabel } from '@/components/ui/field'
import { cn } from '@/components/utils'
import { VariableValue, WfRunVariableAccessLevel } from 'littlehorse-client/proto'
import { CircleAlert } from 'lucide-react'
import { FC, HTMLInputTypeAttribute } from 'react'
import { useFormContext } from 'react-hook-form'
import { accessLevels } from '../../../wfSpec/[...props]/components/Variables'

interface FormFieldProps {
  label: string
  required?: boolean
  id: string
  type?: HTMLInputTypeAttribute
  variableType: NonNullable<VariableValue['value']>['$case']
  as: React.ElementType
  accessLevel?: WfRunVariableAccessLevel
}

const FormField: FC<FormFieldProps> = ({ label, required = false, id, as, type, accessLevel, variableType }) => {
  const {
    register,
    formState: { errors },
  } = useFormContext()
  const As = as

  //  todo: add variable type to label
  return (
    <Field>
      <FieldLabel className="flex items-center gap-2">
        {label}
        {variableType && (
          <span className={cn('rounded bg-blue-300 p-1 text-xs')}>{VARIABLE_CASE_LABELS[variableType]}</span>
        )}
        {accessLevel && <span className={cn('rounded bg-green-300 p-1 text-xs')}>{accessLevels[accessLevel]}</span>}
        <span className={cn('rounded p-1 text-xs', { 'bg-red-300': required, 'bg-gray-300': !required })}>
          {required ? 'Required' : 'Optional'}
        </span>
      </FieldLabel>

      <As
        id={id}
        {...register(id, { required: required ? `${label} is required` : false })}
        className={cn(errors[id] && 'border-destructive', 'w-fit')}
        type={type}
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
