import { VariableTypeToFieldComponent } from '@/app/utils'
import { Input } from '@/components/ui/input'
import { ThreadVarDef, VariableType, WfRunVariableAccessLevel, WfSpec } from 'littlehorse-client/proto'
import { forwardRef, useMemo } from 'react'
import { FormProvider, useForm } from 'react-hook-form'
import FormField from './components/FormField'
import FormLabel from './components/FormLabel'
import { StructDefGroup } from './components/StructDefGroup'

export type FormValues = {
  [key: string]: unknown
}

interface WfRunFormProps {
  wfSpecVariables: ThreadVarDef[]
  wfSpec: WfSpec
  onSubmit: (data: FormValues) => void
}

export const WfRunForm = forwardRef<HTMLFormElement, WfRunFormProps>(({ wfSpecVariables, wfSpec, onSubmit }, ref) => {
  const methods = useForm<FormValues>()

  // sorted by required first
  const sortedVariables = useMemo(
    () =>
      wfSpecVariables.sort((a, b) => {
        if (a.required === b.required) return 0
        return a.required ? -1 : 1
      }),
    [wfSpecVariables]
  )

  return (
    <FormProvider {...methods}>
      <form onSubmit={methods.handleSubmit(onSubmit)} ref={ref} className="space-y-4">
        <FormField
          label={'Custom WfRun Id'}
          as={Input}
          id="customWfRunId"
          type="text"
          variableType={VariableType.STR}
        />
        {wfSpec.parentWfSpec && (
          <FormField label={'Parent WfRun Id'} as={Input} id="parentWfRunId" variableType={VariableType.STR} />
        )}

        {sortedVariables.map((variable: ThreadVarDef) => {
          if (!variable.varDef) return null

          const name = variable.varDef?.name
          if (!name) return null

          const definedType = variable.varDef.typeDef?.definedType
          if (!definedType) return null

          if (variable.accessLevel === WfRunVariableAccessLevel.INHERITED_VAR) {
            return <FormLabel label={name} accessLevel={variable.accessLevel} required={variable.required} />
          }

          if (definedType.$case === 'primitiveType') {
            const { type, component } = VariableTypeToFieldComponent[definedType.value]

            return (
              <FormField
                key={name}
                label={name}
                as={component}
                id={name}
                type={type}
                protoRequired={variable.required}
                accessLevel={variable.accessLevel}
                variableType={definedType.value}
              />
            )
          }

          if (definedType.$case === 'structDefId') {
            return (
              <StructDefGroup
                key={name}
                structDefId={definedType.value}
                name={name}
                required={variable.required}
                defaultValue={variable.varDef?.defaultValue}
              />
            )
          }
        })}
      </form>
    </FormProvider>
  )
})
