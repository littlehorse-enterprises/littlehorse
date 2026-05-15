import { Field, FieldError } from '@/components/ui/field'
import { VariableType, WfRunVariableAccessLevel } from 'littlehorse-client/proto'
import { CircleAlert } from 'lucide-react'
import { FC } from 'react'
import { Controller, useFormContext } from 'react-hook-form'
import FormLabel from './FormLabel'
import { TimestampPicker } from './TimestampPicker'

type TimestampVariableFieldProps = {
  label: string
  id: string
  protoRequired?: boolean
  accessLevel?: WfRunVariableAccessLevel
  masked?: boolean
  disabled?: boolean
}

export const TimestampVariableField: FC<TimestampVariableFieldProps> = ({
  label,
  id,
  protoRequired = false,
  accessLevel,
  masked,
  disabled = false,
}) => {
  const {
    control,
    formState: { errors },
  } = useFormContext()

  return (
    <Field>
      <FormLabel
        label={label}
        variableType={VariableType.TIMESTAMP}
        accessLevel={accessLevel}
        required={protoRequired}
        masked={masked}
      />

      <Controller
        name={id}
        control={control}
        rules={{ required: protoRequired ? `${label} is required` : false }}
        render={({ field }) => (
          <TimestampPicker id={id} value={field.value ?? ''} onChange={field.onChange} disabled={disabled} />
        )}
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
