import { VariableMutation } from 'littlehorse-client/dist/proto/common_wfspec'
import { VariableValue } from 'littlehorse-client/dist/proto/variable'
import { ThreadSpec } from 'littlehorse-client/dist/proto/wf_spec'
import React, { FC, useMemo } from 'react'

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
      <h2 className="text-md font-bold mb-2">Mutations</h2>
      {mutations.map(mutation => (
        <div key={mutation.lhsName} className="flex items-center gap-1 mb-1">
          <span className="text-fuchsia-500	font-mono bg-gray-100 rounded py-1 px-2">{mutation.lhsName}</span>
          <span className="text-xs bg-green-300 rounded p-1">{mutation.operation}</span>
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
      <span className="text-xs bg-gray-200 rounded p-1">Node Output</span>
      {nodeOutput.jsonpath && (
        <span className="text-xs text-orange-500 font-mono bg-gray-100 rounded p-1">{nodeOutput.jsonpath}</span>
      )}
    </>
  )
}

const LiteralValue: FC<Pick<VariableMutation, 'literalValue'>> = ({ literalValue }) => {
  if (!literalValue) return <></>
  const type = Object.keys(literalValue)[0] as keyof VariableValue
  return (
    <>
      <span className="text-xs bg-gray-200 rounded p-1">Literal Value</span>
      <span className="text-xs text-orange-500 font-mono bg-gray-100 rounded p-1">
        {literalValue[type]?.toString()}
      </span>
    </>
  )
}
