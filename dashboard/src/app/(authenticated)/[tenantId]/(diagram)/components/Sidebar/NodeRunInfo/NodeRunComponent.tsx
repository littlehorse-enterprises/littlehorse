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
import { ExitNodeRun } from './ExitNodeRun'
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
  if (!nodeRun) {
    return null
  }

  const nodeType = nodeRun.nodeType!
  // Key by taskGuid so switching to a different TaskRun remounts TaskNodeRun and
  // resets its internal attemptIndex, instead of reusing the previous node's index.
  if (nodeType.oneofKind === 'task') return <TaskNodeRun key={nodeType.task.taskRunId?.taskGuid} node={nodeType.task} />
  if (nodeType.oneofKind === 'externalEvent') return <ExternalEventNodeRun node={nodeType.externalEvent} />
  if (nodeType.oneofKind === 'userTask') return <UserTaskNodeRun node={nodeType.userTask} />
  if (nodeType.oneofKind === 'sleep') return <SleepNodeRun node={nodeType.sleep} />
  if (nodeType.oneofKind === 'waitForThreads') return <WaitForThreadsNodeRun node={nodeType.waitForThreads} />
  if (nodeType.oneofKind === 'startThread') return <StartThreadNodeRun node={nodeType.startThread} />
  if (nodeType.oneofKind === 'startMultipleThreads')
    return <StartMultipleThreadNodeRun node={nodeType.startMultipleThreads} />
  if (nodeType.oneofKind === 'throwEvent') return <ThrowEventNodeRun node={nodeType.throwEvent} />
  if (nodeType.oneofKind === 'runChildWf') return <ChildWFNodeRun node={nodeType.runChildWf} />
  if (nodeType.oneofKind === 'waitForChildWf') return <WaitForChildWfNodeRun node={nodeType.waitForChildWf} />
  if (nodeType.oneofKind === 'exit') return <ExitNodeRun />

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
