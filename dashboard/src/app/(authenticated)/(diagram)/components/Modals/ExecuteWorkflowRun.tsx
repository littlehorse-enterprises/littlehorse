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

export const ExecuteWorkflowRun: FC<Modal> = ({ data }) => {
  const { showModal, setShowModal } = useModal()
  const lhWorkflowSpec = data as WfSpec
  console.log(lhWorkflowSpec)
  const { tenantId } = useWhoAmI()
  const router = useRouter()
  const formRef = useRef<any>(null)
  const wfSpecVariables = lhWorkflowSpec.threadSpecs?.entrypoint?.variableDefs
  const formatVariablesPayload = (values): any => {
    console.log('values', values)
    const transformedObj = Object.keys(values).reduce((acc, key) => {
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
    console.log(wfRun)
    if (!wfRun.id) return
    setShowModal(false)
    router.push(`/wfRun/${wfRun.id.id}`)
  }

  return (
    <Dialog open={showModal} onOpenChange={open => setShowModal(open)}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Execute {lhWorkflowSpec.id?.name}</DialogTitle>
        </DialogHeader>
        <div>
          <hr className="mb-2" />
          <WfRunForm wfSpecVariables={wfSpecVariables} onSubmit={handleFormSubmit} ref={formRef} />
          <DialogFooter>
            <div className="mt-2 flex gap-1">
              <button className="rounded-sm bg-gray-100 px-4 py-1 text-gray-900" onClick={() => setShowModal(false)}>
                Cancel
              </button>
              <button
                onClick={() => {
                  formRef.current?.requestSubmit()
                }}
                type="submit"
                className="flex items-center gap-1 rounded-sm bg-blue-500 p-1 px-4 text-white"
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
