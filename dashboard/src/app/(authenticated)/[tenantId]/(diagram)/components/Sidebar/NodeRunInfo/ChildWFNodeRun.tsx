import { RunChildWfNodeRun as RunChildWfNodeRunProto } from 'littlehorse-client/proto'
import { FC } from 'react'
import { NodeVariable } from '../Components/NodeVariable'
import { wfRunIdToPath } from '@/app/utils/wfRun'

export const ChildWFNodeRun: FC<{ node: RunChildWfNodeRunProto }> = ({ node }) => {
  const childWfRunLink = node.childWfRunId ? `/wfRun/${wfRunIdToPath(node.childWfRunId)}` : ''
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
