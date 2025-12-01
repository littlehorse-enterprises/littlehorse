import { StartThreadRun } from 'littlehorse-client/proto'
import { FC } from 'react'
import { NodeVariable } from '../Components/NodeVariable'

export const StartThreadNodeRun: FC<{ node: StartThreadRun }> = ({ node }) => {
  return (
    <div>
      <NodeVariable label="Node Type:" text="Start Thread" />
      <NodeVariable label="childThreadId:" text={`${node.childThreadId}`} />
      <NodeVariable label="threadSpecName:" text={`${node.threadSpecName}`} />
    </div>
  )
}
