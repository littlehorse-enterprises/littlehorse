import { FC } from 'react'
import { useDiagram } from '../../../hooks/useDiagram'
import { ExitNode } from './ExitNode'
import { ExternalEventNode } from './ExternalEventNode'
import { StartThreadNode } from './StartThreadNode'
import { TaskNode } from './TaskNode'
import { ThrowEventNode } from './ThrowEventNode'
import { WaitForThreadsNode } from './WaitForThreadsNode'
import { SleepNode } from './SleepNode'
import { WaitForCondition } from './WaitForConditionNode'
import { UserTaskNode } from './UserTaskNode/UserTaskNode'

export const Node: FC = () => {
  const { selectedNode } = useDiagram()

  if (!selectedNode || !selectedNode.data.node) return

  const { $case, value } = selectedNode.data.node

  if ($case === 'task') return <TaskNode node={value} />
  if ($case === 'exit') return <ExitNode node={value} />
  if ($case === 'externalEvent') return <ExternalEventNode node={value} />
  if ($case === 'startThread') return <StartThreadNode node={value} />
  if ($case === 'waitForThreads') return <WaitForThreadsNode node={value} />
  if ($case === 'throwEvent') return <ThrowEventNode node={value} />
  if ($case === 'sleep') return <SleepNode node={value} />
  if ($case === 'waitForCondition') return <WaitForCondition node={value} />
  if ($case === 'userTask') return <UserTaskNode node={value} />

  return <></>
}
