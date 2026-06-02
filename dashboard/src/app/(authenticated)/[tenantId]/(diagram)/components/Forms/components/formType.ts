import { VARIABLE_TYPES } from '@/app/constants'
import { FC } from 'react'
import { FormInput, FormSelect, FormTextarea } from './'
import { FormTimestamp } from './FormTimestamp'

export const FormComponent: Record<keyof typeof VARIABLE_TYPES, FC<React.ComponentProps<typeof FormInput>>> = {
  int: FormInput,
  str: FormInput,
  double: FormInput,
  bool: FormSelect,
  jsonObj: FormTextarea,
  jsonArr: FormTextarea,
  bytes: FormInput,
  wfRunId: FormTextarea,
  utcTimestamp: FormTimestamp,
  struct: FormTextarea,
  array: FormTextarea,
} as const

export type FormFieldType = keyof typeof FormComponent
