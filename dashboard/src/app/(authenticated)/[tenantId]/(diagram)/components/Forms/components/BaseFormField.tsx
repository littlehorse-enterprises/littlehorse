import { Label } from '@/components/ui/label'
import { FormFieldProp } from '@/types'
import { CircleAlert } from 'lucide-react'
import { FC, ReactNode } from 'react'
import { useFormContext } from 'react-hook-form'
import { accessLevels } from '../../../wfSpec/[...props]/components/Variables'
import { Button } from '@/components/ui/button'
import { DOT_REPLACEMENT_PATTERN } from '../../Modals/ExecuteWorkflowRun'

type BaseFormFieldProps = FormFieldProp & {
  children: ReactNode
  isDisabled: boolean
  setIsDisabled: (disabled: boolean) => void
}

export const BaseFormField: FC<BaseFormFieldProps> = ({
  variables,
  formState: { errors },
  children,
  isDisabled,
  setIsDisabled,
}) => {
  const { setValue, trigger } = useFormContext()

  if (!variables?.varDef?.name) return null

  const {
    varDef: { name },
    required,
    accessLevel,
  } = variables

  const handleSetNull = () => {
    setValue(name, null)
    trigger(name)
    setIsDisabled(true)
  }

  const handleEnable = () => {
    setIsDisabled(false)
  }

  return (
    <div>
      <div className="mb-2 flex justify-between">
        <Label htmlFor={name} className="flex items-center gap-2">
          {name.replace(DOT_REPLACEMENT_PATTERN, '.')}
          <span className="rounded bg-green-300 p-1 text-xs">{accessLevels[accessLevel]}</span>
          {required ? (
            <span className="rounded bg-red-300 p-1 text-xs">Required</span>
          ) : (
            <span className="rounded bg-gray-300 p-1 text-xs">Optional</span>
          )}
        </Label>
        {!required && (
          <Button
            type="button"
            variant="outline"
            size="sm"
            onClick={isDisabled ? handleEnable : handleSetNull}
            className="h-8"
          >
            {isDisabled ? 'Enter Value' : 'Set Null'}
          </Button>
        )}
      </div>
      {children}
      {errors[name] && (
        <p className="mt-2 flex items-center gap-1 text-sm text-destructive">
          <CircleAlert size={16} />
          {errors[name]?.message}
        </p>
      )}
    </div>
  )
}
