import { WaitForChildWfNode as WaitForChildWfNodeProto } from 'littlehorse-client/proto'
import { FC } from 'react'
import { LabelContent, VariableAssignment } from '../Components'
import './node.css'

export const WaitForChildWfNode: FC<{ node: WaitForChildWfNodeProto }> = ({ node }) => {
  const { childWfRunId, childWfRunSourceNode } = node

  return (
    <div className="flex max-w-full flex-1 flex-col gap-2">
      <small className="node-title">Child WfRun Id</small>
      <div className="mb-2 flex items-center">
        {childWfRunId ? (
          <VariableAssignment variableAssigment={childWfRunId} />
        ) : (
          <p className="text-lg font-medium">—</p>
        )}
      </div>
      {childWfRunSourceNode && <LabelContent label="Child WfRun Source Node" content={childWfRunSourceNode} />}
    </div>
  )
}
