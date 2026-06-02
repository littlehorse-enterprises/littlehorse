import { getPrimitiveFormDefaultValue } from '@/app/utils'
import { Input } from '@/components/ui/input'
import { ThreadVarDef, VariableType, WfRunVariableAccessLevel, WfSpec } from 'littlehorse-client/proto'
import { forwardRef, useMemo } from 'react'
import { FormProvider, useForm } from 'react-hook-form'
import FormField from './components/FormField'
import VariableFormField from './components/VariableFormField'

export type FormValues = {
  [key: string]: unknown
}

export type WfRunFormSubmitMeta = {
  /**
   * Map of field names that the user has changed away from the WfSpec default.
   * Used to distinguish "no value provided" (skip the variable so the server
   * applies its default) from "user explicitly entered/cleared a value".
   */
  dirtyFields: Record<string, boolean | undefined>
}

interface WfRunFormProps {
  wfSpecVariables: ThreadVarDef[]
  wfSpec: WfSpec
  onSubmit: (data: FormValues, meta: WfRunFormSubmitMeta) => void
}

export const WfRunForm = forwardRef<HTMLFormElement, WfRunFormProps>(({ wfSpecVariables, wfSpec, onSubmit }, ref) => {
  // Pre-populate primitive form fields with the default value declared in the
  // WfSpec's VariableDef. Combined with `dirtyFields`-based filtering at submit
  // time, this lets us tell whether the user actually provided a value or
  // simply accepted the default (in which case we should let the server apply
  // it instead of overwriting it from the dashboard).
  const defaultValues = useMemo<FormValues>(() => {
    const values: FormValues = {}
    for (const variable of wfSpecVariables) {
      const varDef = variable.varDef
      if (!varDef?.name) continue
      if (variable.accessLevel === WfRunVariableAccessLevel.INHERITED_VAR) continue
      if (varDef.typeDef?.definedType?.$case !== 'primitiveType') continue
      const defaultFormValue = getPrimitiveFormDefaultValue(varDef.defaultValue)
      if (defaultFormValue === undefined) continue
      values[varDef.name] = defaultFormValue
    }
    return values
  }, [wfSpecVariables])

  const methods = useForm<FormValues>({ defaultValues })

  // sorted by required first
  const sortedVariables = useMemo(
    () =>
      wfSpecVariables.sort((a, b) => {
        if (a.required === b.required) return 0
        return a.required ? -1 : 1
      }),
    [wfSpecVariables]
  )

  const handleSubmit = (data: FormValues) => {
    onSubmit(data, { dirtyFields: methods.formState.dirtyFields as Record<string, boolean | undefined> })
  }

  return (
    <FormProvider {...methods}>
      <form onSubmit={methods.handleSubmit(handleSubmit)} ref={ref} className="space-y-4">
        <FormField
          label={'Custom WfRun Id'}
          as={Input}
          id="customWfRunId"
          type="text"
          variableType={VariableType.STR}
        />
        {wfSpec.parentWfSpec && (
          <FormField label={'Parent WfRun Id'} as={Input} id="parentWfRunId" variableType={VariableType.STR} />
        )}

        {sortedVariables.map((variable: ThreadVarDef, index) => (
          <VariableFormField key={variable.varDef?.name ?? `variable-${index}`} variable={variable} />
        ))}
      </form>
    </FormProvider>
  )
})

WfRunForm.displayName = 'WfRunForm'
