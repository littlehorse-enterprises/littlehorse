import { StartMultipleThreadsRun } from 'littlehorse-client/proto'
import { FC } from 'react'
import { LabelContent } from '../Components'

export const StartMultipleThreadRunNode: FC<{ node: StartMultipleThreadsRun }> = ({ node }) => {
  return (
    <div>
      <LabelContent label="Node Type" content="Start multiple threads "></LabelContent>
      <LabelContent label="Thread spec name" content={node.threadSpecName}></LabelContent>
      {node.childThreadIds.map(thread => {
        return <div className='text-blue-500 ml-1'>{thread}</div>
      })}
    </div>
  )
}
