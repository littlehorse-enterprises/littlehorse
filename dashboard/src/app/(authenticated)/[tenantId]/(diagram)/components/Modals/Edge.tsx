import { getVariable, getVariableValue } from '@/app/utils'
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { Edge as EdgeProto, VariableMutation } from 'littlehorse-client/proto'
import { FC } from 'react'
import { Modal } from '../../context'
import { useModal } from '../../hooks/useModal'

export const Edge: FC<Modal<EdgeProto>> = ({ data }) => {
  const { variableMutations } = data
  const { showModal, setShowModal } = useModal()

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

const MutationRhS: FC<{ rhsValue: VariableMutation['rhsValue'] }> = ({ rhsValue }) => {
  if (!rhsValue) return <></>

  switch (rhsValue.$case) {
    case 'nodeOutput':
      return <NodeOutput value={rhsValue} />
    case 'rhsAssignment':
      return <RhsAssignment value={rhsValue} />
    case 'literalValue':
      return <LiteralValue value={rhsValue} />

    default:
      break
  }
}

const NodeOutput: FC<{ value: Extract<VariableMutation['rhsValue'], { $case: 'nodeOutput' }> }> = ({
  value: { value: nodeOutput },
}) => {
  return (
    <>
      <span className="rounded bg-gray-200 p-1 text-xs">Node Output</span>
      {nodeOutput.jsonpath && (
        <span className="rounded bg-gray-100 p-1 font-mono text-xs text-orange-500">{nodeOutput.jsonpath}</span>
      )}
    </>
  )
}

const RhsAssignment: FC<{ value: Extract<VariableMutation['rhsValue'], { $case: 'rhsAssignment' }> }> = ({
  value: { value: rhsAssignment },
}) => {
  return (
    <>
      <span className="rounded bg-gray-200 p-1 text-xs">RHS Assignment</span>
      <span className="rounded bg-gray-100 p-1 font-mono text-xs text-orange-500">{getVariable(rhsAssignment)}</span>
    </>
  )
}

const LiteralValue: FC<{ value: Extract<VariableMutation['rhsValue'], { $case: 'literalValue' }> }> = ({
  value: { value: literalValue },
}) => {
  return (
    <>
      <span className="rounded bg-gray-200 p-1 text-xs">Literal Value</span>
      <span className="rounded bg-gray-100 p-1 font-mono text-xs text-orange-500">
        {getVariableValue(literalValue)}
      </span>
    </>
  )
}
