import { useDiagram } from '../../../hooks/useDiagram'
import { ExternalEventNodeRun } from './ExternalEventNodeRun/ExternalEventNodeRun'
import { SleepNodeRun } from './SleepNodeRun'
import { FC } from 'react'
import { UserTaskNodeRun } from './UserTaskNodeRun/UserTaskNodeRun'
import { WaitForThreadsNodeRun } from './WaitForThreadNodeRun'
import { StartMultipleThreadNodeRun } from './StartMultipleThreadNodeRun'
import { ThrowEventNodeRun } from './ThrowEventNodeRun'
import { OctagonAlert } from 'lucide-react'
import { StartThreadNodeRun } from './StartThreadNodeRun'
import { TaskNodeRun } from './TaskNodeRun'
import { ChildWFNodeRun } from './ChildWFNodeRun'
import { WaitForChildWfNodeRun } from './WaitForChildWfNodeRun'

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
  if ($case === 'task') return <TaskNodeRun node={value} />
  if ($case === 'externalEvent') return <ExternalEventNodeRun node={value} />
  if ($case === 'userTask') return <UserTaskNodeRun node={value} />
  if ($case === 'sleep') return <SleepNodeRun node={value} />
  if ($case === 'waitForThreads') return <WaitForThreadsNodeRun node={value} />
  if ($case === 'startThread') return <StartThreadNodeRun node={value} />
  if ($case === 'startMultipleThreads') return <StartMultipleThreadNodeRun node={value} />
  if ($case === 'throwEvent') return <ThrowEventNodeRun node={value} />
  if ($case === 'runChildWf') return <ChildWFNodeRun node={value} />
  if ($case === 'waitForChildWf') return <WaitForChildWfNodeRun node={value} />

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
