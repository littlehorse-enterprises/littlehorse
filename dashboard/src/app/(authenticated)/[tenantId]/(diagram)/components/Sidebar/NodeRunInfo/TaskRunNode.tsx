import { TaskNodeRun } from 'littlehorse-client/proto'
import { LabelContent } from '../Components'
import { FC } from 'react'

export const TaskRunNode: FC<{ node: TaskNodeRun }> = ({ node }) => {
  return (
    <div>
      <LabelContent label="Node Type" content="Task"></LabelContent>
      <LabelContent label="Task Run identifier" content={node.taskRunId?.taskGuid}></LabelContent>
    </div>
  )
}
