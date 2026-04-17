import { FormFieldProp } from '@/types'
import { FC, useEffect, useState } from 'react'
import { Controller, useFormContext } from 'react-hook-form'
import { BaseFormField } from './BaseFormField'
import { TimestampPicker } from './TimestampPicker'

export const FormTimestamp: FC<FormFieldProp> = props => {
  const [isDisabled, setIsDisabled] = useState(!props.variables?.required)
  const { control, setValue } = useFormContext()

  useEffect(() => {
    if (!props.variables?.required && props.variables?.varDef?.name) {
      setValue(props.variables.varDef.name, null)
    }
  }, [props.variables, setValue])

  if (!props.variables?.varDef?.name) return null

  const {
    variables: {
      varDef: { name },
      required,
    },
  } = props

  return (
    <BaseFormField {...props} isDisabled={isDisabled} setIsDisabled={setIsDisabled}>
      <Controller
        name={name}
        control={control}
        rules={{ required: required ? `${name} is required` : false }}
        render={({ field }) => (
          <TimestampPicker id={name} value={field.value ?? ''} onChange={field.onChange} disabled={isDisabled} />
        )}
      />
    </BaseFormField>
  )
}
