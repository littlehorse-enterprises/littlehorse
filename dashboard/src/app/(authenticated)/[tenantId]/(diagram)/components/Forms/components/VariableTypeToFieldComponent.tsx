import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { VariableType } from 'littlehorse-client/proto'
import { HTMLInputTypeAttribute, InputHTMLAttributes } from 'react'
import { SelectBool } from './SelectBool'

export type FieldValidator = (value: unknown) => true | string

export const isDecimal: FieldValidator = value => {
  if (value === '' || value === undefined || value === null) return true
  return Number.isFinite(Number(value)) ? true : 'Must be a number'
}

export type PrimitiveFieldConfig = {
  type: HTMLInputTypeAttribute
  inputMode?: InputHTMLAttributes<HTMLInputElement>['inputMode']
  validate?: FieldValidator
  component: React.ElementType
}

export const VariableTypeToFieldComponent: Record<VariableType, PrimitiveFieldConfig> = {
  [VariableType.JSON_OBJ]: { type: 'textarea', component: Textarea },
  [VariableType.JSON_ARR]: { type: 'textarea', component: Textarea },
  [VariableType.DOUBLE]: { type: 'text', inputMode: 'decimal', validate: isDecimal, component: Input },
  [VariableType.BOOL]: { type: 'checkbox', component: SelectBool },
  [VariableType.STR]: { type: 'text', component: Input },
  [VariableType.INT]: { type: 'number', component: Input },
  [VariableType.BYTES]: { type: 'text', component: Input },
  [VariableType.WF_RUN_ID]: { type: 'text', component: Input },
  [VariableType.TIMESTAMP]: { type: 'text', component: Input },
}
