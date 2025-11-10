import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { Edge as EdgeProto } from 'littlehorse-client/proto'
import { FC } from 'react'
import { Modal } from '../../context'
import { useModal } from '../../hooks/useModal'
import { MutationRhS } from '../../wfSpec/[...props]/components/Mutations'

export const Edge: FC<Modal<EdgeProto>> = ({ data }) => {
  const { variableMutations } = data
  const { showModal, setShowModal } = useModal()
  if (variableMutations.length === 0) return

  return (
    <Dialog open={showModal} onOpenChange={open => setShowModal(open)}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Mutations</DialogTitle>
        </DialogHeader>
        {variableMutations.map(mutation => (
          <div
            className="mb-2 flex flex-col gap-2 border-b border-slate-200 pb-2"
            key={mutation.lhsName + mutation.lhsJsonPath}
          >
            <div className="flex flex-col gap-2">
              <small className="text-[0.75em] text-slate-400">Variable</small>
              <span className="rounded	bg-gray-100 px-2 py-1 font-mono text-fuchsia-500">{`${mutation.lhsName}${mutation.lhsJsonPath ? `.${mutation.lhsJsonPath}` : ''}`}</span>
            </div>
            <div className="flex flex-col gap-2">
              <small className="text-[0.75em] text-slate-400">Operation</small>
              {mutation.operation}
            </div>
            <div className="flex flex-col gap-2">
              <small className="text-[0.75em] text-slate-400">Value</small>
              <MutationRhS rhsValue={mutation.rhsValue} />
            </div>
          </div>
        ))}
      </DialogContent>
    </Dialog>
  )
}
