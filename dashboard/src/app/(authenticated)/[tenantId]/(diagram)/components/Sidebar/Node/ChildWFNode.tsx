import { RunChildWfNode, VariableAssignment as VariableAssignmentProto } from 'littlehorse-client/proto'
import { FC } from 'react'
import { LabelContent, VariableAssignment } from '../Components'

export const ChildWFNode: FC<{ node: RunChildWfNode }> = ({ node }) => {
  console.log(node)
  const { wfSpec } = node
  let wfSpecName = ""
  if (wfSpec?.$case === 'wfSpecName') {
    wfSpecName = wfSpec?.value
  }
  if (wfSpec?.$case === "wfSpecVar") {
    wfSpecName = "variable"
  }
  return (
    <div>
      <LabelContent label="Work Flow spec name" content={`${wfSpecName}`}></LabelContent>
      <LabelContent label="Major Version" content={`${node.majorVersion}`}></LabelContent>
      <div className="flex flex-col gap-2">
        <small className="node-title">Inputs</small>
        {Object.values(node.inputs).map((input: VariableAssignmentProto) => {
          return <VariableAssignment variableAssigment={input} />
        })}
      </div>
    </div>
  )
}
