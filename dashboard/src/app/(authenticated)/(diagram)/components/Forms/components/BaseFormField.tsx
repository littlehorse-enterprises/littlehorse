import { getVariableValue } from '@/app/utils'
import { Label } from '@/components/ui/label'
import { FormFieldProp } from '@/types'
import { WfRunVariableAccessLevel } from 'littlehorse-client/proto'
import { CircleAlert } from 'lucide-react'
import { useFormContext } from 'react-hook-form'

export const accessLevels: { [key in WfRunVariableAccessLevel]: string } = {
  PUBLIC_VAR: 'Public',
  INHERITED_VAR: 'Inherited',
  PRIVATE_VAR: 'Private',
  UNRECOGNIZED: '',
}

type FormFieldWrapperProps = {
  variables: FormFieldProp['variable']
  children: React.ReactNode
}

export const BaseFormField = ({ variables, children }: FormFieldWrapperProps) => {
  const {
    formState: { errors },
  } = useFormContext()

  if (!variables?.varDef?.name) return null

  const {
    varDef: { name, defaultValue },
    required,
    accessLevel,
  } = variables

  const hasDefaultValue = !!defaultValue

  return (
    <div>
      <div className="mb-2 flex justify-between">
        <Label htmlFor={name} className="center flex items-center gap-2">
          {name}
          <span className="rounded bg-green-300 p-1 text-xs">{accessLevels[accessLevel]}</span>
          {required ? (
            <span className="rounded bg-red-300 p-1 text-xs">Required</span>
          ) : (
            <span className="rounded bg-gray-300 p-1 text-xs">Optional</span>
          )}
          {hasDefaultValue && (
            <span className="rounded bg-blue-300 p-1 text-xs">
              Default: {getVariableValue(defaultValue)?.toString()}
            </span>
          )}
        </Label>
      </div>

      <div className="w-full">{children}</div>
      {errors[name] && (
        <p className="mt-2 flex items-center gap-1 text-sm text-destructive">
          <CircleAlert size={16} />
          {errors[name]?.message?.toString()}
        </p>
      )}
    </div>
  )
}
