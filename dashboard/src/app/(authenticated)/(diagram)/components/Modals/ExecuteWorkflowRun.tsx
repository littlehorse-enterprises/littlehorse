import React, { useRef } from 'react'
import { useModal } from '../../hooks/useModal'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog'
import { Modal } from '../../context'
import { FC } from 'react'
import { WfSpec } from 'littlehorse-client/proto'

export const ExecuteWorkflowRun: FC<Modal> = ({ data }) => {
  const lhWorkflowSpec = data as WfSpec
  const { showModal, setShowModal } = useModal()
  const formRef = useRef<HTMLFormElement>(null);
  const handleFormSubmit = (values: any) => {
    alert(values)
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
                  <div key={index} className='my-4'>
                    <label className='mr-2'>{def.varDef?.name}</label>
                    {def.varDef?.type === "DOUBLE" ? (
                      <textarea
                        name={def.varDef?.name}
                        required={def.required}
                        placeholder={def.varDef?.name}
                        rows={5}
                      />
                    ) : (
                      <input
                        type={def.varDef?.type === "INT" ? "number" : "text"}
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
              <div className='flex gap-1 mt-2'>
                <button
                  className='bg-gray-100 text-gray-900 px-4 py-1 rounded-sm'
                  onClick={() => setShowModal(false)}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className='flex items-center gap-1 px-4 p-1 text-white bg-blue-500 rounded-sm'
                >
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
