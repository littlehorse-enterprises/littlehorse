import { RunChildWfNodeRun as RunChildWfNodeRunProto } from 'littlehorse-client/proto'
import { FC } from 'react'
import { NodeVariable } from '../Components/NodeVariable'

export const ChildWFNodeRun: FC<{ node: RunChildWfNodeRunProto }> = ({ node }) => {
  const childWfRunLink = `/wfRun/${node.childWfRunId?.parentWfRunId?.id}/${node.childWfRunId?.id}`
  return (
    <div>
      <NodeVariable label="Node Type" text="Child Workflow"></NodeVariable>
      <NodeVariable
        label="childWfRunId"
        text={`${node.childWfRunId?.id}`}
        type="link"
        link={childWfRunLink}
      ></NodeVariable>
    </div>
  )
}
