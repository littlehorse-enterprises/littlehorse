import { FormInput, FormTextarea, FormSelect } from '.'
import { VariableType } from 'littlehorse-client/proto'

export const FormComponent = {
  [VariableType.INT]: FormInput,
  [VariableType.STR]: FormInput,
  [VariableType.DOUBLE]: FormInput,
  [VariableType.BOOL]: FormSelect,
  [VariableType.JSON_OBJ]: FormTextarea,
  [VariableType.JSON_ARR]: FormTextarea,
  [VariableType.BYTES]: FormInput,
  [VariableType.WF_RUN_ID]: FormInput,
  [VariableType.UNRECOGNIZED]: FormInput,
} as const

export type FormFieldType = keyof typeof FormComponent
