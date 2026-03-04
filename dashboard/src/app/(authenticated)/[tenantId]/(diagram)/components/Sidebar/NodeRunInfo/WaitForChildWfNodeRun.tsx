import { wfRunIdToPath } from '@/app/utils/wfRun'
import { WaitForChildWfNodeRun as WaitForChildWfNodeRunProto } from 'littlehorse-client/proto'
import { FC } from 'react'
import { NodeVariable } from '../Components/NodeVariable'

export const WaitForChildWfNodeRun: FC<{ node: WaitForChildWfNodeRunProto }> = ({ node }) => {
  const childWfRunLink = node.childWfRunId ? `/wfRun/${wfRunIdToPath(node.childWfRunId)}` : ''

  return (
    <div className="ml-1 flex max-w-full flex-1 flex-col">
      <NodeVariable label="Node Type" text="Wait For Child Workflow" />
      {node.childWfRunId && (
        <NodeVariable label="Child WfRun Id" text={node.childWfRunId.id} type="link" link={childWfRunLink} />
      )}
    </div>
  )
}
