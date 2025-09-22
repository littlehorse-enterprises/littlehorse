import { getVariable } from '@/app/utils'
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { VariableAssignment } from 'littlehorse-client/proto'
import { FC } from 'react'
import { Modal } from '../../context'
import { useModal } from '../../hooks/useModal'

export const VariableAssigment: FC<Modal<VariableAssignment>> = ({ data }) => {
  if (!data.source) return null
  const { showModal, setShowModal } = useModal()
  const variable = getVariable(data)

  return (
    <Dialog open={showModal} onOpenChange={open => setShowModal(open)}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Variable Assignment</DialogTitle>
        </DialogHeader>

        <div className="flex flex-col gap-2">
          <div className="flex flex-col gap-2">
            <small className="text-[0.75em] text-slate-400">Source</small>
            {data.source.$case}
          </div>
          <div className="flex flex-col gap-2">
            <small className="text-[0.75em] text-slate-400">Value</small>
            <p className="break-all font-mono">{variable}</p>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  )
}
