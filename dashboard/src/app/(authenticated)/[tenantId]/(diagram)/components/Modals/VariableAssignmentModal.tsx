import { getVariable } from '@/app/utils'
import { tryFormatAsJson } from '@/app/utils/tryFormatAsJson'
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { VariableAssignment } from 'littlehorse-client/proto'
import { FC } from 'react'
import { CopyButton } from '../../../components/CopyButton'
import { Modal } from '../../context'
import { useModal } from '../../hooks/useModal'

export const VariableAssignmentModal: FC<Modal<VariableAssignment>> = ({ data }) => {
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
            <div className="flex flex-col border border-slate-200 bg-slate-50 p-2">
              <div className="flex w-full justify-end">
                <CopyButton className="h-4 w-4 text-slate-400" value={variable} />
              </div>
              <p className="break-all font-mono">{tryFormatAsJson(variable)}</p>
            </div>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  )
}
