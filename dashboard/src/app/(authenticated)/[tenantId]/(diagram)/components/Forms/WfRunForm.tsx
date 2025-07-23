import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { ThreadVarDef, WfRunVariableAccessLevel, WfSpec } from 'littlehorse-client/proto'
import { forwardRef } from 'react'
import { FormProvider, useForm } from 'react-hook-form'
import { VariableLabel } from './components/BaseFormField'
import { FormFields } from './components/FormFields'

export type FormValues = {
  [key: string]: unknown
}

type Prop = {
  wfSpecVariables: ThreadVarDef[]
  wfSpec: WfSpec
  onSubmit: (data: FormValues) => void
}

export const WfRunForm = forwardRef<HTMLFormElement, Prop>(({ wfSpecVariables, wfSpec, onSubmit }, ref) => {
  const methods = useForm<FormValues>()
  const { register, handleSubmit, formState } = methods

  const onSubmitForm = async (data: FormValues) => {
    onSubmit(data)
  }

  // Sort variables so required fields come first
  const sortedVariables = wfSpecVariables.sort((a, b) => {
    if (a.required === b.required) return 0
    return a.required ? -1 : 1
  })

  return (
    <FormProvider {...methods}>
      <form onSubmit={handleSubmit(onSubmitForm)} ref={ref} className="space-y-4">
        <div>
          <Label htmlFor="customWfRunId" className="mb-2 flex items-center gap-2">
            Custom WfRun Id
            <span className="rounded bg-gray-300 p-1 text-xs">Optional</span>
          </Label>
          <Input type="text" id="customWfRunId" {...register('customWfRunId')} placeholder="Enter string value" />
        </div>
        {wfSpec.parentWfSpec && (
          <div>
            <Label htmlFor="customWfRunId" className="mb-2 flex items-center gap-2">
              Parent WfRun Id
              <span className="rounded bg-gray-300 p-1 text-xs">Required</span>
            </Label>
            <Input
              type="text"
              id="customWfRunId"
              {...register('parentWfRunId')}
              placeholder="Enter string value"
              required
            />
          </div>
        )}
        {!!sortedVariables.length &&
          sortedVariables.map((variable: ThreadVarDef) =>
            variable.accessLevel === WfRunVariableAccessLevel.INHERITED_VAR ? (
              <VariableLabel key={variable.varDef?.name} {...variable} />
            ) : (
              <FormFields key={variable.varDef?.name} variables={variable} register={register} formState={formState} />
            )
          )}
      </form>
    </FormProvider>
  )
})
