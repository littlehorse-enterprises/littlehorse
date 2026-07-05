import { lhPathToString } from '@/app/utils/lhPath'
import { Badge, IdentifierBadge } from '@/components/ui/badge'
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
          <IdentifierBadge name={mutation.lhsName} />
          <Badge className="bg-green-300">{mutation.operation}</Badge>
          <MutationRhS rhsValue={mutation.rhsValue} />
        </div>
      ))}
    </div>
  )
}

export const MutationRhS: FC<{ rhsValue: VariableMutation['rhsValue'] }> = ({ rhsValue }) => {
  if (!rhsValue) return <></>
  switch (rhsValue.oneofKind) {
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

const NodeOutput: FC<{ value: Extract<VariableMutation['rhsValue'], { oneofKind: 'nodeOutput' }> }> = ({
  value: { nodeOutput },
}) => {
  return (
    <>
      <Badge className="bg-gray-200">Node Output</Badge>
      {nodeOutput.path && nodeOutput.path.oneofKind == 'jsonpath' && (
        <Badge className="bg-gray-100 font-mono text-orange-500">{nodeOutput.path.jsonpath}</Badge>
      )}
      {nodeOutput.path && nodeOutput.path.oneofKind == 'lhPath' && (
        <Badge className="bg-gray-100 font-mono text-orange-500">{lhPathToString(nodeOutput.path.lhPath)}</Badge>
      )}
    </>
  )
}

const RhsAssignment: FC<{ value: Extract<VariableMutation['rhsValue'], { oneofKind: 'rhsAssignment' }> }> = ({
  value: { rhsAssignment },
}) => {
  return <VariableAssignment variableAssigment={rhsAssignment} />
}

const LiteralValue: FC<{ value: Extract<VariableMutation['rhsValue'], { oneofKind: 'literalValue' }> }> = ({
  value: { literalValue },
}) => {
  return (
    <VariableAssignment
      variableAssigment={{ path: { oneofKind: undefined }, source: { oneofKind: 'literalValue', literalValue } }}
    />
  )
}
