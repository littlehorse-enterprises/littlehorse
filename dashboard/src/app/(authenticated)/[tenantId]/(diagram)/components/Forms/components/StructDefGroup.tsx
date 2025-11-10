import { getStructDef } from '@/app/actions/getStructDef'
import { getVariableCaseFromType, VariableTypeToFieldComponent } from '@/app/utils'
import { Button } from '@/components/ui/button'
import { FieldGroup } from '@/components/ui/field'
import { StructDefId, StructField, VariableType, VariableValue } from 'littlehorse-client/proto'
import { useParams } from 'next/navigation'
import { createContext, FC, HTMLInputTypeAttribute, useContext, useEffect, useMemo, useState } from 'react'
import { useFormContext, useWatch } from 'react-hook-form'
import useSWR from 'swr'
import { STRUCT_FORM_FIELD_PREFIX, useStructFormContext, VariableCase } from '../context/StructFormContext'
import { FormValues } from '../WfRunForm'
import FormField from './FormField'
import FormLabel from './FormLabel'

const StructDefParentContext = createContext<{ parentDisabled: boolean; nestedStructPath: string[] }>({
  parentDisabled: false,
  nestedStructPath: [],
})

interface StructPrimitiveFieldProps {
  fieldName: string
  label: string
  component: React.ElementType
  type: HTMLInputTypeAttribute | undefined
  variableType: VariableType
  variableCase: VariableCase
  structPath: string[]
  structDefId: StructDefId
  protoRequired: boolean
  formRequired: boolean
  disabled: boolean
  defaultValue?: VariableValue
}

const StructPrimitiveField: FC<StructPrimitiveFieldProps> = ({
  fieldName,
  label,
  component,
  type,
  variableType,
  variableCase,
  structPath,
  structDefId,
  protoRequired,
  formRequired,
  disabled,
  defaultValue,
}) => {
  const structForm = useStructFormContext()
  const { control, setValue } = useFormContext<FormValues>()
  const fieldId = useMemo(() => [STRUCT_FORM_FIELD_PREFIX, ...structPath, fieldName].join('.'), [structPath, fieldName])
  const value = useWatch({ name: fieldId, control })

  const defaultFormValue = useMemo(() => {
    const union = defaultValue?.value
    if (!union) return undefined

    switch (union.$case) {
      case 'bool':
        return union.value ? 'true' : 'false'
      case 'int':
      case 'double':
        return union.value
      case 'str':
      case 'jsonObj':
      case 'jsonArr':
        return union.value
      default:
        return undefined
    }
  }, [defaultValue])

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

    structForm.setPrimitiveFieldValue(structPath, structDefId, fieldName, variableCase, value)
  }, [disabled, fieldId, fieldName, structDefId, structForm, structPath, value, variableCase, setValue])

  return (
    <FormField
      label={label}
      as={component}
      id={fieldId}
      type={type}
      protoRequired={protoRequired}
      formRequired={formRequired}
      variableType={variableType}
      disabled={disabled}
    />
  )
}

type StructDefGroupProps = {
  structDefId: StructDefId
  name: string
  required: boolean
  defaultValue?: VariableValue
}

export const StructDefGroup: FC<StructDefGroupProps> = ({ structDefId, name: structName, required, defaultValue }) => {
  const tenantId = useParams().tenantId as string
  const { unregister } = useFormContext<FormValues>()
  const { parentDisabled, nestedStructPath } = useContext(StructDefParentContext)
  const structForm = useStructFormContext()
  const [isDisabled, setIsDisabled] = useState(parentDisabled)

  useEffect(() => {
    setIsDisabled(parentDisabled)
  }, [parentDisabled])

  const currentStructPath = useMemo(() => [...nestedStructPath, structName], [structName, nestedStructPath])

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

    structForm.registerStructPath(currentStructPath, structDefId)

    return () => {
      structForm.unregisterStructPath(currentStructPath)
      unregister(fieldPrefix)
    }
  }, [currentStructPath, isDisabled, structDefId, structForm, unregister])

  const defaultStructFieldValues = useMemo<Record<string, StructField>>(() => {
    const union = defaultValue?.value
    if (!union || union.$case !== 'struct' || !union.value.struct?.fields) return {}
    return union.value.struct.fields
  }, [defaultValue])

  const {
    data: structDef,
    error,
    isLoading,
  } = useSWR(`structDef/${tenantId}/${structDefId.name}/${structDefId.version}`, async () => {
    return await getStructDef(tenantId, structDefId)
  })

  return (
    <FieldGroup className="rounded-md border">
      <div className="w-full border-b bg-gray-100">
        <div className="flex items-center justify-between p-2">
          <FormLabel label={structName} structDefId={structDefId} required={required} />
          {!required && (
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
          )}
        </div>
      </div>
      <div className="flex flex-col gap-4 p-3">
        {Object.entries(structDef?.structDef?.fields ?? {}).map(
          ([name, { fieldType, defaultValue: structFieldDefault }]) => {
            const definedType = fieldType?.definedType
            if (!definedType) return

            const inheritedDefaultValue = defaultStructFieldValues[name]?.value
            const effectiveDefaultValue = inheritedDefaultValue ?? structFieldDefault
            const hasDefaultValue = Boolean(effectiveDefaultValue)

            if (definedType.$case === 'primitiveType') {
              const variableType = definedType.value
              if (!variableType) return

              const { type, component } = VariableTypeToFieldComponent[variableType]
              const variableCase = getVariableCaseFromType(variableType)

              return (
                <StructPrimitiveField
                  key={name}
                  fieldName={name}
                  label={name}
                  component={component}
                  type={type}
                  protoRequired={!hasDefaultValue}
                  formRequired={!isDisabled}
                  variableType={variableType}
                  variableCase={variableCase as VariableCase}
                  structPath={currentStructPath}
                  structDefId={structDefId}
                  disabled={parentDisabled || isDisabled}
                  defaultValue={effectiveDefaultValue}
                />
              )
            } else if (definedType.$case === 'structDefId') {
              return (
                <StructDefParentContext.Provider
                  key={definedType.value.name}
                  value={{
                    parentDisabled: parentDisabled || isDisabled,
                    nestedStructPath: currentStructPath,
                  }}
                >
                  <StructDefGroup
                    structDefId={definedType.value}
                    name={name}
                    required={!hasDefaultValue}
                    defaultValue={effectiveDefaultValue}
                  />
                </StructDefParentContext.Provider>
              )
            }
          }
        )}
      </div>
    </FieldGroup>
  )
}
