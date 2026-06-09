'use client'

import { lhPathToString } from '@/app/utils/lhPath'
import { Badge, IdentifierBadge } from '@/components/ui/badge'
import { ThreadSpec, VariableMutation } from 'littlehorse-client/proto'
import { FC, useMemo } from 'react'
import { VariableAssignment } from '../../../components/Sidebar/Components'
import { SpecEmpty, SpecSectionTitle } from './SpecTags'

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

  if (mutations.length === 0) {
    return (
      <div>
        <SpecSectionTitle>Mutations</SpecSectionTitle>
        <SpecEmpty>No mutations</SpecEmpty>
      </div>
    )
  }

  return (
    <div>
      <SpecSectionTitle>Mutations</SpecSectionTitle>
      <ul className="space-y-2">
        {mutations.map((mutation, index) => (
          <li
            key={`${mutation.lhsName}-${index}`}
            className="flex flex-wrap items-center gap-1 rounded-md border border-gray-100 px-3 py-2"
          >
            <IdentifierBadge name={mutation.lhsName} />
            <Badge className="bg-green-300">{mutation.operation}</Badge>
            <MutationRhS rhsValue={mutation.rhsValue} />
          </li>
        ))}
      </ul>
    </div>
  )
}

export const MutationRhS: FC<{ rhsValue: VariableMutation['rhsValue'] }> = ({ rhsValue }) => {
  if (!rhsValue) return null

  switch (rhsValue.$case) {
    case 'nodeOutput':
      return <NodeOutput value={rhsValue} />
    case 'rhsAssignment':
      return <RhsAssignment value={rhsValue} />
    case 'literalValue':
      return <LiteralValue value={rhsValue} />
    default:
      return null
  }
}

const NodeOutput: FC<{ value: Extract<VariableMutation['rhsValue'], { $case: 'nodeOutput' }> }> = ({
  value: { value: nodeOutput },
}) => (
  <>
    <Badge className="bg-gray-200">Node Output</Badge>
    {nodeOutput.path && nodeOutput.path.$case === 'jsonpath' && (
      <Badge className="bg-gray-100 font-mono text-orange-500">{nodeOutput.path.value}</Badge>
    )}
    {nodeOutput.path && nodeOutput.path.$case === 'lhPath' && (
      <Badge className="bg-gray-100 font-mono text-orange-500">{lhPathToString(nodeOutput.path.value)}</Badge>
    )}
  </>
)

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
