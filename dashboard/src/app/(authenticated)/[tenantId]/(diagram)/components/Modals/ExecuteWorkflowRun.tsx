import { concatWfRunIds, getVariableDefType } from '@/app/utils'
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
import { ThreadVarDef, VariableType, WfRunId, WfRunVariableAccessLevel, WfSpec } from 'littlehorse-client/proto'
import { useParams, useRouter } from 'next/navigation'
import { FC, useMemo, useRef } from 'react'
import { toast } from 'sonner'
import { Modal } from '../../context'
import { useModal } from '../../hooks/useModal'
import { runWfSpec } from '../../wfSpec/[...props]/actions/runWfSpec'
import { FormValues, WfRunForm } from '../Forms/WfRunForm'

const buildWfRunId = (flattenedId: string): WfRunId => {
  const ids = flattenedId.split('_')

  return ids.reduce<WfRunId | undefined>((parentWfRunId, currentId) => ({
    id: currentId,
    parentWfRunId
  }), undefined)!
}

export const DOT_REPLACEMENT_PATTERN = '*-/:DOT_REPLACE:'

export const ExecuteWorkflowRun: FC<Modal> = ({ data }) => {
  const { showModal, setShowModal } = useModal()
  const wfSpec = data as WfSpec
  const tenantId = useParams().tenantId as string
  const router = useRouter()
  const formRef = useRef<HTMLFormElement | null>(null)
  const wfSpecVariables = useMemo(() => {
    if (!wfSpec.threadSpecs?.[wfSpec.entrypointThreadName]) return []
    return (
      wfSpec.threadSpecs[wfSpec.entrypointThreadName].variableDefs?.map(variable => {
        const newVariable = { ...variable }
        if (newVariable.varDef?.name) {
          newVariable.varDef.name = newVariable.varDef.name.replace(/\./g, DOT_REPLACEMENT_PATTERN)
        }
        return newVariable
      }) ?? []
    )
  }, [wfSpec])

  const formatVariablesPayload = (values: FormValues) => {
    const transformedObj = Object.keys(values).reduce((acc: Record<string, FormValues>, key) => {
      if (values[key] === undefined) return acc
      const transformedKey = key.replace(DOT_REPLACEMENT_PATTERN, '.')

      if (wfSpecVariables.some(variable => variable.accessLevel === WfRunVariableAccessLevel.INHERITED_VAR && variable.varDef?.name === transformedKey)) {
        return acc
      }

      acc[transformedKey] = { [matchVariableType(transformedKey)]: values[key] }
      return acc
    }, {})

    return transformedObj
  }

  const matchVariableType = (key: string): string => {
    const variable = wfSpecVariables.find((variable: ThreadVarDef) => variable.varDef?.name === key)

    if (!variable) return ''

    if (!variable.varDef) return ''

    const type = getVariableDefType(variable.varDef)

    switch (type) {
      case VariableType.JSON_ARR:
        return 'jsonArr'
      case VariableType.JSON_OBJ:
        return 'jsonObj'
      default:
        return type.toLowerCase()
    }
  }

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
        parentWfRunId: wfSpec.parentWfSpec ? buildWfRunId(parentWfRunId) : undefined,
        wfSpecName: wfSpec.id.name,
        majorVersion: wfSpec.id.majorVersion,
        revision: wfSpec.id.revision,
        id: customWfRunId || undefined,
        variables,
      })
      if (!wfRun.id) return
      toast.success('Workflow has been executed')
      setShowModal(false)
      router.push(`/${tenantId}/wfRun/${concatWfRunIds(wfRun.id)}`)
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

        <WfRunForm wfSpecVariables={wfSpecVariables} wfSpec={wfSpec} onSubmit={handleFormSubmit} ref={formRef} />

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
