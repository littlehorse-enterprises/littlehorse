import { ThreadVarDef, VariableType, WfRunVariableAccessLevel } from 'littlehorse-client/proto'
import { FC } from 'react'
import { useFormContext } from 'react-hook-form'
import { formatTypeDefinition } from '@/app/utils'
import { Field, FieldError } from '@/components/ui/field'
import { Textarea } from '@/components/ui/textarea'
import { CircleAlert } from 'lucide-react'
import FormField from './FormField'
import FormLabel from './FormLabel'
import { StructDefGroup } from './StructDefGroup'
import { TimestampVariableField } from './TimestampVariableField'
import { VariableTypeToFieldComponent } from './VariableTypeToFieldComponent'

type ContainerKind = 'inlineArrayDef' | 'inlineMapDef'

/**
 * Text input for Map/Array variables in the Execute WfRun form. The value is entered as the
 * same human-friendly JSON the dashboard displays (a Map is `{"one":1}`, an Array is `[1,2]`)
 * and converted to a typed VariableValue at submit time via `getTypedVariableValueFromTypeDef`.
 */
const ContainerVariableField: FC<{
  name: string
  kind: ContainerKind
  typeLabel: string
  required?: boolean
  accessLevel?: WfRunVariableAccessLevel
  masked?: boolean
}> = ({ name, kind, typeLabel, required, accessLevel, masked }) => {
  const {
    register,
    formState: { errors },
  } = useFormContext()

  const wantsObject = kind === 'inlineMapDef'
  const placeholder = wantsObject ? '{ "key": value }' : '[ value, … ]'

  return (
    <Field>
      <FormLabel label={name} typeLabel={typeLabel} accessLevel={accessLevel} required={required} masked={masked} />
      <Textarea
        id={name}
        placeholder={placeholder}
        className={errors[name] ? 'border-destructive' : undefined}
        {...register(name, {
          required: required ? `${name} is required` : false,
          validate: value => {
            if (!value) return true
            let parsed: unknown
            try {
              parsed = JSON.parse(value)
            } catch {
              return 'Input must be valid JSON'
            }
            if (wantsObject && (parsed === null || typeof parsed !== 'object' || Array.isArray(parsed))) {
              return `Expected a JSON object for ${typeLabel}`
            }
            if (!wantsObject && !Array.isArray(parsed)) {
              return `Expected a JSON array for ${typeLabel}`
            }
            return true
          },
        })}
      />
      {errors[name] && (
        <FieldError className="flex items-center gap-1 text-sm text-destructive">
          <CircleAlert size={16} />
          {String(errors[name]?.message)}
        </FieldError>
      )}
    </Field>
  )
}

interface VariableFormFieldProps {
  variable: ThreadVarDef
}

export const VariableFormField: FC<VariableFormFieldProps> = ({ variable }) => {
  const varDef = variable.varDef
  if (!varDef) return null

  const name = varDef.name
  if (!name) return null

  const definedType = varDef.typeDef?.definedType
  if (!definedType) return null

  if (variable.accessLevel === WfRunVariableAccessLevel.INHERITED_VAR) {
    return (
      <FormLabel
        label={name}
        accessLevel={variable.accessLevel}
        required={variable.required}
        masked={varDef.typeDef?.masked}
      />
    )
  }

  if (definedType.oneofKind === 'primitiveType' && definedType.primitiveType === VariableType.TIMESTAMP) {
    return (
      <TimestampVariableField
        label={name}
        id={name}
        protoRequired={variable.required}
        accessLevel={variable.accessLevel}
        masked={varDef.typeDef?.masked}
      />
    )
  }

  if (definedType.oneofKind === 'primitiveType') {
    const { type, component } = VariableTypeToFieldComponent[definedType.primitiveType]

    return (
      <FormField
        label={name}
        as={component}
        id={name}
        type={type}
        protoRequired={variable.required}
        accessLevel={variable.accessLevel}
        variableType={definedType.primitiveType}
        masked={varDef.typeDef?.masked}
      />
    )
  }

  if (definedType.oneofKind === 'structDefId') {
    return (
      <StructDefGroup
        structDefId={definedType.structDefId}
        name={name}
        required={variable.required}
        masked={varDef.typeDef?.masked}
        defaultValue={varDef.defaultValue}
      />
    )
  }

  if (definedType.oneofKind === 'inlineArrayDef' || definedType.oneofKind === 'inlineMapDef') {
    return (
      <ContainerVariableField
        name={name}
        kind={definedType.oneofKind}
        typeLabel={formatTypeDefinition(definedType)}
        required={variable.required}
        accessLevel={variable.accessLevel}
        masked={varDef.typeDef?.masked}
      />
    )
  }

  return null
}

export default VariableFormField
