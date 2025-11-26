import { useDiagram } from '../../../hooks/useDiagram'
import { ExternalEventRunNode } from './ExternalEventRunNode'
import { SleepRunNode } from './SleepRunNode'
import { FC } from 'react'
import { UserTaskRunNode } from './UserTaskRunNode'
import { WaitForThreadsNodeRun } from './WaitForThreadNodeRun'
import { StartMultipleThreadRunNode } from './StartMultipleThreadRunNode'
import { ThrowEventRunNode } from './ThrowEventRunNode'
import { OctagonAlert } from 'lucide-react'
import { StartThreadRunNode } from './StartThreadRunNode'
import { TaskRunNode } from './TaskRunNode'
import { ChildWFNodeRun } from './ChildWFNodeRun'

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
  if ($case === 'userTask') return <UserTaskRunNode node={value} />
  if ($case === 'sleep') return <SleepRunNode node={value} />
  if ($case === 'waitForThreads') return <WaitForThreadsNodeRun node={value} />
  if ($case === 'startThread') return <StartThreadRunNode node={value} />
  if ($case === 'startMultipleThreads') return <StartMultipleThreadRunNode node={value} />
  if ($case === 'throwEvent') return <ThrowEventRunNode node={value} />
  if ($case === 'runChildWf') return <ChildWFNodeRun node={value} />

  return (
    <div className="mt-2 flex justify-center">
      <div>
        <div className="flex justify-center">
          <OctagonAlert className="lex justify-center" />
        </div>
        <div>No information required here. </div>
      </div>
    </div>
  )
}
