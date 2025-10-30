import { useDiagram } from '../../../hooks/useDiagram'
import { ExternalEventRunNode } from './ExternalEventRunNode'
import { SleepRunNode } from './SleepRunNode'
import { TaskRunNode } from './TaskRunNode'
import { FC } from 'react'
import { WaitForCondition } from './WaitForConditioNodeRun'

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
  if ($case === 'externalEvent') return <ExternalEventRunNode node={value} />
  if ($case === 'waitForCondition') return <WaitForCondition  />
  if ($case === 'sleep') return <SleepRunNode node={value} />

  return <></>
}
