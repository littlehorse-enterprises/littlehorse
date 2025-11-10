import { StartThreadRun } from 'littlehorse-client/proto'
import { FC } from 'react'
import { LabelContent } from '../Components'

export const StartThreadRunNode: FC<{ node: StartThreadRun }> = ({ node }) => {
  return (
    <div>
      <LabelContent label="Node Type" content="Start Thread"></LabelContent>
      <LabelContent label="Thread id of the child " content={`${node.childThreadId}`}></LabelContent>
      <LabelContent label="Thread Spec Name" content={`${node.threadSpecName}`}></LabelContent>
    </div>
  )
}
