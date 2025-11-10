import { getStructDef } from '@/app/actions/getStructDef'
import { getVariableCaseFromType, VariableTypeToFieldComponent } from '@/app/utils'
import { Button } from '@/components/ui/button'
import { FieldGroup } from '@/components/ui/field'
import { StructDefId } from 'littlehorse-client/proto'
import { useParams } from 'next/navigation'
import { createContext, FC, useContext, useEffect, useMemo, useState } from 'react'
import { useFormContext } from 'react-hook-form'
import useSWR from 'swr'
import { STRUCT_FIELD_DATA_SEPARATOR_PATTERN, STRUCT_PATH_SEPARATOR } from '../../Modals/ExecuteWorkflowRun'
import { FormValues } from '../WfRunForm'
import FormField from './FormField'
import FormLabel from './FormLabel'

const StructDefParentContext = createContext<{ parentDisabled: boolean; nestedStructPath: string[] }>({
  parentDisabled: false,
  nestedStructPath: [],
})

export const StructDefGroup: FC<{ structDefId: StructDefId; name: string; required: boolean }> = ({
  structDefId,
  name: structName,
  required,
}) => {
  const tenantId = useParams().tenantId as string
  const { register, formState } = useFormContext<FormValues>()
  const { parentDisabled, nestedStructPath } = useContext(StructDefParentContext)
  const [isDisabled, setIsDisabled] = useState(parentDisabled)

  useEffect(() => {
    setIsDisabled(parentDisabled)
  }, [parentDisabled])

  const fullStructPath = useMemo(
    () => [...nestedStructPath, structName].join(STRUCT_PATH_SEPARATOR),
    [structName, nestedStructPath]
  )

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
        {Object.entries(structDef?.structDef?.fields ?? {}).map(([name, { fieldType, defaultValue }]) => {
          const definedType = fieldType?.definedType
          if (!definedType) return

          if (definedType.$case === 'primitiveType') {
            const variableType = definedType.value
            if (!variableType) return

            const { type, component } = VariableTypeToFieldComponent[variableType]
            const variableCase = getVariableCaseFromType(variableType)

            return (
              <FormField
                key={name}
                label={name}
                as={component}
                id={[name, variableCase, fullStructPath, structDefId.name, structDefId.version].join(
                  STRUCT_FIELD_DATA_SEPARATOR_PATTERN
                )}
                type={type}
                protoRequired={!defaultValue}
                formRequired={!isDisabled}
                variableType={variableType}
                disabled={parentDisabled || isDisabled}
              />
            )
          } else if (definedType.$case === 'structDefId') {
            return (
              <StructDefParentContext.Provider
                key={definedType.value.name}
                value={{
                  parentDisabled: parentDisabled || isDisabled,
                  nestedStructPath: [fullStructPath],
                }}
              >
                <StructDefGroup structDefId={definedType.value} name={name} required={!defaultValue} />
              </StructDefParentContext.Provider>
            )
          }
        })}
      </div>
    </FieldGroup>
  )
}
