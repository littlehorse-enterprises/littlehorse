import { getVariableValue } from '@/app/utils'
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { cn } from '@/components/utils'
import { ExternalEvent as LHExternalEvent } from 'littlehorse-client/proto'
import { ClipboardIcon } from 'lucide-react'
import { FC } from 'react'
import { Modal } from '../../context'
import { useModal } from '../../hooks/useModal'

export const ExternalEvent: FC<Modal> = ({ data }) => {
  const lhExternalEvent = data as LHExternalEvent
  const { showModal, setShowModal } = useModal()

  return (
    <Dialog open={showModal} onOpenChange={open => setShowModal(open)}>
      <DialogContent className="flex flex-col">
        <DialogHeader>
          <DialogTitle className="flex items-center justify-between">
            <h2 className="text-lg font-bold">ExternalEvent</h2>
            <div className="item-center flex gap-1 bg-gray-200 px-2 py-1">
              <span className="font-mono text-sm">{lhExternalEvent.id?.guid}</span>
              <ClipboardIcon className="h-4 w-4 fill-transparent stroke-blue-500" />
            </div>
          </DialogTitle>
        </DialogHeader>

        <div className="flex items-center gap-2">
          <div className="font-bold">Triggered:</div>
          <div className="">{lhExternalEvent.createdAt}</div>
        </div>

        <div className={cn('flex w-full flex-col overflow-auto rounded p-1', 'bg-zinc-500 text-white')}>
          <h3 className="font-bold">Content</h3>
          <pre className="overflow-auto">{getVariableValue(lhExternalEvent.content)}</pre>
        </div>
      </DialogContent>
    </Dialog>
  )
}
