import { lhPathToString } from '@/app/utils/lhPath'
import { ThreadSpec, VariableMutation } from 'littlehorse-client/proto'
import { FC, useMemo } from 'react'
import { VariableAssignment } from '../../../components/Sidebar/Components'

type Props = Pick<ThreadSpec, 'nodes'>
export const Mutations: FC<Props> = ({ nodes }) => {
  const mutations = useMemo(() => {
    return Object.values(nodes).reduce<VariableMutation[]>((acc, node) => {
      for (const mutation of node.outgoingEdges) {
        acc = [...acc, ...mutation.variableMutations]
      }
      return acc
    }, [])
  }, [nodes])

  if (mutations.length === 0) return <p className="font-semibold">No mutations</p>

  return (
    <div className="">
      <h2 className="text-md mb-2 font-bold">Mutations</h2>
      {mutations.map(mutation => (
        <div key={mutation.lhsName} className="mb-1 flex items-center gap-1">
          <span className="rounded	bg-gray-100 px-2 py-1 font-mono text-fuchsia-500">{mutation.lhsName}</span>
          <span className="rounded bg-green-300 p-1 text-xs">{mutation.operation}</span>
          <MutationRhS rhsValue={mutation.rhsValue} />
        </div>
      ))}
    </div>
  )
}

export const MutationRhS: FC<{ rhsValue: VariableMutation['rhsValue'] }> = ({ rhsValue }) => {
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
      {nodeOutput.path && nodeOutput.path.$case == 'jsonpath' && (
        <span className="rounded bg-gray-100 p-1 font-mono text-xs text-orange-500">{nodeOutput.path.value}</span>
      )}
      {nodeOutput.path && nodeOutput.path.$case == 'lhPath' && (
        <span className="rounded bg-gray-100 p-1 font-mono text-xs text-orange-500">
          {lhPathToString(nodeOutput.path.value)}
        </span>
      )}
    </>
  )
}

const RhsAssignment: FC<{ value: Extract<VariableMutation['rhsValue'], { $case: 'rhsAssignment' }> }> = ({
  value: { value: rhsAssignment },
}) => {
  return <VariableAssignment variableAssigment={rhsAssignment} />
}

const LiteralValue: FC<{ value: Extract<VariableMutation['rhsValue'], { $case: 'literalValue' }> }> = ({
  value: { value: literalValue },
}) => {
  return <VariableAssignment variableAssigment={{ source: { $case: 'literalValue', value: literalValue } }} />
}
