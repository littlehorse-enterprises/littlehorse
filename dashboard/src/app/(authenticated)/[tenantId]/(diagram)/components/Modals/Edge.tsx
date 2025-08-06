import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { Edge as EdgeProto, VariableMutation } from 'littlehorse-client/proto'
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
        {variableMutations.map((mutation: VariableMutation) => (
          <div key={mutation.lhsName} className="mb-1 flex items-center gap-1">
            <span className="rounded	bg-gray-100 px-2 py-1 font-mono text-fuchsia-500">{`${mutation.lhsName}${mutation.lhsJsonPath ? `.${mutation.lhsJsonPath}` : ''}`}</span>
            <span className="rounded bg-green-300 p-1 text-xs">{mutation.operation}</span>

            <MutationRhS rhsValue={mutation.rhsValue} />
          </div>
        ))}
      </DialogContent>
    </Dialog>
  )
}
