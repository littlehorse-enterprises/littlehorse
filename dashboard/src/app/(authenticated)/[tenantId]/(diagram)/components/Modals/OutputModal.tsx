import { FC } from 'react'
import { Modal } from '../../context'
import { useModal } from '../../hooks/useModal'
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog'

export const OutputModal: FC<Modal<{ message: string; label: string }>> = ({ data }) => {
  const { message, label } = data
  const { showModal, setShowModal } = useModal()
  return (
    <Dialog open={showModal} onOpenChange={open => setShowModal(open)}>
      <DialogContent className=" max-w-ful">
        <DialogHeader>
          <DialogTitle>{label}</DialogTitle>
        </DialogHeader>
        <div className="mb-2 flex flex-col gap-2 overflow-y-auto border-b border-slate-200 pb-2">{message}</div>
      </DialogContent>
    </Dialog>
  )
}
