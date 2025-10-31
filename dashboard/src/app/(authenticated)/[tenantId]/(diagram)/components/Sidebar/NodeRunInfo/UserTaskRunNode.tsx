import { UserTaskNodeRun } from 'littlehorse-client/proto'
import { FC } from 'react'
import { LabelContent } from '../Components'

export const UserTaskRunNode: FC<{ node: UserTaskNodeRun }> = ({ node }) => {
  return (
    <div>
      <LabelContent label="Node Type" content="User task"></LabelContent>
      <LabelContent label="Work flow id" content={node.userTaskRunId?.wfRunId?.id}></LabelContent>
      <LabelContent label="User task node id " content={node.userTaskRunId?.userTaskGuid}></LabelContent>
    </div>
  )
}
