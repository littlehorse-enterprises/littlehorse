import React, { useRef } from 'react'
import { useModal } from '../../hooks/useModal'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog'
import { Modal } from '../../context'
import { FC } from 'react'
import { ThreadVarDef, VariableType, WfSpec } from 'littlehorse-client/proto'
import { runWfSpec } from '../../wfSpec/[...props]/actions/runWfSpec'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { useRouter } from 'next/navigation'
import { WfRunForm } from '@/app/(authenticated)/(diagram)/components/Forms/WfRunForm'
import { X } from 'lucide-react'
import { FormValues } from '@/app/(authenticated)/(diagram)/components/Forms/WfRunForm'

export const ExecuteWorkflowRun: FC<Modal> = ({ data }) => {
  const { showModal, setShowModal } = useModal()
  const lhWorkflowSpec = data as WfSpec
  const { tenantId } = useWhoAmI()
  const router = useRouter()
  const formRef = useRef<HTMLFormElement | null>(null)
  const wfSpecVariables = lhWorkflowSpec.threadSpecs?.entrypoint?.variableDefs

  const formatVariablesPayload = (values: FormValues) => {
    const transformedObj = Object.keys(values).reduce((acc: Record<string, FormValues>, key) => {
      acc[key] = { [matchVariableType(key)]: values[key] }
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
    const customWfRunId = values['custom-id-wfRun-flow'] as string
    delete values['custom-id-wfRun-flow']
    if (!lhWorkflowSpec.id) return
    try {
      const wfRun = await runWfSpec({
        ...lhWorkflowSpec.id,
        wfSpecName: lhWorkflowSpec.id.name,
        tenantId,
        id: customWfRunId || undefined,
        variables: formatVariablesPayload(values),
      })
      if (!wfRun.id) return
      setShowModal(false)
      router.push(`/wfRun/${wfRun.id.id}`)
    } catch (error) {
      // console.log(error.message?.split(':')?.[1])
      // needs to implement error handling
    }
  }

  return (
    <Dialog open={showModal} onOpenChange={open => setShowModal(open)}>
      <DialogContent className="overflow-hidden p-0">
        <div className="relative flex max-h-[calc(100vh-50px)] flex-col">
          <DialogHeader className="sticky top-0 z-10 bg-background px-4 py-3 shadow-md ">
            <DialogTitle className="text-gray-700">
              Execute <span className="text-black">{lhWorkflowSpec.id?.name}</span>
              <button
                onClick={() => setShowModal(false)}
                className="float-right text-gray-500 hover:text-gray-700 focus:outline-none"
                aria-label="Close"
              >
                <X size={18} />
              </button>
            </DialogTitle>
          </DialogHeader>
          <div className="flex-1 overflow-y-auto px-4 py-2 pb-5">
            <WfRunForm wfSpecVariables={wfSpecVariables} onSubmit={handleFormSubmit} ref={formRef} />
          </div>
          <DialogFooter className="sticky bottom-0 z-10 bg-background px-4 py-3 shadow-md">
            <div className="flex justify-end gap-2">
              <button className="rounded-sm bg-gray-100 px-4 py-1 text-gray-900" onClick={() => setShowModal(false)}>
                Cancel
              </button>
              <button
                onClick={() => {
                  formRef.current?.requestSubmit()
                }}
                type="submit"
                className="flex items-center gap-1 rounded-sm bg-blue-500 px-4 py-1 text-white"
              >
                Run
              </button>
            </div>
          </DialogFooter>
        </div>
      </DialogContent>
    </Dialog>
  )
}
