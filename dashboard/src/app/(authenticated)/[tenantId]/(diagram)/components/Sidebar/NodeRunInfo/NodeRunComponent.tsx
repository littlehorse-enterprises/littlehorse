import { useDiagram } from '../../../hooks/useDiagram'
import { SleepRunNode } from './SleepRunNode'
import { TaskRunNode } from './TaskRunNode'
import { FC } from 'react'

export const NodeRunComponent: FC<{ nodeRunIndex: number }> = ({ nodeRunIndex }) => {
  const { selectedNode } = useDiagram()

  if (!selectedNode) {
    return null
  }

  if (!('nodeRunsList' in selectedNode.data)) {
    return null
  }

  const nodeRun = selectedNode.data.nodeRunsList[nodeRunIndex]

  const { $case, value } = nodeRun.nodeType!
  if ($case === 'task') return <TaskRunNode node={value} />
  if ($case  === 'sleep') return <SleepRunNode/>


  return <></>
}
