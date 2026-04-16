import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { VariableType } from 'littlehorse-client/proto'
import { HTMLInputTypeAttribute } from 'react'
import { SelectBool } from './SelectBool'

export const VariableTypeToFieldComponent = {
  [VariableType.JSON_OBJ]: { type: 'textarea', component: Textarea },
  [VariableType.JSON_ARR]: { type: 'textarea', component: Textarea },
  [VariableType.DOUBLE]: { type: 'number', component: Input },
  [VariableType.BOOL]: { type: 'checkbox', component: SelectBool },
  [VariableType.STR]: { type: 'text', component: Input },
  [VariableType.INT]: { type: 'number', component: Input },
  [VariableType.BYTES]: { type: 'text', component: Input },
  [VariableType.WF_RUN_ID]: { type: 'text', component: Input },
  [VariableType.TIMESTAMP]: { type: 'text', component: Input },
  [VariableType.UNRECOGNIZED]: { type: 'text', component: Input },
} as const satisfies Record<keyof typeof VariableType, { type: HTMLInputTypeAttribute; component: React.ElementType }>
