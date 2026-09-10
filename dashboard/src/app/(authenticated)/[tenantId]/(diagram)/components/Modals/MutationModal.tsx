import { IdentifierBadge } from '@/components/ui/badge'
import { variableMutationLhsToString } from '@/app/utils/variables'
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { VariableMutation, VariableMutationType } from 'littlehorse-client/proto'
import { FC } from 'react'
import { Modal } from '../../context'
import { useModal } from '../../hooks/useModal'
import { MutationRhS } from '../../wfSpec/[...props]/components/Mutations'

export const MutationModal: FC<Modal<VariableMutation>> = ({ data }) => {
  const { operation, rhsValue } = data
  const { showModal, setShowModal } = useModal()
  const lhs = variableMutationLhsToString(data)
  return (
    <Dialog open={showModal} onOpenChange={open => setShowModal(open)}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Mutation</DialogTitle>
        </DialogHeader>
        <div className="mb-2 flex flex-col gap-2 border-b border-slate-200 pb-2" key={lhs}>
          <div className="flex flex-col gap-2">
            <small className="text-[0.75em] text-slate-400">Variable</small>
            <IdentifierBadge name={lhs} />
          </div>
          <div className="flex flex-col gap-2">
            <small className="text-[0.75em] text-slate-400">Operation</small>
            {VariableMutationType[operation]}
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
