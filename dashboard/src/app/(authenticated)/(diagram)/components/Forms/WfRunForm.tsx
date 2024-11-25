import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { ThreadVarDef } from 'littlehorse-client/proto'
import { forwardRef } from 'react'
import { FormProvider, useForm } from 'react-hook-form'
import { FormFields } from './components/FormFields'

export type FormValues = {
  [key: string]: unknown
}

type Prop = {
  wfSpecVariables: ThreadVarDef[]
  onSubmit: (data: FormValues) => void
}

export const WfRunForm = forwardRef<HTMLFormElement, Prop>(({ wfSpecVariables, onSubmit }, ref) => {
  const methods = useForm()
  const { register, handleSubmit, formState } = methods

  return (
    <FormProvider {...methods}>
      <form onSubmit={methods.handleSubmit(onSubmit)} ref={ref} className="space-y-4">
        <div>
          <div className="mb-2 flex justify-between">
            <Label htmlFor="customWfRunId" className="center flex items-center gap-2">
              Custom WfRun Id
              <span className="rounded bg-gray-300 p-1 text-xs">Optional</span>
            </Label>
          </div>
          <Input {...methods.register('customWfRunId')} type="text" placeholder="Enter custom WfRun Id" />
        </div>

        {!!wfSpecVariables?.length &&
          wfSpecVariables.map((variable: ThreadVarDef) => (
            <FormFields
              key={variable.varDef?.name}
              variable={variable}
              register={methods.register}
              formState={methods.formState}
            />
          ))}
      </form>
    </FormProvider>
  )
})

WfRunForm.displayName = 'WfRunForm'
