import { RunChildWfNodeRun as RunChildWfNodeRunProto } from 'littlehorse-client/proto'
import { FC } from 'react'
import { LabelContent } from '../Components'

export const ChildWFNodeRun: FC<{ node: RunChildWfNodeRunProto }> = ({ node }) => {
  return (
    <div>
      <LabelContent label="Node Type" content="Child workflow node"></LabelContent>

      <LabelContent label="Work Flow Id" content={`${node.childWfRunId?.id}`}></LabelContent>
    </div>
  )
}
