import { Input } from '@/components/ui/input'
import { ThreadVarDef, VariableType, WfRunVariableAccessLevel, WfSpec } from 'littlehorse-client/proto'
import { forwardRef, HTMLInputTypeAttribute, useMemo } from 'react'
import { FormProvider, useForm } from 'react-hook-form'
import { VariableLabel } from './components/BaseFormField'
import FormField from './components/FormField'
import { SelectBool } from './components/SelectBool'
import { getVariableCaseFromTypeDef } from '@/app/utils'

export type FormValues = {
  [key: string]: unknown
}

interface WfRunFormProps {
  wfSpecVariables: ThreadVarDef[]
  wfSpec: WfSpec
  onSubmit: (data: FormValues) => void
}

export const WfRunForm = forwardRef<HTMLFormElement, WfRunFormProps>(({ wfSpecVariables, wfSpec, onSubmit }, ref) => {
  const methods = useForm<FormValues>()
  const { register, handleSubmit, formState } = methods

  // Sorted by required first
  const sortedVariables = useMemo(
    () =>
      wfSpecVariables.sort((a, b) => {
        if (a.required === b.required) return 0
        return a.required ? -1 : 1
      }),
    [wfSpecVariables]
  )

  return (
    <FormProvider {...methods}>
      <form onSubmit={handleSubmit(onSubmit)} ref={ref} className="space-y-4">
        <FormField label={'Custom WfRun Id'} as={Input} id="customWfRunId" type="text" />
        {wfSpec.parentWfSpec && <FormField label={'Parent WfRun Id'} as={Input} id="parentWfRunId" />}

        {sortedVariables.map((variable: ThreadVarDef) => {
          const name = variable.varDef?.name
          if (!name) return null

          const definedType = variable.varDef?.typeDef?.definedType
          if (!definedType || definedType?.$case === 'structDefId') return null

          // todo : make sure this is corret & works with deprecated proto
          const { type, component } = TypeMap[definedType.value]

          return variable.accessLevel === WfRunVariableAccessLevel.INHERITED_VAR ? (
            <VariableLabel key={name} {...variable} />
          ) : (
            <FormField
              label={name}
              as={component}
              id={name}
              type={type}
              required={variable.required}
              accessLevel={variable.accessLevel}
              variableType={}
            />
          )
        })}
      </form>
    </FormProvider>
  )
})

// todo : move this out
const TypeMap = {
  [VariableType.JSON_OBJ]: { type: 'text', component: Input },
  [VariableType.JSON_ARR]: { type: 'text', component: Input },
  [VariableType.DOUBLE]: { type: 'number', component: Input },
  [VariableType.BOOL]: { type: "checkbox", component: SelectBool },
  [VariableType.STR]: { type: 'text', component: Input },
  [VariableType.INT]: { type: 'number', component: Input },
  [VariableType.BYTES]: { type: 'text', component: Input },
  [VariableType.WF_RUN_ID]: { type: 'text', component: Input },
  [VariableType.TIMESTAMP]: { type: 'text', component: Input },
  [VariableType.UNRECOGNIZED]: { type: 'text', component: Input },
} as const satisfies Record<keyof typeof VariableType, { type: HTMLInputTypeAttribute; component: React.ElementType }>
