import { getVariableDefType, wfRunIdFromFlattenedId, wfRunIdToPath } from '@/app/utils'
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
import { runWfSpec } from '../../wfSpec/[...props]/actions/runWfSpec'
import { DOT_REPLACEMENT_PATTERN, StructFormContextValue, StructFormProvider } from '../Forms/context/StructFormContext'
import { FormValues, WfRunForm } from '../Forms/WfRunForm'

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
    (values: FormValues) => {
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

        if (
          wfSpecVariables.some(
            variable =>
              variable.accessLevel === WfRunVariableAccessLevel.INHERITED_VAR &&
              variable.varDef?.name === transformedKey
          )
        ) {
          return acc
        }

        acc[transformedKey] = VariableValue.fromJSON({ [matchVariableType(transformedKey)]: primitiveValues[key] })
        return acc
      }, structVariables)

      return transformedObj
    },
    [matchVariableType, wfSpecVariables]
  )

  const handleFormSubmit = async (values: FormValues) => {
    const customWfRunId = values.customWfRunId as string
    const parentWfRunId = values.parentWfRunId as string
    delete values.customWfRunId
    delete values.parentWfRunId
    if (!wfSpec.id || (wfSpec.parentWfSpec && !parentWfRunId)) return
    const variables = formatVariablesPayload(values)

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
      router.push(`/${tenantId}/wfRun/${wfRunIdToPath(wfRun.id)}`)
    } catch (error: any) {
      if (error.message) {
        toast.error(error.message.split(':')[1])
      } else {
        toast.error('An error occurred while executing the workflow')
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
