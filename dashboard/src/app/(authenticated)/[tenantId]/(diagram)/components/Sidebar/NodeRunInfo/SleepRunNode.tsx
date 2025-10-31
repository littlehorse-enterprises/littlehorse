import { SleepNodeRun } from 'littlehorse-client/proto'
import { FC } from 'react'
import { LabelContent } from '../Components'
import { utcToLocalDateTime } from '@/app/utils'

export const SleepRunNode: FC<{ node: SleepNodeRun }> = ({ node }) => {
  return (
    <div>
      <LabelContent label="Node Type" content="Sleep"></LabelContent>
      <LabelContent label="Maturation Time" content={utcToLocalDateTime(`${node.maturationTime}`)}></LabelContent>
      <LabelContent label="Maturation status" content={node.matured? "Ready": "No ready yet"}></LabelContent>
    </div>
  )
}
