import React, { forwardRef } from 'react'
import { useForm, FormProvider } from 'react-hook-form'
import { FormFields } from './components/FormFields'
import { ThreadVarDef } from 'littlehorse-client/proto'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'

type Prop = {
  wfSpecVariables: ThreadVarDef[]
  onSubmit: any
}
ThreadVarDef

// eslint-disable-next-line react/display-name
export const WfRunForm = forwardRef<HTMLDivElement, Prop>(({ wfSpecVariables, onSubmit }, ref) => {
  const methods = useForm()
  const { register, handleSubmit, formState } = methods

  const onSubmitForm = data => {
    onSubmit(data)
  }

  return (
    <FormProvider {...methods}>
      <form onSubmit={handleSubmit(onSubmitForm)} ref={ref}>
        <div>
          <Label htmlFor={'custom-id'}>Id (Custom WfRun Id)</Label>
          <Input type="text" className="mb-4 mt-1" id="custom-id" {...register('custom-id-wfRun-flow')} />
        </div>
        {!!wfSpecVariables?.length &&
          wfSpecVariables.map((variable: ThreadVarDef, index: number) => (
            <FormFields key={`form-field-${index}`} variables={variable} register={register} formState={formState} />
          ))}
      </form>
    </FormProvider>
  )
})
