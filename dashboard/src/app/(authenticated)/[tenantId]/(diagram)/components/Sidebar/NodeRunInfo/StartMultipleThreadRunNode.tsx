import { StartMultipleThreadsRun } from 'littlehorse-client/proto'
import { FC } from 'react'
import { NodeVariable } from '../Components/NodeVariable'

export const StartMultipleThreadRunNode: FC<{ node: StartMultipleThreadsRun }> = ({ node }) => {
  return (
    <div>
      <NodeVariable label="Node Type" text="Start multiple threads "></NodeVariable>
      <NodeVariable label="threadSpecName:" text={node.threadSpecName}></NodeVariable>
      {node.childThreadIds.map(thread => {
        return <div className="ml-1 text-blue-500">{thread}</div>
      })}
    </div>
  )
}
