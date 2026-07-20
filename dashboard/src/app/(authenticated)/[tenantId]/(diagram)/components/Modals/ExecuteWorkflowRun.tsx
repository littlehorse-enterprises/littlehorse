import { getVariableDefType, wfRunIdFromFlattenedId, wfRunIdToPath } from '@/app/utils'
import { getTypedVariableValueFromTypeDef } from '@/app/utils/variables'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { RunWfRequest, ThreadVarDef, VariableValue, WfRunVariableAccessLevel, WfSpec } from 'littlehorse-client/proto'
import { useParams, useRouter } from 'next/navigation'
import { FC, useCallback, useMemo, useRef } from 'react'
import { toast } from 'sonner'
import { Modal } from '../../context'
import { useModal } from '../../hooks/useModal'
import { routes, withTenant } from '@/app/routes'
import { runWfSpec } from '../../wfSpec/[...props]/actions/runWfSpec'
import { DOT_REPLACEMENT_PATTERN, StructFormContextValue, StructFormProvider } from '../Forms/context/StructFormContext'
import { FormValues, WfRunForm, WfRunFormSubmitMeta } from '../Forms/WfRunForm'

export const ExecuteWorkflowRun: FC<Modal<WfSpec>> = ({ data: wfSpec }) => {
  const { showModal, setShowModal } = useModal()
  const tenantId = useParams().tenantId as string
  const formRef = useRef<HTMLFormElement | null>(null)
  const router = useRouter()
  const wfSpecVariables = useMemo(() => {
    if (!wfSpec.threadSpecs[wfSpec.entrypointThreadName]) return []
    return (
      wfSpec.threadSpecs[wfSpec.entrypointThreadName].variableDefs.map(variable => {
        const newVariable = { ...variable }
        if (newVariable.varDef?.name) {
          newVariable.varDef.name = newVariable.varDef.name.replace(/\./g, DOT_REPLACEMENT_PATTERN)
        }
        return newVariable
      }) ?? []
    )
  }, [wfSpec])

  const structFormContextRef = useRef<StructFormContextValue | null>(null)

  const matchVariableType = useCallback(
    (key: string) => {
      const variable = wfSpecVariables.find((variable: ThreadVarDef) => variable.varDef?.name === key)
      if (!variable || !variable.varDef) return ''

      return getVariableDefType(variable.varDef)
    },
    [wfSpecVariables]
  )

  const formatVariablesPayload = useCallback(
    (values: FormValues, dirtyFields: WfRunFormSubmitMeta['dirtyFields']) => {
      const { structValues: _ignoredStructValues, ...primitiveValues } = values as FormValues & {
        structValues?: Record<string, unknown>
      }

      const structVariablesSource = structFormContextRef.current?.getStructVariables() ?? {}
      const structVariables = Object.entries(structVariablesSource).reduce(
        (acc, [key, value]) => {
          acc[key.split(DOT_REPLACEMENT_PATTERN).join('.')] = value
          return acc
        },
        {} as RunWfRequest['variables']
      )

      const transformedObj = Object.keys(primitiveValues).reduce((acc: RunWfRequest['variables'], key) => {
        if (primitiveValues[key] === undefined) return acc

        const transformedKey = key.split(DOT_REPLACEMENT_PATTERN).join('.')
        const variableDef = wfSpecVariables.find(variable => variable.varDef?.name === key)
        const isRequired = variableDef?.required ?? false

        if (!dirtyFields[key] && !isRequired) return acc

        if (
          wfSpecVariables.some(
            variable =>
              variable.accessLevel === WfRunVariableAccessLevel.INHERITED_VAR &&
              variable.varDef?.name === transformedKey
          )
        ) {
          return acc
        }

        const caseName = matchVariableType(transformedKey)

        // Container types (Map/Array) are entered as human-friendly JSON in a textarea
        // (e.g. {"one":1}) and need the declared key/element types to build proper entries.
        // fromJson would reject that shape and the create() fallback below would silently
        // produce an EMPTY container, discarding the user's data.
        if (caseName === 'map' || caseName === 'array') {
          try {
            acc[transformedKey] = getTypedVariableValueFromTypeDef(
              variableDef?.varDef?.typeDef,
              String(primitiveValues[key])
            )
          } catch {
            // Field-level validation already blocks invalid JSON before submit; if something
            // still slips through, omit the variable rather than send an empty container.
          }
          return acc
        }

        // The old ts-proto `VariableValue.fromJSON({ [case]: value })` coerced JSON values
        // to the correct runtime type (e.g. base64 string -> Uint8Array for BYTES, ISO
        // string -> Timestamp). @protobuf-ts `fromJson` performs the same coercion, but
        // throws on values it cannot parse (e.g. a non-object passed for WF_RUN_ID), where
        // the old code silently produced a default. Fall back to `create` (raw assignment)
        // to preserve that lenient, no-throw behavior.
        try {
          acc[transformedKey] = VariableValue.fromJson({
            [caseName]: primitiveValues[key],
          } as unknown as Parameters<typeof VariableValue.fromJson>[0])
        } catch {
          acc[transformedKey] = VariableValue.create({
            value: { oneofKind: caseName, [caseName]: primitiveValues[key] } as unknown as VariableValue['value'],
          })
        }
        return acc
      }, structVariables)

      return transformedObj
    },
    [matchVariableType, wfSpecVariables]
  )

  const handleFormSubmit = async (values: FormValues, meta: WfRunFormSubmitMeta) => {
    const customWfRunId = values.customWfRunId as string
    const parentWfRunId = values.parentWfRunId as string
    delete values.customWfRunId
    delete values.parentWfRunId
    if (!wfSpec.id || (wfSpec.parentWfSpec && !parentWfRunId)) return
    const variables = formatVariablesPayload(values, meta.dirtyFields)

    try {
      const wfRun = await runWfSpec({
        tenantId,
        parentWfRunId: wfSpec.parentWfSpec ? wfRunIdFromFlattenedId(parentWfRunId) : undefined,
        wfSpecName: wfSpec.id.name,
        majorVersion: wfSpec.id.majorVersion,
        revision: wfSpec.id.revision,
        id: customWfRunId || undefined,
        variables,
      })
      if (!wfRun.id) return
      toast.success('Workflow has been executed')
      setShowModal(false)
      router.push(withTenant(tenantId, routes.wfRun.detail(wfRunIdToPath(wfRun.id))))
    } catch (error: any) {
      if (error.message) {
        const sanitizedErrorMessage = error.message.slice(error.message.indexOf(':') + 1).trim()
        toast.error(sanitizedErrorMessage, { duration: Infinity, closeButton: true })
      } else {
        toast.error('An error occurred while executing the workflow', { duration: Infinity, closeButton: true })
      }
    }
  }

  return (
    <Dialog open={showModal} onOpenChange={open => setShowModal(open)}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Execute {wfSpec.id?.name}</DialogTitle>
          <DialogDescription className="text-primary/50">
            You can leave all optional fields blank if desired.
          </DialogDescription>
        </DialogHeader>

        <StructFormProvider contextRef={structFormContextRef}>
          <WfRunForm wfSpecVariables={wfSpecVariables} wfSpec={wfSpec} onSubmit={handleFormSubmit} ref={formRef} />
        </StructFormProvider>

        <DialogFooter>
          <DialogClose className="mr-4">Cancel</DialogClose>
          <Button
            onClick={() => {
              formRef.current?.requestSubmit()
            }}
            type="submit"
          >
            Execute Workflow
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
