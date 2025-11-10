import { ThrowEventNodeRun } from 'littlehorse-client/proto'
import { FC } from 'react'
import { LabelContent } from '../Components'

export const ThrowEventRunNode: FC<{ node: ThrowEventNodeRun }> = ({ node }) => {
  return (
    <div>
      <LabelContent label="Node Type" content="Throw event"></LabelContent>
      <LabelContent label="Work flow event id" content={node.workflowEventId?.wfRunId?.id}></LabelContent>
      <LabelContent label="Work flow run id" content={node.workflowEventId?.workflowEventDefId?.name}></LabelContent>
    </div>
  )
}
