import { SleepNodeRun } from 'littlehorse-client/proto'
import { FC } from 'react'
import { NodeVariable } from './NodeVariable'

export const SleepRunNode: FC<{ node: SleepNodeRun }> = ({ node }) => {
  return (
    <div>
      <NodeVariable label="Node Type:" text="Sleep" />
      <NodeVariable label="maturationTime:" text={node.maturationTime} type="date" />
      <NodeVariable label="matured:" text={node.matured ? 'Yes' : 'No'} />
    </div>
  )
}
