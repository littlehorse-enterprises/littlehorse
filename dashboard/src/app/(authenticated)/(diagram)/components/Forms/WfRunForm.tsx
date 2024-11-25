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

// eslint-disable-next-line react/display-name
export const WfRunForm = forwardRef<HTMLFormElement, Prop>(({ wfSpecVariables, onSubmit }, ref) => {
  const methods = useForm()
  const { register, handleSubmit, formState } = methods

  const onSubmitForm = (data: FormValues) => {
    onSubmit(data)
  }

  return (
    <FormProvider {...methods}>
      <form onSubmit={handleSubmit(onSubmitForm)} ref={ref}>
        <div>
          <Label htmlFor="custom-id">Custom WfRun Id</Label>
          <Input
            type="text"
            className="mb-4 mt-1"
            id="custom-id"
            {...register('custom-id-wfRun-flow')}
            placeholder="Enter string value"
          />
        </div>
        {!!wfSpecVariables?.length &&
          wfSpecVariables.map((variable: ThreadVarDef) => (
            <FormFields key={variable.varDef?.name} variables={variable} register={register} formState={formState} />
          ))}
      </form>
    </FormProvider>
  )
})
