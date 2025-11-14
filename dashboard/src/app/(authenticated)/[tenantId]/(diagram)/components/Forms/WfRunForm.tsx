import { Input } from '@/components/ui/input'
import { ThreadVarDef, VariableType, WfSpec } from 'littlehorse-client/proto'
import { forwardRef, useMemo } from 'react'
import { FormProvider, useForm } from 'react-hook-form'
import FormField from './components/FormField'
import VariableFormField from './components/VariableFormField'

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

        {sortedVariables.map((variable: ThreadVarDef, index) => (
          <VariableFormField key={variable.varDef?.name ?? `variable-${index}`} variable={variable} />
        ))}
      </form>
    </FormProvider>
  )
})
