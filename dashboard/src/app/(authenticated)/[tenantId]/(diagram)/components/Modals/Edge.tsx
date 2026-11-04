import { variableMutationLhsToString } from '@/app/utils/variables'
import { IdentifierBadge } from '@/components/ui/badge'
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { VariableMutationType } from 'littlehorse-client/proto'
import { FC } from 'react'
import { Modal } from '../../context'
import { useModal } from '../../hooks/useModal'
import { MutationRhS } from '../../wfSpec/[...props]/components/Mutations'
import { EdgeConditionDetail } from '../EdgeTypes/EdgeConditionDetail'
import type { DiagramEdgeData } from '../EdgeTypes/extractEdges'

export const Edge: FC<Modal<DiagramEdgeData>> = ({ data }) => {
  const { variableMutations, edgeCondition } = data
  const { showModal, setShowModal } = useModal()
  const hasMutations = variableMutations.length > 0
  const hasCondition = edgeCondition?.oneofKind !== undefined
  if (!hasMutations && !hasCondition) return
  const title = hasCondition && hasMutations ? 'Edge' : hasCondition ? 'Branch Condition' : 'Mutations'

  return (
    <Dialog open={showModal} onOpenChange={open => setShowModal(open)}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
        </DialogHeader>
        {hasCondition && (
          <div className={hasMutations ? 'mb-4 border-b border-slate-200 pb-4' : undefined}>
            <EdgeConditionDetail edge={data} />
          </div>
        )}
        {variableMutations.map(mutation => {
          const lhs = variableMutationLhsToString(mutation)
          return (
            <div className="mb-2 flex flex-col gap-2 border-b border-slate-200 pb-2" key={lhs}>
              <div className="flex flex-col gap-2">
                <small className="text-[0.75em] text-slate-400">Variable</small>
                <IdentifierBadge name={lhs} />
              </div>
              <div className="flex flex-col gap-2">
                <small className="text-[0.75em] text-slate-400">Operation</small>
                {VariableMutationType[mutation.operation]}
              </div>
              <div className="flex flex-col gap-2">
                <small className="text-[0.75em] text-slate-400">Value</small>
                <MutationRhS rhsValue={mutation.rhsValue} />
              </div>
            </div>
          )
        })}
      </DialogContent>
    </Dialog>
  )
}
