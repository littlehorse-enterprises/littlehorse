import React, { useRef } from 'react'
import { useModal } from '../../hooks/useModal'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog'
import { Modal } from '../../context'
import { FC } from 'react'
import { WfSpec } from 'littlehorse-client/proto'
import { runWfSpec } from '../../wfSpec/[...props]/actions/runWfSpec'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { useRouter } from 'next/navigation'
import { WfForm } from '@/components/ui/wfForm'

export const ExecuteWorkflowRun: FC<Modal> = ({ data }) => {
  const { showModal, setShowModal } = useModal()
  const lhWorkflowSpec = data as WfSpec
  const { tenantId } = useWhoAmI()
  const router = useRouter()
  const formRef = useRef<any>(null);
  const handleFormSubmit = async (event: any) => {
    console.log('values', event)
    // event.preventDefault()
    if (!lhWorkflowSpec.id) return
    const wfRun = await runWfSpec({
      ...lhWorkflowSpec.id,
      wfSpecName: lhWorkflowSpec.id.name,
      tenantId,
      // id:"test-variables-greet-3",
      variables: {
        'input-name': { str: 'qamar' },
      },
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
  const triggerSubmit = () => {
    
  };
  return (
    <Dialog open={showModal} onOpenChange={open => setShowModal(open)}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Execute Workflow</DialogTitle>
        </DialogHeader>
        <div>
            {lhWorkflowSpec.threadSpecs?.entrypoint && 
            lhWorkflowSpec.threadSpecs?.entrypoint?.variableDefs?.some(def => def.required) ? (
              <WfForm variableDefs={lhWorkflowSpec.threadSpecs.entrypoint.variableDefs} onSubmit={handleFormSubmit} formRef={formRef} />
            ) : (
              <p>Currently no variables required!</p>
            )}
            <DialogFooter>
              <div className="mt-2 flex gap-1">
                <button className="rounded-sm bg-gray-100 px-4 py-1 text-gray-900" onClick={() => setShowModal(false)}>
                  Cancel
                </button>
                <button onClick={() => {formRef.current?.requestSubmit()}}  type="submit" className="flex items-center gap-1 rounded-sm bg-blue-500 p-1 px-4 text-white">
                  Run
                </button>
              </div>
            </DialogFooter>
        </div>
      </DialogContent>
    </Dialog>
  )
}
