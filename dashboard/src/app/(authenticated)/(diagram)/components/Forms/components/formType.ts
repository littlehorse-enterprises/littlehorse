import { FormInput, FormTextarea, FormSwitch } from './'
import { VariableType } from 'littlehorse-client/proto'

export const FormComponent = {
  [VariableType.INT]: FormInput,
  [VariableType.STR]: FormInput,
  [VariableType.DOUBLE]: FormInput,
  [VariableType.BOOL]: FormSwitch,
  [VariableType.JSON_OBJ]: FormTextarea,
  [VariableType.JSON_ARR]: FormTextarea,
  [VariableType.BYTES]: FormInput,
  [VariableType.UNRECOGNIZED]: FormInput,
} as const

export type FormFieldType = keyof typeof FormComponent
