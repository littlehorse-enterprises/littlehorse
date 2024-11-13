import React from 'react'
import { useModal } from '../../hooks/useModal'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog'
import { Modal } from '../../context'
import { FC } from 'react'
import { WfSpec } from 'littlehorse-client/proto'
import { runWfSpec } from '../../wfSpec/[...props]/actions/runWfSpec'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { useRouter } from 'next/navigation'

export const ExecuteWorkflowRun: FC<Modal> = ({ data }) => {
  const { showModal, setShowModal } = useModal()
  const lhWorkflowSpec = data as WfSpec
  const { tenantId } = useWhoAmI()
  const router = useRouter()
  const handleFormSubmit = async (event: any) => {
    event.preventDefault()
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
  return (
    <Dialog open={showModal} onOpenChange={open => setShowModal(open)}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Execute Workflow</DialogTitle>
        </DialogHeader>
        <div>
          <form onSubmit={handleFormSubmit}>
            {lhWorkflowSpec.threadSpecs?.entrypoint &&
            lhWorkflowSpec.threadSpecs?.entrypoint?.variableDefs?.some(def => def.required) ? (
              lhWorkflowSpec.threadSpecs.entrypoint.variableDefs
                .filter(def => def.required)
                .map((def, index) => (
                  <div key={index} className="my-4">
                    <label className="mr-2">{def.varDef?.name}</label>
                    {def.varDef?.type === 'DOUBLE' ? (
                      <textarea
                        name={def.varDef?.name}
                        required={def.required}
                        placeholder={def.varDef?.name}
                        rows={5}
                      />
                    ) : (
                      <input
                        type={def.varDef?.type === 'INT' ? 'number' : 'text'}
                        name={def.varDef?.name}
                        required={def.required}
                        placeholder={def.varDef?.name}
                      />
                    )}
                  </div>
                ))
            ) : (
              <p>Currently no variables required!</p>
            )}
            <DialogFooter>
              <div className="mt-2 flex gap-1">
                <button className="rounded-sm bg-gray-100 px-4 py-1 text-gray-900" onClick={() => setShowModal(false)}>
                  Cancel
                </button>
                <button type="submit" className="flex items-center gap-1 rounded-sm bg-blue-500 p-1 px-4 text-white">
                  Run
                </button>
              </div>
            </DialogFooter>
          </form>
        </div>
      </DialogContent>
    </Dialog>
  )
}
