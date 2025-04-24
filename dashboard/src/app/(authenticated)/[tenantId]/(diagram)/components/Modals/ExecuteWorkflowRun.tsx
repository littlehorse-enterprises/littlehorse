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
import { ThreadVarDef, VariableType, WfSpec } from 'littlehorse-client/proto'
import { useParams, useRouter } from 'next/navigation'
import { FC, useMemo, useRef } from 'react'
import { toast } from 'sonner'
import { Modal } from '../../context'
import { useModal } from '../../hooks/useModal'
import { runWfSpec } from '../../wfSpec/[...props]/actions/runWfSpec'
import { FormValues, WfRunForm } from '../Forms/WfRunForm'

export const DOT_REPLACEMENT_PATTERN = "*-/:DOT_REPLACE:"

export const ExecuteWorkflowRun: FC<Modal> = ({ data }) => {
  const { showModal, setShowModal } = useModal()
  const lhWorkflowSpec = data as WfSpec
  const tenantId = useParams().tenantId as string
  const router = useRouter()
  const formRef = useRef<HTMLFormElement | null>(null)
  const wfSpecVariables = useMemo(() => {
    return lhWorkflowSpec.threadSpecs?.entrypoint?.variableDefs?.map(variable => {
      const newVariable = { ...variable };
      if (newVariable.varDef?.name) {
        newVariable.varDef.name = newVariable.varDef.name.replace(/\./g, DOT_REPLACEMENT_PATTERN);
      }
      return newVariable;
    }) ?? [];
  }, [lhWorkflowSpec]);

  const formatVariablesPayload = (values: FormValues) => {
    const transformedObj = Object.keys(values).reduce((acc: Record<string, FormValues>, key) => {
      if (values[key] === undefined) return acc
      const transformedKey = key.replace(DOT_REPLACEMENT_PATTERN, '.')
      acc[transformedKey] = { [matchVariableType(transformedKey)]: values[key] }
      return acc
    }, {})

    return transformedObj
  }

  const matchVariableType = (key: string): string => {
    const variable = wfSpecVariables.find((variable: ThreadVarDef) => variable.varDef?.name === key)

    if (!variable) return ''

    const type = variable.varDef?.type as string

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
    delete values.customWfRunId
    if (!lhWorkflowSpec.id) return
    try {
      const wfRun = await runWfSpec({
        tenantId,
        wfSpecName: lhWorkflowSpec.id.name,
        majorVersion: lhWorkflowSpec.id.majorVersion,
        revision: lhWorkflowSpec.id.revision,
        id: customWfRunId || undefined,
        variables: formatVariablesPayload(values),
      })
      if (!wfRun.id) return
      toast.success('Workflow has been executed')
      setShowModal(false)
      router.push(`/${tenantId}/wfRun/${wfRun.id.id}`)
    } catch (error: any) {
      toast.error(error.message?.split(':')?.[1])
    }
  }

  return (
    <Dialog open={showModal} onOpenChange={open => setShowModal(open)}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Execute {lhWorkflowSpec.id?.name}</DialogTitle>
          <DialogDescription className="text-primary/50">
            You can leave all optional fields blank if desired.
          </DialogDescription>
        </DialogHeader>

        <WfRunForm wfSpecVariables={wfSpecVariables} onSubmit={handleFormSubmit} ref={formRef} />

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
