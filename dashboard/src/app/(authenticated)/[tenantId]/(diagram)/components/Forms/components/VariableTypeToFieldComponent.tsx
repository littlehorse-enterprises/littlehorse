import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { VariableType } from 'littlehorse-client/proto'
import { HTMLInputTypeAttribute, InputHTMLAttributes } from 'react'
import { SelectBool } from './SelectBool'

export type PrimitiveFieldConfig = {
  type: HTMLInputTypeAttribute
  step?: InputHTMLAttributes<HTMLInputElement>['step']
  component: React.ElementType
}

export const VariableTypeToFieldComponent: Record<VariableType, PrimitiveFieldConfig> = {
  [VariableType.JSON_OBJ]: { type: 'textarea', component: Textarea },
  [VariableType.JSON_ARR]: { type: 'textarea', component: Textarea },
  [VariableType.DOUBLE]: { type: 'number', step: 'any', component: Input },
  [VariableType.BOOL]: { type: 'checkbox', component: SelectBool },
  [VariableType.STR]: { type: 'text', component: Input },
  [VariableType.INT]: { type: 'number', component: Input },
  [VariableType.BYTES]: { type: 'text', component: Input },
  [VariableType.WF_RUN_ID]: { type: 'text', component: Input },
  [VariableType.TIMESTAMP]: { type: 'text', component: Input },
}
