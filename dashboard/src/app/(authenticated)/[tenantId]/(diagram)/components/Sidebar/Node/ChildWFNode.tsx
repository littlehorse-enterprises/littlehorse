import { RunChildWfNode, VariableAssignment as VariableAssignmentProto } from 'littlehorse-client/proto'
import { FC } from 'react'
import { LabelContent, VariableAssignment } from '../Components'

export const ChildWFNode: FC<{ node: RunChildWfNode }> = ({ node }) => {
  return (
    <div>
      <LabelContent label="Work Flow spec name" content={`${node.wfSpecName}`}></LabelContent>
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
