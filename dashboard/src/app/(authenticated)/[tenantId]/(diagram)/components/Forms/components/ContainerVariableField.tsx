import { formatTypeDefinition, getTypedVariableValueFromTypeDef, getVariableValue } from '@/app/utils'
import { Field, FieldError } from '@/components/ui/field'
import { Textarea } from '@/components/ui/textarea'
import { TypeDefinition, VariableValue, WfRunVariableAccessLevel } from 'littlehorse-client/proto'
import { CircleAlert } from 'lucide-react'
import { FC, useEffect, useMemo } from 'react'
import { useFormContext, useWatch } from 'react-hook-form'
import { TypeDefinitionBadge } from './TypeDefinitionBadge'
import VariableFieldHeader from './VariableFieldHeader'

export type ContainerTypeDef = TypeDefinition & {
  definedType: Extract<TypeDefinition['definedType'], { oneofKind: 'inlineArrayDef' | 'inlineMapDef' }>
}

type Props = {
  name: string
  fieldId?: string
  typeDef: ContainerTypeDef
  required?: boolean
  accessLevel?: WfRunVariableAccessLevel
  description?: string
  masked?: boolean
  disabled?: boolean
  defaultValue?: VariableValue
  includeDefaultValue?: boolean
  onTypedValueChange?: (value?: VariableValue) => void
}

export const ContainerVariableField: FC<Props> = ({
  name,
  fieldId = name,
  typeDef,
  required,
  accessLevel,
  description,
  masked,
  disabled,
  defaultValue,
  includeDefaultValue = false,
  onTypedValueChange,
}) => {
  const { control, getFieldState, register, setValue, formState } = useFormContext()
  const value = useWatch({ name: fieldId, control })
  const fieldError = getFieldState(fieldId, formState).error
  const defaultFormValue = useMemo(() => (defaultValue ? getVariableValue(defaultValue) : undefined), [defaultValue])
  const wantsObject = typeDef.definedType.oneofKind === 'inlineMapDef'
  const typeLabel = formatTypeDefinition(typeDef)
  const placeholder = wantsObject ? '{ "key": value }' : '[ value, … ]'

  useEffect(() => {
    if (disabled || value !== undefined || defaultFormValue === undefined) return
    setValue(fieldId, defaultFormValue, { shouldDirty: false })
  }, [defaultFormValue, disabled, fieldId, setValue, value])

  useEffect(() => {
    if (!onTypedValueChange) return
    if (disabled || value === undefined || value === '') {
      onTypedValueChange(undefined)
      return
    }
    if (!includeDefaultValue && !getFieldState(fieldId).isDirty) {
      onTypedValueChange(undefined)
      return
    }

    try {
      onTypedValueChange(getTypedVariableValueFromTypeDef(typeDef, String(value)))
    } catch {
      onTypedValueChange(undefined)
    }
  }, [disabled, fieldId, getFieldState, includeDefaultValue, onTypedValueChange, typeDef, value])

  return (
    <Field>
      <VariableFieldHeader
        name={name}
        description={description}
        typeBadge={<TypeDefinitionBadge typeDef={typeDef} />}
        accessLevel={accessLevel}
        required={required}
        masked={masked}
      />
      <Textarea
        id={fieldId}
        placeholder={placeholder}
        disabled={disabled}
        className={fieldError ? 'border-destructive' : undefined}
        {...register(fieldId, {
          required: required ? `${name} is required` : false,
          validate: input => {
            if (!input) return true
            let parsed: unknown
            try {
              parsed = JSON.parse(input)
            } catch {
              return 'Input must be valid JSON'
            }
            if (wantsObject && (parsed === null || typeof parsed !== 'object' || Array.isArray(parsed))) {
              return `Expected a JSON object for ${typeLabel}`
            }
            if (!wantsObject && !Array.isArray(parsed)) {
              return `Expected a JSON array for ${typeLabel}`
            }
            try {
              getTypedVariableValueFromTypeDef(typeDef, input)
            } catch (error) {
              return error instanceof Error ? error.message : `Invalid value for ${typeLabel}`
            }
            return true
          },
        })}
      />
      {fieldError && (
        <FieldError className="flex items-center gap-1 text-sm text-destructive">
          <CircleAlert size={16} />
          {String(fieldError.message)}
        </FieldError>
      )}
    </Field>
  )
}
