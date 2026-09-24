import { getStructDef } from '@/app/actions/getStructDef'
import { getPrimitiveFormDefaultValue, getVariableCaseFromType, isStructFieldRequired } from '@/app/utils'
import { TypeBadge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { FieldGroup } from '@/components/ui/field'
import {
  InlineStructDef,
  StructDefId,
  StructField,
  TypeDefinition,
  VariableType,
  VariableValue,
  WfRunVariableAccessLevel,
} from 'littlehorse-client/proto'
import { useParams } from 'next/navigation'
import { ExternalLink } from 'lucide-react'
import { createContext, FC, HTMLInputTypeAttribute, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { useFormContext, useWatch } from 'react-hook-form'
import useSWR from 'swr'
import { STRUCT_FORM_FIELD_PREFIX, useStructFormContext, VariableCase } from '../context/StructFormContext'
import { FormValues } from '../WfRunForm'
import FormField from './FormField'
import VariableFieldHeader from './VariableFieldHeader'
import LinkWithTenant from '@/app/(authenticated)/[tenantId]/components/LinkWithTenant'
import { routes } from '@/app/routes'
import { OverflowText } from '@/app/(authenticated)/[tenantId]/components/OverflowText'
import { VariableTypeToFieldComponent } from './VariableTypeToFieldComponent'
import { ContainerTypeDef, ContainerVariableField } from './ContainerVariableField'

const StructDefParentContext = createContext<{
  parentDisabled: boolean
  nestedStructPath: string[]
  includeDefaultValues: boolean
}>({
  parentDisabled: false,
  nestedStructPath: [],
  includeDefaultValues: false,
})

interface StructPrimitiveFieldProps {
  fieldName: string
  label: string
  description?: string
  component: React.ElementType
  type: HTMLInputTypeAttribute | undefined
  typeDef: TypeDefinition['definedType']
  variableCase: VariableCase
  structPath: string[]
  structDefId?: StructDefId
  protoRequired: boolean
  formRequired: boolean
  masked: boolean
  disabled: boolean
  defaultValue?: VariableValue
  includeDefaultValue: boolean
}

const StructPrimitiveField: FC<StructPrimitiveFieldProps> = ({
  fieldName,
  label,
  description,
  component,
  type,
  typeDef,
  variableCase,
  structPath,
  structDefId,
  protoRequired,
  formRequired,
  masked,
  disabled,
  defaultValue,
  includeDefaultValue,
}) => {
  const structForm = useStructFormContext()
  const { control, getFieldState, setValue } = useFormContext<FormValues>()
  const fieldId = useMemo(() => [STRUCT_FORM_FIELD_PREFIX, ...structPath, fieldName].join('.'), [structPath, fieldName])
  const value = useWatch({ name: fieldId, control })

  const defaultFormValue = useMemo(() => getPrimitiveFormDefaultValue(defaultValue), [defaultValue])

  useEffect(() => {
    if (!disabled) {
      const hasValue = value !== undefined && value !== null && (typeof value !== 'string' || value.trim().length > 0)
      if (!hasValue && defaultFormValue !== undefined) {
        setValue(fieldId, defaultFormValue, { shouldDirty: false })
      }
    }
  }, [defaultFormValue, disabled, fieldId, setValue, value])

  useEffect(() => {
    if (disabled) {
      if (value !== undefined) {
        setValue(fieldId, undefined)
      }
      structForm.clearFieldValue(structPath, fieldName)
      return
    }

    if (value === undefined || value === null || (typeof value === 'string' && value === '')) {
      structForm.clearFieldValue(structPath, fieldName)
      return
    }

    if (!includeDefaultValue && !getFieldState(fieldId).isDirty) {
      structForm.clearFieldValue(structPath, fieldName)
      return
    }

    structForm.setPrimitiveFieldValue(structPath, structDefId, fieldName, variableCase, value)
  }, [
    disabled,
    fieldId,
    fieldName,
    getFieldState,
    includeDefaultValue,
    structDefId,
    structForm,
    structPath,
    value,
    variableCase,
    setValue,
  ])

  return (
    <FormField
      label={label}
      description={description}
      as={component}
      id={fieldId}
      type={type}
      protoRequired={protoRequired}
      formRequired={formRequired}
      typeDef={typeDef}
      masked={masked}
      disabled={disabled}
    />
  )
}

type StructDefGroupCommonProps = {
  name: string
  required: boolean
  accessLevel?: WfRunVariableAccessLevel
  description?: string
  masked?: boolean
  defaultValue?: VariableValue
}

type StructDefGroupProps = StructDefGroupCommonProps &
  ({ structDefId: StructDefId; inlineStructDef?: never } | { structDefId?: never; inlineStructDef: InlineStructDef })

const StructContainerField: FC<{
  fieldName: string
  description?: string
  typeDef: ContainerTypeDef
  structPath: string[]
  structDefId?: StructDefId
  required: boolean
  masked: boolean
  disabled: boolean
  defaultValue?: VariableValue
  includeDefaultValue: boolean
}> = ({
  fieldName,
  description,
  typeDef,
  structPath,
  structDefId,
  required,
  masked,
  disabled,
  defaultValue,
  includeDefaultValue,
}) => {
  const structForm = useStructFormContext()
  const fieldId = useMemo(() => [STRUCT_FORM_FIELD_PREFIX, ...structPath, fieldName].join('.'), [fieldName, structPath])
  const handleValueChange = useCallback(
    (value?: VariableValue) => {
      if (value) {
        structForm.setFieldValue(structPath, structDefId, fieldName, value)
      } else {
        structForm.clearFieldValue(structPath, fieldName)
      }
    },
    [fieldName, structDefId, structForm, structPath]
  )

  return (
    <ContainerVariableField
      name={fieldName}
      fieldId={fieldId}
      typeDef={typeDef}
      required={required}
      description={description}
      masked={masked}
      disabled={disabled}
      defaultValue={defaultValue}
      includeDefaultValue={includeDefaultValue}
      onTypedValueChange={handleValueChange}
    />
  )
}

export const StructDefGroup: FC<StructDefGroupProps> = ({
  structDefId,
  inlineStructDef,
  name: structName,
  required,
  accessLevel,
  description,
  masked,
  defaultValue,
}) => {
  const tenantId = useParams().tenantId as string
  const { unregister } = useFormContext<FormValues>()
  const {
    parentDisabled,
    nestedStructPath,
    includeDefaultValues: parentIncludesDefaultValues,
  } = useContext(StructDefParentContext)
  const structForm = useStructFormContext()
  const defaultsToNull = !required && !defaultValue
  const [isDisabled, setIsDisabled] = useState(parentDisabled || defaultsToNull)

  useEffect(() => {
    setIsDisabled(parentDisabled || defaultsToNull)
  }, [defaultsToNull, parentDisabled])

  const currentStructPath = useMemo(() => [...nestedStructPath, structName], [structName, nestedStructPath])
  const includeDefaultValues = nestedStructPath.length === 0 ? required : parentIncludesDefaultValues

  useEffect(() => {
    const fieldPrefix = [STRUCT_FORM_FIELD_PREFIX, ...currentStructPath].join('.')

    if (isDisabled) {
      structForm.unregisterStructPath(currentStructPath)
      unregister(fieldPrefix)

      return () => {
        structForm.unregisterStructPath(currentStructPath)
        unregister(fieldPrefix)
      }
    }

    structForm.registerStructPath(currentStructPath, structDefId, includeDefaultValues)

    return () => {
      structForm.unregisterStructPath(currentStructPath)
      unregister(fieldPrefix)
    }
  }, [currentStructPath, includeDefaultValues, isDisabled, structDefId, structForm, unregister])

  const defaultStructFieldValues = useMemo<Record<string, StructField>>(() => {
    const union = defaultValue?.value
    if (!union || union.oneofKind !== 'struct' || !union.struct.struct?.fields) return {}
    return union.struct.struct.fields
  }, [defaultValue])

  const { data: structDef } = useSWR(
    structDefId ? `structDef/${tenantId}/${structDefId.name}/${structDefId.version}` : null,
    async () => await getStructDef(tenantId, structDefId!)
  )
  const fields = inlineStructDef?.fields ?? structDef?.structDef?.fields ?? {}

  return (
    <div className="flex flex-col gap-2">
      <VariableFieldHeader
        name={structName}
        description={description}
        typeBadge={<TypeBadge>{inlineStructDef ? 'InlineStruct' : 'Struct'}</TypeBadge>}
        accessLevel={accessLevel}
        required={required}
        masked={masked}
        action={
          !required && (
            <Button
              variant="outline"
              className="h-fit px-3 py-1 text-xs"
              onClick={e => {
                e.preventDefault()
                setIsDisabled(!isDisabled)
              }}
            >
              {isDisabled ? 'Enter Value' : 'Set Null'}
            </Button>
          )
        }
      />
      <FieldGroup className="gap-2 rounded-md border">
        {structDefId && (
          <div className="flex w-full flex-col items-start gap-1 border-b bg-gray-100 p-2">
            <LinkWithTenant
              className="flex w-full items-center gap-2 text-sm"
              href={routes.structDef.detail(structDefId.name, structDefId.version)}
            >
              <span>{`Struct<${structDefId.name},v${structDefId.version}>`}</span>
              <ExternalLink aria-hidden="true" className="size-4 shrink-0" />
            </LinkWithTenant>
          </div>
        )}
        <div className="flex flex-col gap-4 p-3">
          {Object.entries(fields).map(([name, fieldDef]) => {
            const { fieldType, defaultValue: structFieldDefault, description } = fieldDef
            const definedType = fieldType?.definedType
            if (!definedType) return

            const inheritedDefaultValue = defaultStructFieldValues[name]?.value
            const effectiveDefaultValue = inheritedDefaultValue ?? structFieldDefault
            const fieldRequired = isStructFieldRequired({ ...fieldDef, defaultValue: effectiveDefaultValue })

            if (definedType.oneofKind === 'primitiveType') {
              const variableType = definedType.primitiveType
              if (variableType === undefined || variableType === null) return

              const { type, component } = VariableTypeToFieldComponent[variableType]
              const variableCase = getVariableCaseFromType(variableType)

              return (
                <StructPrimitiveField
                  key={name}
                  fieldName={name}
                  label={name}
                  description={description}
                  component={component}
                  type={type}
                  protoRequired={fieldRequired}
                  formRequired={!isDisabled && fieldRequired}
                  masked={Boolean(fieldType?.masked)}
                  typeDef={definedType}
                  variableCase={variableCase as VariableCase}
                  structPath={currentStructPath}
                  structDefId={structDefId}
                  disabled={parentDisabled || isDisabled}
                  defaultValue={effectiveDefaultValue}
                  includeDefaultValue={includeDefaultValues}
                />
              )
            } else if (definedType.oneofKind === 'structDefId') {
              return (
                <StructDefParentContext.Provider
                  key={name}
                  value={{
                    parentDisabled: parentDisabled || isDisabled,
                    nestedStructPath: currentStructPath,
                    includeDefaultValues,
                  }}
                >
                  <StructDefGroup
                    structDefId={definedType.structDefId}
                    name={name}
                    required={fieldRequired}
                    description={description}
                    masked={fieldType?.masked}
                    defaultValue={effectiveDefaultValue}
                  />
                </StructDefParentContext.Provider>
              )
            } else if (definedType.oneofKind === 'inlineStructDef') {
              return (
                <StructDefParentContext.Provider
                  key={name}
                  value={{
                    parentDisabled: parentDisabled || isDisabled,
                    nestedStructPath: currentStructPath,
                    includeDefaultValues,
                  }}
                >
                  <StructDefGroup
                    inlineStructDef={definedType.inlineStructDef}
                    name={name}
                    required={fieldRequired}
                    description={description}
                    masked={fieldType?.masked}
                    defaultValue={effectiveDefaultValue}
                  />
                </StructDefParentContext.Provider>
              )
            } else if (definedType.oneofKind === 'inlineArrayDef' || definedType.oneofKind === 'inlineMapDef') {
              return (
                <StructContainerField
                  key={name}
                  fieldName={name}
                  description={description}
                  typeDef={fieldType as ContainerTypeDef}
                  structPath={currentStructPath}
                  structDefId={structDefId}
                  required={fieldRequired}
                  masked={fieldType.masked}
                  disabled={parentDisabled || isDisabled}
                  defaultValue={effectiveDefaultValue}
                  includeDefaultValue={includeDefaultValues}
                />
              )
            }
          })}
        </div>
      </FieldGroup>
    </div>
  )
}
