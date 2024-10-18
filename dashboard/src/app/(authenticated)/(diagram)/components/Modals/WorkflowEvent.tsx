import { getVariableValue } from '@/app/utils'
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { cn } from '@/components/utils'
import { WorkflowEvent as LHWorkflowEvent } from 'littlehorse-client/proto'
import { ClipboardIcon } from 'lucide-react'
import { FC } from 'react'
import { Modal } from '../../context'
import { useModal } from '../../hooks/useModal'

export const WorkflowEvent: FC<Modal> = ({ data }) => {
  const lhWorkflowEvent = data as LHWorkflowEvent
  const { showModal, setShowModal } = useModal()

  return (
    <Dialog open={showModal} onOpenChange={open => setShowModal(open)}>
      <DialogContent className="flex flex-col">
        <DialogHeader>
          <DialogTitle className="flex items-center justify-between">
            <h2 className="text-lg font-bold">WorkflowEvent</h2>
            <div className="item-center flex gap-1 bg-gray-200 px-2 py-1">
              <span className="font-mono text-sm">{lhWorkflowEvent.id?.workflowEventDefId?.name}</span>
              <ClipboardIcon
                className="h-4 w-4 fill-transparent stroke-blue-500"
                onClick={() => {
                  navigator.clipboard.writeText(lhWorkflowEvent.id?.workflowEventDefId?.name ?? '')
                }}
              />
            </div>
          </DialogTitle>
        </DialogHeader>

        <div>
          <div className="flex items-center gap-2">
            <div className="font-bold">Time Thrown:</div>
            <div className="">{lhWorkflowEvent.createdAt}</div>
          </div>
          <div className="flex items-center gap-2">
            <div className="font-bold">Sequence Number:</div>
            <div className="">{lhWorkflowEvent.id?.number}</div>
          </div>
        </div>

        <div className={cn('flex w-full flex-col overflow-auto rounded p-1', 'bg-zinc-500 text-white')}>
          <h3 className="font-bold">Content</h3>
          <pre className="overflow-auto">{getVariableValue(lhWorkflowEvent.content)}</pre>
        </div>
      </DialogContent>
    </Dialog>
  )
}
