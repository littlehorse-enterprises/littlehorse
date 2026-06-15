import { IdentifierBadge } from '@/components/ui/badge'
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { FC } from 'react'
import type { EdgeData } from '../EdgeTypes/Default'
import { EdgeConditionDetail } from '../EdgeTypes/EdgeConditionDetail'
import { Modal } from '../../context'
import { useModal } from '../../hooks/useModal'
import { MutationRhS } from '../../wfSpec/[...props]/components/Mutations'

export const Edge: FC<Modal<EdgeData>> = ({ data }) => {
  const { variableMutations, edgeCondition } = data
  const { showModal, setShowModal } = useModal()
  const hasMutations = variableMutations.length > 0
  const hasCondition = edgeCondition != null

  if (!hasMutations && !hasCondition) return null

  const title = hasCondition && hasMutations ? 'Edge' : hasCondition ? 'Branch Condition' : 'Mutations'

  return (
    <Dialog open={showModal} onOpenChange={open => setShowModal(open)}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
        </DialogHeader>
        {hasCondition ? (
          <div className={hasMutations ? 'mb-4 border-b border-slate-200 pb-4' : undefined}>
            <EdgeConditionDetail edge={data} />
          </div>
        ) : null}
        {hasMutations
          ? variableMutations.map(mutation => (
              <div
                className="mb-2 flex flex-col gap-2 border-b border-slate-200 pb-2"
                key={mutation.lhsName + mutation.lhsJsonPath}
              >
                <div className="flex flex-col gap-2">
                  <small className="text-[0.75em] text-slate-400">Variable</small>
                  <IdentifierBadge
                    name={`${mutation.lhsName}${mutation.lhsJsonPath ? `.${mutation.lhsJsonPath}` : ''}`}
                  />
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
            ))
          : null}
      </DialogContent>
    </Dialog>
  )
}
