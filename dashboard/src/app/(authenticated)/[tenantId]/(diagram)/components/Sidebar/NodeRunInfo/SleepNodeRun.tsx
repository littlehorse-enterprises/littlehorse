import { SleepNodeRun as SleepNodeRunProto } from 'littlehorse-client/proto'
import { FC } from 'react'
import { NodeVariable } from '../Components/NodeVariable'

export const SleepNodeRun: FC<{ node: SleepNodeRunProto }> = ({ node }) => {
  return (
    <div>
      <NodeVariable label="Node Type:" text="Sleep" />
      <NodeVariable label="maturationTime:" text={node.maturationTime} type="date" />
      <NodeVariable label="matured:" text={node.matured ? 'Yes' : 'No'} />
    </div>
  )
}
