import { ThreadSpec, VariableMutation, VariableValue } from 'littlehorse-client/proto'
import { FC, useMemo } from 'react'

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

  if (mutations.length === 0) return <></>

  return (
    <div className="">
      <h2 className="text-md mb-2 font-bold">Mutations</h2>
      {mutations.map(mutation => (
        <div key={mutation.lhsName} className="mb-1 flex items-center gap-1">
          <span className="rounded	bg-gray-100 px-2 py-1 font-mono text-fuchsia-500">{mutation.lhsName}</span>
          <span className="rounded bg-green-300 p-1 text-xs">{mutation.operation}</span>
          <NodeOutput nodeOutput={mutation.nodeOutput} />
          <LiteralValue literalValue={mutation.literalValue} />
        </div>
      ))}
    </div>
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

const LiteralValue: FC<Pick<VariableMutation, 'literalValue'>> = ({ literalValue }) => {
  if (!literalValue) return <></>
  const type = Object.keys(literalValue)[0] as keyof VariableValue
  return (
    <>
      <span className="rounded bg-gray-200 p-1 text-xs">Literal Value</span>
      <span className="rounded bg-gray-100 p-1 font-mono text-xs text-orange-500">
        {literalValue[type]?.toString()}
      </span>
    </>
  )
}
