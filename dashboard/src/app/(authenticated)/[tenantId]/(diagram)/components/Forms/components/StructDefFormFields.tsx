import { getStructDef } from '@/app/actions/getStructDef'
import { getVariableCaseFromType } from '@/app/utils'
import { StructDefId, ThreadVarDef } from 'littlehorse-client/proto'
import { useParams } from 'next/navigation'
import { FC, useMemo } from 'react'
import { useForm } from 'react-hook-form'
import useSWR from 'swr'
import { FormValues } from '../WfRunForm'
import { FormComponent } from './formType'

export const StructDefFormFields: FC<{ structDefId: StructDefId; threadVarDef: ThreadVarDef }> = ({
  structDefId,
  threadVarDef,
}) => {
  const tenantId = useParams().tenantId as string
  const methods = useForm<FormValues>()
  const { register, handleSubmit, formState } = methods

  const { data, error, isLoading } = useSWR(
    `structDef/${tenantId}/${structDefId.name}/${structDefId.version}`,
    async () => {
      return await getStructDef(tenantId, structDefId)
    }
  )
  const fields = useMemo(
    () =>
      Object.entries(data?.structDef?.fields ?? {}).map(([name, field]) => ({
        name,
        fieldType: field.fieldType?.definedType,
      })),
    [data]
  )
  fields.forEach(field => {})

  return (
    <div>
      {fields.map(field => {
        if (field?.fieldType?.$case !== 'primitiveType') return
        const type = getVariableCaseFromType(field.fieldType?.value)
        const Component = FormComponent[type]

        // todo : make formcomponents compatible w/ this usecase
        return <Component key={field.name} variables={threadVarDef} formState={formState} register={register} />
      })}
    </div>
  )
}
