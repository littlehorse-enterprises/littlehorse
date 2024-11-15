import React, { useRef } from 'react'
import { useModal } from '../../hooks/useModal'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog'
import { Modal } from '../../context'
import { FC } from 'react'
import { ThreadVarDef, WfSpec } from 'littlehorse-client/proto'
import { runWfSpec } from '../../wfSpec/[...props]/actions/runWfSpec'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { useRouter } from 'next/navigation'
import { WfRunForm } from '@/app/(authenticated)/(diagram)/components/Forms/WfRunForm'
import { X } from 'lucide-react'

export const ExecuteWorkflowRun: FC<Modal> = ({ data }) => {
  const { showModal, setShowModal } = useModal()
  const lhWorkflowSpec = data as WfSpec
  const { tenantId } = useWhoAmI()
  const router = useRouter()
  const formRef = useRef<any>(null)
  const wfSpecVariables = lhWorkflowSpec.threadSpecs?.entrypoint?.variableDefs
  const formatVariablesPayload = (values: any) => {
    const transformedObj = Object.keys(values).reduce((acc: any, key) => {
      acc[key] = { [matchVariableType(key)]: values[key] }
      return acc
    }, {})

    return transformedObj
  }

  const matchVariableType = (key: string): string => {
    return wfSpecVariables.filter((variable: ThreadVarDef) => variable.varDef?.name === key)[0]?.varDef?.type as string
  }

  const handleFormSubmit = async (values: any) => {
    const customWfRunId = values['custom-id-wfRun-flow'] || undefined
    delete values['custom-id-wfRun-flow']
    if (!lhWorkflowSpec.id) return
    const wfRun = await runWfSpec({
      ...lhWorkflowSpec.id,
      wfSpecName: lhWorkflowSpec.id.name,
      tenantId,
      id: customWfRunId,
      variables: formatVariablesPayload(values),
      // parentWfRunId:{
      //   id: ,
      //   lhWorkflowSpec.parentWfSpec?.wfSpecName
      // }
    })
    if (!wfRun.id) return
    setShowModal(false)
    router.push(`/wfRun/${wfRun.id.id}`)
  }

  return (
    <Dialog open={showModal} onOpenChange={open => setShowModal(open)}>
      <DialogContent className="overflow-hidden p-0">
        <div className="relative flex max-h-[calc(100vh-50px)] flex-col">
          <DialogHeader className="sticky top-0 z-10 bg-background px-4 py-3 shadow-md ">
            <DialogTitle className="text-gray-700">
              Execute <span className="rounded-sm bg-green-200 px-2 py-1 text-black">{lhWorkflowSpec.id?.name}</span>
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
