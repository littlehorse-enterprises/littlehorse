import { getVariable, getVariableValue } from '@/app/utils'
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { Edge as EdgeProto, VariableMutation } from 'littlehorse-client/proto'
import { FC } from 'react'
import { Modal } from '../../context'
import { useModal } from '../../hooks/useModal'

export const Edge: FC<Modal> = ({ data }) => {
  const { variableMutations } = data as EdgeProto
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
            <NodeOutput nodeOutput={mutation.nodeOutput} />
            <LiteralValue literalValue={mutation.literalValue} />
            <RhsAssignment rhsAssignment={mutation.rhsAssignment} />
          </div>
        ))}
      </DialogContent>
    </Dialog>
  )
}

const NodeOutput: FC<Pick<VariableMutation, 'nodeOutput'>> = ({ nodeOutput }) => {
  if (!nodeOutput) return <></>
  return (
    <>
      <span className="rounded bg-gray-200 p-1 text-xs">Node Output</span>
      {nodeOutput.jsonpath && (
        <span className="rounded bg-gray-100 p-1 font-mono text-xs text-orange-500">{nodeOutput.jsonpath}</span>
      )}
    </>
  )
}

const RhsAssignment: FC<Pick<VariableMutation, 'rhsAssignment'>> = ({ rhsAssignment }) => {
  if (!rhsAssignment) return <></>
  return (
    <>
      <span className="rounded bg-gray-200 p-1 text-xs">RHS Assignment</span>
      <span className="rounded bg-gray-100 p-1 font-mono text-xs text-orange-500">
        {String(getVariable(rhsAssignment))}
      </span>
    </>
  )
}

const LiteralValue: FC<Pick<VariableMutation, 'literalValue'>> = ({ literalValue }) => {
  if (!literalValue) return <></>
  return (
    <>
      <span className="rounded bg-gray-200 p-1 text-xs">Literal Value</span>
      <span className="rounded bg-gray-100 p-1 font-mono text-xs text-orange-500">
        {String(getVariableValue(literalValue))}
      </span>
    </>
  )
}
