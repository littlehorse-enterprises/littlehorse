import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { VariableMutation } from 'littlehorse-client/proto'
import { FC } from 'react'
import { Modal } from '../../context'
import { useModal } from '../../hooks/useModal'
import { MutationRhS } from '../../wfSpec/[...props]/components/Mutations'

export const MutationModal: FC<Modal<VariableMutation>> = ({ data }) => {
  const { lhsName, lhsJsonPath, operation, rhsValue } = data
  const { showModal, setShowModal } = useModal()

  return (
    <Dialog open={showModal} onOpenChange={open => setShowModal(open)}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Mutation</DialogTitle>
        </DialogHeader>
        <div className="mb-2 flex flex-col gap-2 border-b border-slate-200 pb-2" key={lhsName + lhsJsonPath}>
          <div className="flex flex-col gap-2">
            <small className="text-[0.75em] text-slate-400">Variable</small>
            <span className="rounded	bg-gray-100 px-2 py-1 font-mono text-fuchsia-500">{`${lhsName}${lhsJsonPath ? `.${lhsJsonPath}` : ''}`}</span>
          </div>
          <div className="flex flex-col gap-2">
            <small className="text-[0.75em] text-slate-400">Operation</small>
            {operation}
          </div>
          <div className="flex flex-col gap-2">
            <small className="text-[0.75em] text-slate-400">Value</small>
            <MutationRhS rhsValue={rhsValue} />
          </div>
        </div>
      </DialogContent>
    </Dialog>
  )
}
